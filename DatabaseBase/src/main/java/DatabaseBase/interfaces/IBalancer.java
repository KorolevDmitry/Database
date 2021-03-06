package DatabaseBase.interfaces;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandMultiKeyNode;
import DatabaseBase.entities.Route;
import DatabaseBase.exceptions.BalancerException;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 5:33 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IBalancer {
    int GetIndex(Object key);
    List<Route> GetMultiKeyRoutes(CommandMultiKeyNode command, List<Route> triedRoutes) throws BalancerException;
    Route GetRoute(CommandKeyNode command, List<Route> triedRoutes) throws BalancerException;
    void AddServer(Route route) throws BalancerException;
    void RemoveServer(Route route) throws BalancerException;
    void UpdateServer(Route route) throws BalancerException;
    void Replicate(Route from, Route to, int fromId, int endId, boolean removeFromCluster) throws BalancerException;
    boolean Ping(Route clientRoute) throws BalancerException;
    List<Route> GetServersList();
    void Close();
}
