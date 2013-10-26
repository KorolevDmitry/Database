import DatabaseBase.components.Balancer;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.exceptions.ConnectionException;
import DatabaseClient.api.TcpSender;
import DatabaseClient.parser.Lexer;
import DatabaseClient.parser.ServerCommand;
import DatabaseClient.parser.commands.RequestCommand;
import DatabaseServer.api.ServerEvaluator;
import DatabaseServer.api.TcpListener;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;
import DatabaseServer.parser.ServerParser;
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
public class ClientServerIntegrationTest {
    @Test
    public void Start_1Server1Client_NoErrors() throws IOException {
        //arrange
        TcpListener<String, String> server1 = CreateServer(1107);
        TcpSender<String, String> client1 = CreateClient("localhost:1107");
        server1.Start();
        ServerCommand<String> command1 = new ServerCommand<String>(RequestCommand.ADD, "x1", "add x1 x1");
        ServerCommand<String> command2 = new ServerCommand<String>(RequestCommand.ADD, "x2", "add x2 x2");
        ServerCommand<String> command3 = new ServerCommand<String>(RequestCommand.GET, "x1", "get x1");
        EvaluationResult<String, String> result1 = null;
        EvaluationResult<String, String> result2 = null;
        EvaluationResult<String, String> result3 = null;
        boolean hasException = false;

        //act
        try {
            result1 = client1.Send(command1);
            result2 = client1.Send(command2);
            result3 = client1.Send(command3);
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
        assertEquals("x1", result3.Result);
    }

    @Test
    public void Start_2Servers1Client_NoErrors() throws IOException {
        //arrange
        TcpListener<String, String> server1 = CreateServer(1107);
        TcpListener<String, String> server2 = CreateServer(1108);
        TcpSender<String, String> client1 = CreateClient("localhost:1107;localhost:1108");
        server1.Start();
        server2.Start();
        ServerCommand<String> command1 = new ServerCommand<String>(RequestCommand.ADD, "x1", "add x1 x1");
        ServerCommand<String> command2 = new ServerCommand<String>(RequestCommand.ADD, "x2", "add x2 x2");
        ServerCommand<String> command3 = new ServerCommand<String>(RequestCommand.GET, "x1", "get x1");
        EvaluationResult<String, String> result1 = null;
        EvaluationResult<String, String> result2 = null;
        EvaluationResult<String, String> result3 = null;
        boolean hasException = false;

        //act
        try {
            result1 = client1.Send(command1);
            result2 = client1.Send(command2);
            result3 = client1.Send(command3);
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
        assertEquals("x1", result3.Result);
    }

    private TcpListener<String, String> CreateServer(int port) {
        MemoryBasedDataStorage<String, String> dataStorage = new MemoryBasedDataStorage<String, String>();
        ServerEvaluator<String, String> evaluator = new ServerEvaluator<String, String>(dataStorage, new ServerParser(new Lexer()));
        TcpListener<String, String> listener = new TcpListener<String, String>(evaluator, port);
        return listener;
    }

    private TcpSender<String, String> CreateClient(String listOfServers) {
        Balancer<String, String> balancer = new Balancer<String, String>(listOfServers);
        TcpSender<String, String> sender = new TcpSender<String, String>(balancer);
        return sender;
    }
}
