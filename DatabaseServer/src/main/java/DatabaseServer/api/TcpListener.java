package DatabaseServer.api;

import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.interfaces.ISizable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/26/13
 * Time: 2:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class TcpListener<TKey extends ISizable, TValue extends ISizable> implements Runnable {
    private ServerEvaluator<TKey, TValue> _serverEvaluator;
    private int _port;

    private Thread _thread;
    private static boolean _stopRequest;
    private static boolean _isWorking;

    public TcpListener(ServerEvaluator<TKey, TValue> serverEvaluator, int port) {
        _serverEvaluator = serverEvaluator;
        _port = port;
        _thread = new Thread(this);
    }

    public void Start() {
        _stopRequest = false;
        _isWorking = true;
        _thread.start();
    }

    public void Stop() {
        _stopRequest = true;
        while (_isWorking) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    @Override
    public void run() {
        String clientSentence;
        ServerSocket welcomeSocket = null;
        EvaluationResult result = null;
        try {
            welcomeSocket = new ServerSocket(_port);
            welcomeSocket.setSoTimeout(1000);
            while (!_stopRequest) {
                Socket connectionSocket = null;
                try {
                    connectionSocket = welcomeSocket.accept();
                } catch (SocketTimeoutException e) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        return;
                    }
                    continue;
                }
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                clientSentence = inFromClient.readLine();
                if (clientSentence == null || clientSentence.isEmpty())
                    continue;
                System.out.println(_port + " received: " + clientSentence);
                try {
                    result = _serverEvaluator.Evaluate(clientSentence);
                    if (result.Quit) {
                        Stop();
                    }
                } finally {
                    ObjectOutputStream outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
                    outToClient.writeObject(result);
                    if (connectionSocket != null) {
                        try {
                            connectionSocket.shutdownOutput();
                            connectionSocket.shutdownInput();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            _isWorking = false;
            if (welcomeSocket != null) {
                try {
                    welcomeSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
