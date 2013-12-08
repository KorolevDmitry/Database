import DatabaseBalancer.DynamicBalancer;
import DatabaseBalancer.api.BalancerEvaluator;
import DatabaseBase.commands.CommandKeyValueNode;
import DatabaseBase.commands.CommandMultiKeyNode;
import DatabaseBase.commands.CommandMultiKeyValueNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.commands.service.ServiceCommand;
import DatabaseBase.components.TcpListener;
import DatabaseBase.components.TcpSender;
import DatabaseBase.entities.*;
import DatabaseBase.exceptions.BalancerException;
import DatabaseBase.interfaces.IBalancer;
import DatabaseBase.parser.Lexer;
import DatabaseBase.parser.ParserStringString;
import DatabaseClient.api.ClientEvaluator;
import DatabaseServer.api.ServerEvaluator;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 12/9/13
 * Time: 12:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class ClientServerMultiCommandsIntegrationTest {

    @Test
    public void MultiGet_1Server1Client_Got() throws IOException, BalancerException, InterruptedException {
        //arrange
        Route routeServer1 = new Route("localhost:1107", ServerRole.MASTER, null);
        Route routeBalancer = new Route("localhost:1111", ServerRole.MASTER, null);

        TcpListener<StringSizable, StringSizable> server1 = CreateServer(routeServer1);
        TcpListener<StringSizable, StringSizable> balancer = CreateBalancer(routeBalancer);
        ClientEvaluator<StringSizable, StringSizable> client1 = CreateClient(routeBalancer);

        server1.Start();
        balancer.Start();
        Query addServerQuery = new Query();
        ServiceCommand addServerCommand = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer1);
        addServerQuery.Command = addServerCommand;
        Query addQuery1 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> addCommand1 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x1"), new StringSizable("x1"));
        addQuery1.Command = addCommand1;
        Query addQuery2 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> addCommand2 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x2"), new StringSizable("x2"));
        addQuery2.Command = addCommand2;
        Query addQuery3 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> addCommand3 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x3"), new StringSizable("x3"));
        addQuery3.Command = addCommand3;
        Query getQuery = new Query();
        CommandMultiKeyNode<StringSizable> getCommand = new CommandMultiKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x0"), new StringSizable("x4"));
        getQuery.Command = getCommand;
        EvaluationResult<StringSizable, StringSizable> addServerResult = null;
        EvaluationResult<StringSizable, StringSizable> add1Result = null;
        EvaluationResult<StringSizable, StringSizable> add2Result = null;
        EvaluationResult<StringSizable, StringSizable> add3Result = null;
        EvaluationResult<StringSizable, StringSizable> getResult = null;
        boolean hasException = false;

        //act
        addServerResult = client1.Evaluate(addServerQuery);
        Thread.sleep(1000);
        add1Result = client1.Evaluate(addQuery1);
        add2Result = client1.Evaluate(addQuery2);
        add3Result = client1.Evaluate(addQuery3);
        getResult = client1.Evaluate(getQuery);
        server1.Stop();
        balancer.Stop();

        //assert
        assertFalse(hasException);
        assertFalse(addServerResult.ErrorDescription, addServerResult.HasError);
        assertFalse(add1Result.ErrorDescription, add1Result.HasError);
        assertFalse(add2Result.ErrorDescription, add2Result.HasError);
        assertFalse(add3Result.ErrorDescription, add3Result.HasError);
        assertFalse(getResult.ErrorDescription, getResult.HasError);
        assertTrue(getResult.HasReturnResult);
        assertEquals(3, getResult.Result.size());
    }

    @Test
    public void MultiUpdate_1Server1Client_Got() throws IOException, BalancerException, InterruptedException {
        //arrange
        Route routeServer1 = new Route("localhost:1107", ServerRole.MASTER, null);
        Route routeBalancer = new Route("localhost:1111", ServerRole.MASTER, null);

        TcpListener<StringSizable, StringSizable> server1 = CreateServer(routeServer1);
        TcpListener<StringSizable, StringSizable> balancer = CreateBalancer(routeBalancer);
        ClientEvaluator<StringSizable, StringSizable> client1 = CreateClient(routeBalancer);

        server1.Start();
        balancer.Start();
        Query addServerQuery = new Query();
        ServiceCommand addServerCommand = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer1);
        addServerQuery.Command = addServerCommand;
        Query addQuery1 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> addCommand1 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x1"), new StringSizable("x1"));
        addQuery1.Command = addCommand1;
        Query addQuery2 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> addCommand2 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x2"), new StringSizable("x2"));
        addQuery2.Command = addCommand2;
        Query addQuery3 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> addCommand3 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x3"), new StringSizable("x3"));
        addQuery3.Command = addCommand3;
        Query updateQuery = new Query();
        CommandMultiKeyValueNode<StringSizable, StringSizable> updateCommand = new CommandMultiKeyValueNode<StringSizable, StringSizable>(RequestCommand.UPDATE, new StringSizable("x0"), new StringSizable("x3"), new StringSizable("x0"));
        updateQuery.Command = updateCommand;
        Query getQuery = new Query();
        CommandMultiKeyNode<StringSizable> getCommand = new CommandMultiKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x0"), new StringSizable("x4"));
        getQuery.Command = getCommand;
        EvaluationResult<StringSizable, StringSizable> addServerResult = null;
        EvaluationResult<StringSizable, StringSizable> add1Result = null;
        EvaluationResult<StringSizable, StringSizable> add2Result = null;
        EvaluationResult<StringSizable, StringSizable> add3Result = null;
        EvaluationResult<StringSizable, StringSizable> updateResult = null;
        EvaluationResult<StringSizable, StringSizable> getResult = null;
        boolean hasException = false;

        //act
        addServerResult = client1.Evaluate(addServerQuery);
        Thread.sleep(1000);
        add1Result = client1.Evaluate(addQuery1);
        add2Result = client1.Evaluate(addQuery2);
        add3Result = client1.Evaluate(addQuery3);
        updateResult = client1.Evaluate(updateQuery);
        getResult = client1.Evaluate(getQuery);
        server1.Stop();
        balancer.Stop();

        //assert
        assertFalse(hasException);
        assertFalse(addServerResult.ErrorDescription, addServerResult.HasError);
        assertFalse(add1Result.ErrorDescription, add1Result.HasError);
        assertFalse(add2Result.ErrorDescription, add2Result.HasError);
        assertFalse(add3Result.ErrorDescription, add3Result.HasError);
        assertFalse(updateResult.ErrorDescription, updateResult.HasError);
        assertFalse(getResult.ErrorDescription, getResult.HasError);
        assertTrue(getResult.HasReturnResult);
        assertEquals(3, getResult.Result.size());
        for(int i=0;i<getResult.Result.size();i++){
            assertEquals(new StringSizable("x0"), getResult.Result.get(i).Value);
        }
    }

    private TcpListener<StringSizable, StringSizable> CreateServer(Route route) throws IOException {
        MemoryBasedDataStorage<StringSizable, StringSizable> dataStorage = new MemoryBasedDataStorage<StringSizable, StringSizable>();
        TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
        ServerEvaluator<StringSizable, StringSizable> evaluator =
                new ServerEvaluator<StringSizable, StringSizable>(dataStorage,
                        new ParserStringString(new Lexer()), route, sender);
        TcpListener<StringSizable, StringSizable> listener = new TcpListener<StringSizable, StringSizable>(evaluator, route.Port);
        return listener;
    }

    private TcpListener<StringSizable, StringSizable> CreateBalancer(Route route) throws IOException {
        TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
        IBalancer balancer = new DynamicBalancer(sender, 500);
        BalancerEvaluator<StringSizable, StringSizable> evaluator = new BalancerEvaluator<StringSizable, StringSizable>(balancer, new ParserStringString(new Lexer()));
        TcpListener<StringSizable, StringSizable> listener = new TcpListener<StringSizable, StringSizable>(evaluator, route.Port);
        return listener;
    }

    private ClientEvaluator<StringSizable, StringSizable> CreateClient(Route balancer) {
        TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
        ClientEvaluator<StringSizable, StringSizable> client = new ClientEvaluator<StringSizable, StringSizable>(sender,
                new ParserStringString(new Lexer()), balancer);
        return client;
    }
}
