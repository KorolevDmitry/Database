package DatabaseServer.api;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandKeyValueNode;
import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.Route;
import DatabaseBase.entities.StringSizable;
import DatabaseBase.interfaces.IDataStorage;
import DatabaseBase.mocks.TcpSenderMock;
import DatabaseBase.parser.Lexer;
import DatabaseBase.parser.ParserStringString;
import DatabaseBase.utils.Observer;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/23/13
 * Time: 11:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerEvaluatorTest {
    private ServerEvaluator<StringSizable, StringSizable> _serverEvaluator;
    private ObserverQuery _observerQuery;
    private ObserverEvaluationResult _observerEvaluationResult;

    @Before
    public void setUp() throws IOException {
        IDataStorage<StringSizable, StringSizable> dataStorage = new MemoryBasedDataStorage<StringSizable, StringSizable>();
        Route route = new Route("localhost", 1111);
        _serverEvaluator = new ServerEvaluator<StringSizable, StringSizable>(dataStorage,
                new ParserStringString(new Lexer()), route, new TcpSenderMock<StringSizable, StringSizable>());
        _observerQuery = new ObserverQuery();
        _observerEvaluationResult = new ObserverEvaluationResult();
        _serverEvaluator.AddMessageReceivedObserver(_observerQuery);
        _serverEvaluator.AddMessageExecutedObserver(_observerEvaluationResult);
    }

    @Test
    public void Evaluate_QueryNull_HasError() {
        //arrange

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate((Query)null);

        //assert
        assertTrue(result.HasError);
        assertFalse(_observerQuery.Received);
        assertFalse(_observerEvaluationResult.Received);
    }

    @Test
    public void Evaluate_CommandNull_HasError() {
        //arrange
        Query query = new Query();

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertTrue(result.HasError);
        assertFalse(_observerQuery.Received);
        assertFalse(_observerEvaluationResult.Received);
    }

    @Test
    public void Evaluate_CommandSingleNodeQuit_Quit() {
        //arrange
        Query query = new Query();
        query.Command = new CommandSingleNode(RequestCommand.QUIT);

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        assertTrue(result.Quit);
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandSingleNodeHelp_Help() {
        //arrange
        Query query = new Query();
        query.Command = new CommandSingleNode(RequestCommand.HELP);

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandSingleNodeOther_HasError() {
        //arrange
        Query query = new Query();
        query.Command = new CommandSingleNode(RequestCommand.ADD);

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertTrue(result.HasError);
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandKeyNodeGet_NoErrorResultEmpty() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x"));

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        assertTrue(result.HasReturnResult);
        assertEquals(0, result.Result.size());
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandKeyNodeDelete_NoErrorNoResult() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyNode<StringSizable>(RequestCommand.DELETE, new StringSizable("x"));

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        assertFalse(result.HasReturnResult);
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandKeyNodeOther_HasError() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyNode<StringSizable>(RequestCommand.ADD, new StringSizable("x"));

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertTrue(result.HasError);
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandKeyValueNodeAdd_NoErrorNoResult() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x"), new StringSizable("x"));

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        assertFalse(result.HasReturnResult);
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandKeyValueNodeOther_HasError() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.GET, new StringSizable("x"), new StringSizable("x"));

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertTrue(result.HasError);
        AssertObservers(query, result);
    }

    private void AssertObservers(Query query, EvaluationResult<StringSizable, StringSizable> result){
        assertTrue(_observerQuery.Received);
        assertTrue(_observerEvaluationResult.Received);
        assertEquals(query, _observerQuery.ReceivedQuery);
        assertEquals(result, _observerEvaluationResult.ReceivedResult);
    }
}

class ObserverQuery implements Observer<Query> {
    Query ReceivedQuery;
    boolean Received;

    @Override
    public void update(Query data) {
        ReceivedQuery = data;
        Received = true;
    }
}

class ObserverEvaluationResult implements Observer<EvaluationResult<StringSizable, StringSizable>> {
    EvaluationResult<StringSizable, StringSizable> ReceivedResult;
    boolean Received;

    @Override
    public void update(EvaluationResult<StringSizable, StringSizable> data) {
        ReceivedResult = data;
        Received = true;
    }
}
