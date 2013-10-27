package DatabaseServer.api;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandKeyValueNode;
import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.StringSizable;
import DatabaseBase.interfaces.IDataStorage;
import DatabaseBase.parser.Lexer;
import DatabaseBase.utils.Observer;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;
import DatabaseServer.parser.ServerParserStringString;
import org.junit.Before;
import org.junit.Test;

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
    public void setUp()
    {
        IDataStorage<StringSizable, StringSizable> dataStorage = new MemoryBasedDataStorage<StringSizable, StringSizable>();
        _serverEvaluator = new ServerEvaluator<StringSizable, StringSizable>(dataStorage, new ServerParserStringString(new Lexer()));
        _observerQuery = new ObserverQuery();
        _observerEvaluationResult = new ObserverEvaluationResult();
        _serverEvaluator.AddMessageReceivedObserver(_observerQuery);
        _serverEvaluator.AddMessageExecutedObserver(_observerEvaluationResult);
    }

    @Test
    public void Evaluate_QueryNull_HasError() {
        //arrange

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(null);

        //assert
        assertTrue(result.HasError);
        assertFalse(_observerQuery.Received);
        assertFalse(_observerEvaluationResult.Received);
    }

    @Test
    public void Evaluate_CommandNull_HasError() {
        //arrange
        Query query = new Query();
        EvaluationResult<StringSizable, StringSizable> result = new EvaluationResult<StringSizable, StringSizable>();

        //act
        _serverEvaluator.Evaluate(query, result);

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
        EvaluationResult<StringSizable, StringSizable> result = new EvaluationResult<StringSizable, StringSizable>();

        //act
        _serverEvaluator.Evaluate(query, result);

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
        EvaluationResult<StringSizable, StringSizable> result = new EvaluationResult<StringSizable, StringSizable>();

        //act
        _serverEvaluator.Evaluate(query, result);

        //assert
        assertFalse(result.HasError);
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandSingleNodeOther_HasError() {
        //arrange
        Query query = new Query();
        query.Command = new CommandSingleNode(RequestCommand.ADD);
        EvaluationResult<StringSizable, StringSizable> result = new EvaluationResult<StringSizable, StringSizable>();

        //act
        _serverEvaluator.Evaluate(query, result);

        //assert
        assertTrue(result.HasError);
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandKeyNodeGet_NoErrorResultNull() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x"));
        EvaluationResult<StringSizable, StringSizable> result = new EvaluationResult<StringSizable, StringSizable>();

        //act
        _serverEvaluator.Evaluate(query, result);

        //assert
        assertFalse(result.HasError);
        assertTrue(result.HasReturnResult);
        assertNull(result.Result);
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandKeyNodeDelete_NoErrorNoResult() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyNode<StringSizable>(RequestCommand.DELETE, new StringSizable("x"));
        EvaluationResult<StringSizable, StringSizable> result = new EvaluationResult<StringSizable, StringSizable>();

        //act
        _serverEvaluator.Evaluate(query, result);

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
        EvaluationResult<StringSizable, StringSizable> result = new EvaluationResult<StringSizable, StringSizable>();

        //act
        _serverEvaluator.Evaluate(query, result);

        //assert
        assertTrue(result.HasError);
        AssertObservers(query, result);
    }

    @Test
    public void Evaluate_CommandKeyValueNodeAdd_NoErrorNoResult() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x"), new StringSizable("x"));
        EvaluationResult<StringSizable, StringSizable> result = new EvaluationResult<StringSizable, StringSizable>();

        //act
        _serverEvaluator.Evaluate(query, result);

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
        EvaluationResult<StringSizable, StringSizable> result = new EvaluationResult<StringSizable, StringSizable>();

        //act
        _serverEvaluator.Evaluate(query, result);

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
}