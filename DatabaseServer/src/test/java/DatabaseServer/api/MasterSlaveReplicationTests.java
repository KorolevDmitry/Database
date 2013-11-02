package DatabaseServer.api;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandKeyValueNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.commands.service.ReplicateCommand;
import DatabaseBase.entities.*;
import DatabaseBase.interfaces.IDataStorage;
import DatabaseBase.mocks.TcpSenderMock;
import DatabaseBase.parser.Lexer;
import DatabaseBase.parser.ParserStringString;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 11/2/13
 * Time: 1:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class MasterSlaveReplicationTests {
    private ServerEvaluator<StringSizable, StringSizable> _serverEvaluator;
    private TcpSenderMock<StringSizable, StringSizable> _senderMock;
    private Route _route;
    private ObserverQuery _observerQuery;
    private ObserverEvaluationResult _observerEvaluationResult;

    @Before
    public void setUp() throws IOException {
        IDataStorage<StringSizable, StringSizable> dataStorage = new MemoryBasedDataStorage<StringSizable, StringSizable>();
        _route = new Route("localhost", 1111);
        _route.Slaves.add(new Route("localhost:2222", ServerRole.SLAVE, _route));
        _senderMock = new TcpSenderMock<StringSizable, StringSizable>();
        _serverEvaluator = new ServerEvaluator<StringSizable, StringSizable>(dataStorage,
                new ParserStringString(new Lexer()), _route, _senderMock);
        _observerQuery = new ObserverQuery();
        _observerEvaluationResult = new ObserverEvaluationResult();
        _serverEvaluator.AddMessageReceivedObserver(_observerQuery);
        _serverEvaluator.AddMessageExecutedObserver(_observerEvaluationResult);
    }

    @Test
    public void Evaluate_ModifyRequest_SentToSlave() throws InterruptedException {
        //arrange
        _senderMock.AddExpectedBehavior(_route.Slaves.get(0), RequestCommand.REPLICATE, GetExpectedResult());
        Query query = new Query();
        query.Command = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x"), new StringSizable("x"));

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        Thread.sleep(1000);
        assertEquals(1, _senderMock.SequenceOfSentCommands.size());
        assertEquals(RequestCommand.REPLICATE, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        ReplicateCommand replicateCommand = (ReplicateCommand) _senderMock.SequenceOfSentCommands.get(0);
        assertEquals(1, replicateCommand.Queries.size());
        assertEquals(query, replicateCommand.Queries.get(0));
    }

    @Test
    public void Evaluate_GetRequest_NotSentToSlave() {
        //arrange
        _senderMock.AddExpectedBehavior(_route.Slaves.get(0), RequestCommand.REPLICATE, GetExpectedResult());
        Query query = new Query();
        query.Command = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x"));

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        assertEquals(0, _senderMock.SequenceOfSentCommands.size());
    }

    @Test
    public void Evaluate_ModifyRequestFailed_NotSentToSlave() {
        //arrange
        _senderMock.AddExpectedBehavior(_route.Slaves.get(0), RequestCommand.REPLICATE, GetExpectedResult());
        Query query = new Query();
        query.Command = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.UPDATE, new StringSizable("x"), new StringSizable("x"));

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertTrue(result.HasError);
        assertEquals(0, _senderMock.SequenceOfSentCommands.size());
    }

    @Test
    public void Evaluate_ReplicateRequestWithItems_ItemsAdded() throws InterruptedException {
        //arrange
        _senderMock.AddExpectedBehavior(_route.Slaves.get(0), RequestCommand.REPLICATE, GetExpectedResult());
        Query query = new Query();
        ReplicateCommand replicateCommand = new ReplicateCommand(_route, _route, 0, 0, false);
        Query innerQuery = new Query();
        innerQuery.Command = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x"), new StringSizable("x"));
        replicateCommand.Queries = new ArrayList<Query>();
        replicateCommand.Queries.add(innerQuery);
        query.Command = replicateCommand;

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        Thread.sleep(1000);
        assertEquals(1, _senderMock.SequenceOfSentCommands.size());
        Query getQuery = new Query();
        getQuery.Command = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x"));
        result = _serverEvaluator.Evaluate(getQuery);
        assertFalse(result.HasError);
        assertTrue(result.HasReturnResult);
        assertEquals(new StringSizable("x"), result.Result);
    }

    @Test
    public void Evaluate_ReplicateRequest_SentItemsToSlave() throws InterruptedException {
        //arrange
        _senderMock.AddExpectedBehavior(_route.Slaves.get(0), RequestCommand.REPLICATE, GetExpectedResult());
        Query addQuery = new Query();
        addQuery.Command = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x"), new StringSizable("x"));
        _serverEvaluator.Evaluate(addQuery);
        Query query = new Query();
        ReplicateCommand replicateCommand = new ReplicateCommand(_route, _route.Slaves.get(0), 0, 0, false);
        query.Command = replicateCommand;

        //act
        EvaluationResult<StringSizable, StringSizable> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        Thread.sleep(1000);
        assertEquals(2, _senderMock.SequenceOfSentCommands.size());
        assertEquals(RequestCommand.REPLICATE, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        assertEquals(RequestCommand.REPLICATE, _senderMock.SequenceOfSentCommands.get(1).GetCommand());
        replicateCommand = (ReplicateCommand) _senderMock.SequenceOfSentCommands.get(0);
        assertEquals(1, replicateCommand.Queries.size());
        assertEquals(addQuery, replicateCommand.Queries.get(0));
        replicateCommand = (ReplicateCommand) _senderMock.SequenceOfSentCommands.get(1);
        assertEquals(1, replicateCommand.Queries.size());
        assertEquals(addQuery, replicateCommand.Queries.get(0));
    }

    private EvaluationResult<StringSizable, StringSizable> GetExpectedResult() {
        EvaluationResult<StringSizable, StringSizable> result = new EvaluationResult<StringSizable, StringSizable>();
        result.HasError = false;

        return result;
    }
}
