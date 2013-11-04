package DatabaseBase.components;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandNode;
import DatabaseBase.entities.Route;
import DatabaseBase.exceptions.BalancerException;
import DatabaseBase.interfaces.IBalancer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/12/13
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class StaticBalancer implements IBalancer {

    ConcurrentHashMap<Integer, Route> _routs;

    public StaticBalancer(String serverStr) {
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
        Object key = ((CommandKeyNode)command).Key;
        return key == null ? 0 : key.hashCode() % _routs.size();
    }

    @Override
    public int GetIndex(Object key) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Route GetRoute(CommandKeyNode command, List<Route> triedRoutes) {
        return _routs.get(GetIndex(command));
    }

    public void AddServer(Route route){}

    public void RemoveServer(Route route){}

    @Override
    public void UpdateServer(Route route) throws BalancerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void Replicate(Route from, Route to, int fromId, int endId, boolean removeFromCluster) throws BalancerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean Ping(Route clientRoute) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Route> GetServersList() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void Close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
