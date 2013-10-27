package DatabaseBase.components;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandNode;
import DatabaseBase.entities.Route;
import DatabaseBase.interfaces.ISizable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/12/13
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class Balancer<TKey extends ISizable, TValue extends ISizable> {

    ConcurrentHashMap<Integer, Route> _routs;

    public Balancer(String serverStr) {
        LoadFromArgs(serverStr);
    }

    private void LoadFromArgs(String serversStr) {
        String[] servers = serversStr.split(";");
        _routs = new ConcurrentHashMap<Integer, Route>(servers.length);
        for (int i = 0; i < servers.length; i++) {
            String[] route = servers[i].split(":");
            _routs.put(i, new Route(route[0], Integer.parseInt(route[1])));
        }
    }

    private Integer GetIndex(CommandNode command) {
        TKey key = ((CommandKeyNode<TKey>)command).Key;
        return key == null ? 0 : key.hashCode() % _routs.size();
    }

    public Route GetRoute(CommandNode command) {
        return _routs.get(GetIndex(command));
    }

    public void AddServer(Route route){}

    public void RemoveServer(Route route){}

    public void Heartbeat(){}


}
