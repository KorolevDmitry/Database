package DatabaseClient.api;

import DatabaseBase.components.Balancer;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Route;
import DatabaseBase.exceptions.ConnectionException;
import DatabaseBase.interfaces.ISizable;
import DatabaseClient.parser.ServerCommand;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/26/13
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TcpSender<TKey extends ISizable, TValue extends ISizable> {
    private Balancer<TKey, TValue> _balancer;

    public TcpSender(Balancer<TKey, TValue> balancer) {
        _balancer = balancer;
    }

    public EvaluationResult<TKey, TValue> Send(ServerCommand<TKey> command) throws ConnectionException {
        if (command == null)
            throw new IllegalArgumentException("command can not be null");
        Socket clientSocket = null;
        Route route = _balancer.GetRoute(command);
        try {
            clientSocket = new Socket(route.Address, route.Port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(command.toString() + '\n');
            ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());
            EvaluationResult<TKey, TValue> serverEvaluationResult = (EvaluationResult<TKey, TValue>) inFromServer.readObject();
            if (serverEvaluationResult == null)
                throw new ConnectionException("Null answer from: " + route.toString());
            return serverEvaluationResult;
        } catch (UnknownHostException e) {
            throw new ConnectionException("Unknown host: " + route.toString(), e);
        } catch (IOException e) {
            throw new ConnectionException("Communication problems with: " + route.toString(), e);
        } catch (ClassNotFoundException e) {
            throw new ConnectionException("Can not read answer from: " + route.toString(), e);
        } /*catch (ConnectException e){
            e.printStackTrace();
            throw new ConnectionException("Nobody listen: " + route.toString(), e);
        }*/ finally {
            if (clientSocket != null) {
                try {
                    clientSocket.shutdownOutput();
                    clientSocket.shutdownInput();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
