package DatabaseBase.components;

import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.Route;
import DatabaseBase.exceptions.ConnectionException;
import DatabaseBase.interfaces.ISizable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public EvaluationResult<TKey, TValue> Send(Query query, Route route, int timeout) throws ConnectionException {
        if (query == null)
            throw new IllegalArgumentException("command can not be null");
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(route.Host, route.Port);
            clientSocket.setSoTimeout(timeout);
            ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            outToServer.writeObject(query);
            ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());
            EvaluationResult<TKey, TValue> result = (EvaluationResult<TKey, TValue>) inFromServer.readObject();
            if (result == null)
                throw new ConnectionException("Null answer from: " + route.toString());
            return result;
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

    public EvaluationResult<TKey, TValue> Send(Query query, Route route) throws ConnectionException {
        return Send(query, route, 0);
    }
}
