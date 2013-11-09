import DatabaseBalancer.DynamicBalancer;
import DatabaseBalancer.api.BalancerEvaluator;
import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandKeyValueNode;
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 11/9/13
 * Time: 8:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientServerReadWriteConstraintsTests {
    @Test
    public void Read_1Server2Needed_Error() throws IOException, BalancerException, InterruptedException {
        //arrange
        Route routeServer1 = new Route("localhost:1107", ServerRole.MASTER, null);
        Route routeBalancer = new Route("localhost:1111", ServerRole.MASTER, null);

        TcpListener<StringSizable, StringSizable> server1 = CreateServer(routeServer1);
        TcpListener<StringSizable, StringSizable> balancer = CreateBalancer(routeBalancer);
        ClientEvaluator<StringSizable, StringSizable> client1 = CreateClient(routeBalancer, 2, 1);

        server1.Start();
        balancer.Start();
        Query query1 = new Query();
        ServiceCommand command1 = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer1);
        query1.Command = command1;
        Query query2 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> command2 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x1"), new StringSizable("x1"));
        query2.Command = command2;
        Query query3 = new Query();
        CommandKeyNode<StringSizable> command3 = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x1"));
        query3.Command = command3;
        EvaluationResult<StringSizable, StringSizable> result1 = null;
        EvaluationResult<StringSizable, StringSizable> result2 = null;
        EvaluationResult<StringSizable, StringSizable> result3 = null;
        boolean hasException = false;

        //act
        result1 = client1.Evaluate(query1);
        Thread.sleep(1000);
        result2 = client1.Evaluate(query2);
        result3 = client1.Evaluate(query3);
        server1.Stop();
        balancer.Stop();

        //assert
        assertFalse(hasException);
        assertFalse(result1.ErrorDescription, result1.HasError);
        assertFalse(result2.ErrorDescription, result2.HasError);
        assertTrue(result3.HasError);
    }

    @Test
    public void Read_2Server2Needed_Success() throws IOException, BalancerException, InterruptedException {
        //arrange
        Route routeServer1 = new Route("localhost:1107", ServerRole.MASTER, null);
        Route routeServer2 = new Route("localhost:1108", ServerRole.SLAVE, routeServer1);
        Route routeBalancer = new Route("localhost:1111", ServerRole.MASTER, null);

        TcpListener<StringSizable, StringSizable> server1 = CreateServer(routeServer1);
        TcpListener<StringSizable, StringSizable> server2 = CreateServer(routeServer2);
        TcpListener<StringSizable, StringSizable> balancer = CreateBalancer(routeBalancer);
        ClientEvaluator<StringSizable, StringSizable> client1 = CreateClient(routeBalancer, 2, 1);

        server1.Start();
        server2.Start();
        balancer.Start();
        Query query0 = new Query();
        ServiceCommand command0 = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer1);
        query0.Command = command0;
        Query query1 = new Query();
        ServiceCommand command1 = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer2);
        query1.Command = command1;
        Query query2 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> command2 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x1"), new StringSizable("x1"));
        query2.Command = command2;
        Query query3 = new Query();
        CommandKeyNode<StringSizable> command3 = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x1"));
        query3.Command = command3;
        EvaluationResult<StringSizable, StringSizable> result0 = null;
        EvaluationResult<StringSizable, StringSizable> result1 = null;
        EvaluationResult<StringSizable, StringSizable> result2 = null;
        EvaluationResult<StringSizable, StringSizable> result3 = null;
        boolean hasException = false;

        //act
        result0 = client1.Evaluate(query0);
        Thread.sleep(1000);
        result1 = client1.Evaluate(query1);
        Thread.sleep(1000);
        result2 = client1.Evaluate(query2);
        result3 = client1.Evaluate(query3);
        server1.Stop();
        server2.Stop();
        balancer.Stop();

        //assert
        assertFalse(hasException);
        assertFalse(result1.ErrorDescription, result0.HasError);
        assertFalse(result1.ErrorDescription, result1.HasError);
        assertFalse(result2.ErrorDescription, result2.HasError);
        assertFalse(result3.ErrorDescription, result3.HasError);
        assertTrue(result3.HasReturnResult);
        assertEquals(new StringSizable("x1"), result3.Result);
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

    private ClientEvaluator<StringSizable, StringSizable> CreateClient(Route balancer, int numberToRead, int numberToWrite) {
        TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
        ClientEvaluator<StringSizable, StringSizable> client = new ClientEvaluator<StringSizable, StringSizable>(sender,
                new ParserStringString(new Lexer()), balancer, numberToRead, numberToWrite);
        return client;
    }
}
