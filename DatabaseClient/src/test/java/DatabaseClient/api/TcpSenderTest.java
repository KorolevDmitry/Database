package DatabaseClient.api;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.components.StaticBalancer;
import DatabaseBase.components.TcpSender;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.Route;
import DatabaseBase.entities.StringSizable;
import DatabaseBase.exceptions.ConnectionException;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static junit.framework.Assert.*;

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
    TcpSender<StringSizable, StringSizable> _sender;
    StaticBalancer _balancer;
    private static Query _messageReceivedOnServer;
    private Thread _listenerThread;

    @Before
    public void setUp() throws Exception {
        if (_listenerThread != null) {
            _listenerThread.stop();
            _listenerThread = null;
        }
        _listenerThread = new Thread(this);
        _messageReceivedOnServer = null;
        _balancer = new StaticBalancer(_server);
        _sender = new TcpSender<StringSizable, StringSizable>();
    }

    @Test
    public void Send_NullArgument_IllegalArgumentException() {
        //arrange

        boolean hasException = false;

        //act
        try {
            _sender.Send(null, new Route("localhost", 1107));
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
        Query query = new Query();
        CommandKeyNode<StringSizable> command = new CommandKeyNode<StringSizable>(RequestCommand.ADD, new StringSizable(""));
        query.Command = command;
        boolean hasException = false;

        //act
        try {
            _sender.Send(query, _balancer.GetRoute(command, null));
        } catch (ConnectionException e) {
            hasException = true;
        }

        //assert
        assertTrue(hasException);
    }

    @Test
    public void Send_Listening_ConnectionException() throws Exception {
        //arrange
        Query query = new Query();
        String message = "test";
        CommandKeyNode<StringSizable> command = new CommandKeyNode<StringSizable>(RequestCommand.ADD, new StringSizable(message));
        query.Command = command;
        boolean hasException = false;
        StartListen();

        //act
        try {
            _sender.Send(query, _balancer.GetRoute(command, null));
        } catch (ConnectionException e) {
            hasException = true;
        }

        //assert
        assertFalse(hasException);
        assertEquals(query, _messageReceivedOnServer);
    }

    @Override
    public void run() {
        ServerSocket welcomeSocket;
        try {
            welcomeSocket = new ServerSocket(_serverPort);
            Socket connectionSocket = welcomeSocket.accept();
            ObjectInputStream inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
            _messageReceivedOnServer = (Query) inFromClient.readObject();
            ObjectOutputStream outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
            outToClient.writeObject(new EvaluationResult<StringSizable, StringSizable>());
            connectionSocket.shutdownInput();
            connectionSocket.shutdownOutput();
            connectionSocket.close();
            welcomeSocket.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
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
