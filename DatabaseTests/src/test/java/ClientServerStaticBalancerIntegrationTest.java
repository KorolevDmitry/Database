import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandKeyValueNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.components.StaticBalancer;
import DatabaseBase.components.TcpListener;
import DatabaseBase.components.TcpSender;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.Route;
import DatabaseBase.entities.StringSizable;
import DatabaseBase.exceptions.BalancerException;
import DatabaseBase.exceptions.ConnectionException;
import DatabaseBase.interfaces.IBalancer;
import DatabaseBase.parser.Lexer;
import DatabaseBase.parser.ParserStringString;
import DatabaseServer.api.ServerEvaluator;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/24/13
 * Time: 1:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class ClientServerStaticBalancerIntegrationTest {
    @Test
    public void Start_1Server1Client_NoErrors() throws IOException, BalancerException {
        //arrange
        TcpListener<StringSizable, StringSizable> server1 = CreateServer(1107);
        TcpSender<StringSizable, StringSizable> client1 = CreateClient();
        IBalancer balancer = new StaticBalancer("localhost:1107");
        server1.Start();
        Query query1 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> command1 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x1"), new StringSizable("x1"));
        query1.Command = command1;
        Query query2 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> command2 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x2"), new StringSizable("x2"));
        query2.Command = command2;
        Query query3 = new Query();
        CommandKeyNode<StringSizable> command3 = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x1"));
        query3.Command = command3;
        EvaluationResult<StringSizable, StringSizable> result1 = null;
        EvaluationResult<StringSizable, StringSizable> result2 = null;
        EvaluationResult<StringSizable, StringSizable> result3 = null;
        boolean hasException = false;

        //act
        try {
            result1 = client1.Send(query1, balancer.GetRoute(command1, null));
            result2 = client1.Send(query2, balancer.GetRoute(command2, null));
            result3 = client1.Send(query3, balancer.GetRoute(command3, null));
        } catch (ConnectionException e) {
            hasException = true;
        }
        server1.Stop();

        //assert
        assertFalse(hasException);
        assertFalse(result1.HasError);
        assertFalse(result2.HasError);
        assertFalse(result3.HasError);
        assertTrue(result3.HasReturnResult);
        assertEquals(new StringSizable("x1"), result3.Result);
    }

    @Test
    public void Start_2Servers1Client_NoErrors() throws IOException, BalancerException {
        //arrange
        TcpListener<StringSizable, StringSizable> server1 = CreateServer(1107);
        TcpListener<StringSizable, StringSizable> server2 = CreateServer(1108);
        TcpSender<StringSizable, StringSizable> client1 = CreateClient();
        IBalancer balancer = new StaticBalancer("localhost:1107;localhost:1108");
        server1.Start();
        server2.Start();
        Query query1 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> command1 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x1"), new StringSizable("x1"));
        query1.Command = command1;
        Query query2 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> command2 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x2"), new StringSizable("x2"));
        query2.Command = command2;
        Query query3 = new Query();
        CommandKeyNode<StringSizable> command3 = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x1"));
        query3.Command = command3;
        EvaluationResult<StringSizable, StringSizable> result1 = null;
        EvaluationResult<StringSizable, StringSizable> result2 = null;
        EvaluationResult<StringSizable, StringSizable> result3 = null;
        boolean hasException = false;

        //act
        try {
            result1 = client1.Send(query1, balancer.GetRoute(command1, null));
            result2 = client1.Send(query2, balancer.GetRoute(command2, null));
            result3 = client1.Send(query3, balancer.GetRoute(command3, null));
        } catch (ConnectionException e) {
            hasException = true;
        }
        server1.Stop();
        server2.Stop();

        //assert
        assertFalse(hasException);
        assertFalse(result1.HasError);
        assertFalse(result2.HasError);
        assertFalse(result3.HasError);
        assertTrue(result3.HasReturnResult);
        assertEquals(new StringSizable("x1"), result3.Result);
    }

    private TcpListener<StringSizable, StringSizable> CreateServer(int port) throws IOException {
        MemoryBasedDataStorage<StringSizable, StringSizable> dataStorage = new MemoryBasedDataStorage<StringSizable, StringSizable>();
        Route route = new Route("localhost", port);
        TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
        ServerEvaluator<StringSizable, StringSizable> evaluator =
                new ServerEvaluator<StringSizable, StringSizable>(dataStorage,
                        new ParserStringString(new Lexer()), route, sender);
        TcpListener<StringSizable, StringSizable> listener = new TcpListener<StringSizable, StringSizable>(evaluator, port);
        return listener;
    }

    private TcpSender<StringSizable, StringSizable> CreateClient() {
        TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
        return sender;
    }
}
