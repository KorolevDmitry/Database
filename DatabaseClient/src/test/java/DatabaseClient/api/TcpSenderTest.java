package DatabaseClient.api;

import DatabaseBase.components.Balancer;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.exceptions.ConnectionException;
import DatabaseClient.parser.ServerCommand;
import DatabaseClient.parser.commands.RequestCommand;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/26/13
 * Time: 5:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class TcpSenderTest implements Runnable {
    private final String _serverHost = "localhost";
    private final int _serverPort = 6507;
    private final String _server = _serverHost + ":" + _serverPort;
    TcpSender<String, String> _sender;
    private static String _messageReceivedOnServer;
    private Thread _listenerThread;

    @Before
    public void setUp() throws Exception {
        if (_listenerThread != null) {
            _listenerThread.stop();
            _listenerThread = null;
        }
        _listenerThread = new Thread(this);
        _messageReceivedOnServer = null;
        Balancer<String, String> balancer = new Balancer<String, String>(_server);
        _sender = new TcpSender<String, String>(balancer);
    }

    @Test
    public void Send_NullArgument_IllegalArgumentException() {
        //arrange

        boolean hasException = false;

        //act
        try {
            _sender.Send(null);
        } catch (IllegalArgumentException e) {
            hasException = true;
        } catch (ConnectionException e) {
            e.printStackTrace();
        }

        //assert
        assertTrue(hasException);
    }

    @Test
    public void Send_NobodyListenTo_ConnectionException() throws Exception {
        //arrange
        ServerCommand<String> command = new ServerCommand<String>(RequestCommand.ADD, "", "");
        boolean hasException = false;

        //act
        try {
            _sender.Send(command);
        } catch (ConnectionException e) {
            hasException = true;
        }

        //assert
        assertTrue(hasException);
    }

    @Test
    public void Send_Listening_ConnectionException() throws Exception {
        //arrange
        String message = "test";
        ServerCommand<String> command = new ServerCommand<String>(RequestCommand.ADD, "", message);
        boolean hasException = false;
        StartListen();

        //act
        try {
            _sender.Send(command);
        } catch (ConnectionException e) {
            hasException = true;
        }

        //assert
        assertFalse(hasException);
        assertEquals(message, _messageReceivedOnServer);
    }

    @Override
    public void run() {
        ServerSocket welcomeSocket;
        try {
            welcomeSocket = new ServerSocket(_serverPort);
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            _messageReceivedOnServer = inFromClient.readLine();
            ObjectOutputStream outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
            outToClient.writeObject(new EvaluationResult<String, String>());
            connectionSocket.shutdownInput();
            connectionSocket.shutdownOutput();
            connectionSocket.close();
            welcomeSocket.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void StartListen() {
        _listenerThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }
}
