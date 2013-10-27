package DatabaseBase.interfaces;

import DatabaseBase.commands.CommandKeyNode;
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
    Route GetRoute(CommandKeyNode command, List<Route> triedRoutes) throws BalancerException;
    void AddServer(Route route) throws BalancerException;
    void RemoveServer(Route route) throws BalancerException;
    boolean Ping(Route clientRoute);
}
