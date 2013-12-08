package DatabaseBase.components;

import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.interfaces.ISizable;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/26/13
 * Time: 2:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class TcpListener<TKey extends ISizable, TValue extends ISizable> implements Runnable {
    private Evaluator<TKey, TValue> _Evaluator;
    private int _port;

    private Thread _thread;
    private boolean _stopRequest;
    private boolean _isWorking;

    public TcpListener(Evaluator<TKey, TValue> serverEvaluator, int port) {
        _Evaluator = serverEvaluator;
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
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        } while (_isWorking);
    }

    @Override
    public void run() {
        Query query;
        ServerSocket welcomeSocket = null;
        EvaluationResult result = null;
        try {
            welcomeSocket = new ServerSocket(_port);
            welcomeSocket.setSoTimeout(1000);
            while (!_stopRequest) {
                Socket connectionSocket = null;
                try {
                    connectionSocket = welcomeSocket.accept();
                    ObjectInputStream inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
                    query = (Query) inFromClient.readObject();
                    if (query == null)
                        continue;
                    System.out.println(new Date().toString() + " " + _port + " received: " + query);
                    result = _Evaluator.Evaluate(query);
                    if (result.Quit) {
                        Stop();
                    }
                } catch (SocketTimeoutException e) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        return;
                    }
                    continue;
                } catch (SocketException e) {
                    e.printStackTrace();
                    continue;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    continue;
                } catch (EOFException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    continue;
                } finally {
                    if (connectionSocket != null) {
                        try {
                            ObjectOutputStream outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
                            outToClient.writeObject(result);
                            connectionSocket.shutdownOutput();
                            connectionSocket.shutdownInput();
                        } catch (SocketException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (welcomeSocket != null) {
                try {
                    welcomeSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (_Evaluator != null) {
                _Evaluator.Close();
                _Evaluator = null;
            }
            _isWorking = false;
        }
    }
}
