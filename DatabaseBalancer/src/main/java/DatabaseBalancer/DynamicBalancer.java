package DatabaseBalancer;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.commands.service.ReplicateCommand;
import DatabaseBase.commands.service.ServiceCommand;
import DatabaseBase.components.ServiceResult;
import DatabaseBase.components.TcpSender;
import DatabaseBase.entities.*;
import DatabaseBase.exceptions.BalancerException;
import DatabaseBase.exceptions.ConnectionException;
import DatabaseBase.interfaces.IBalancer;
import DatabaseBase.utils.ConsistentHash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 5:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class DynamicBalancer implements IBalancer {
    ConsistentHash<Route> _routes;
    ConcurrentHashMap<Route, Route> _clientToServerRouteMap;
    //TcpListener<TKey, TValue> _routesListener;
    TcpSender<StringSizable, ServiceResult> _sender;

    public DynamicBalancer(TcpSender<StringSizable, ServiceResult> sender, int heartbeatTimeout) {
        _routes = new ConsistentHash<Route>(new HashFunction(), new ArrayList<Route>());
        _clientToServerRouteMap = new ConcurrentHashMap<Route, Route>();
        _sender = sender;
        //TODO: start heartbeat timer
    }

    public Route GetRoute(CommandKeyNode command, List<Route> triedRoutes) throws BalancerException {
        if (command == null)
            throw new IllegalArgumentException("Command can not be null");
        if (triedRoutes == null) {
            triedRoutes = new ArrayList<Route>();
        }
        Route firstRoute;
        Route route = firstRoute = _routes.get(command.Key);
        if (route == null)
            return null;
        while (!route.IsReady) {
            route = _routes.getNext(route);
            if (route.equals(firstRoute)) {
                route = null;
                break;
            }
        }
        return GetRouteFromMaster(route, command, triedRoutes);
    }

    public void AddServer(Route clientRoute) throws BalancerException {
        if (clientRoute == null)
            throw new IllegalArgumentException("Route can not be null");
        if (_clientToServerRouteMap.containsKey(clientRoute))
            throw new BalancerException("Route " + clientRoute + " is already exists");
        Route route = new Route(clientRoute.Host, clientRoute.Port);
        route.Role = clientRoute.Role;
        _clientToServerRouteMap.put(clientRoute, route);
        if (!Ping(route)) {
            _clientToServerRouteMap.remove(clientRoute);
            throw new BalancerException("Route " + clientRoute + " is not responded");
        }
        if (clientRoute.Role == ServerRole.Slave) {
            if (clientRoute.Master == null)
                throw new BalancerException("Master route for slave can not be null");
            if (!_clientToServerRouteMap.containsKey(clientRoute.Master))
                throw new BalancerException("Unknown master route: " + clientRoute.Master);
            Route master = _clientToServerRouteMap.get(clientRoute.Master);
            route.Master = master;
            route.Index = master.Index;
            master.Slaves.add(route);
        } else {
            route.Index = _routes.getIndex(route);
            _routes.add(route, route.Index);
        }
        if(!ReplicateTo(route))
        {
            FailedStartReplication(route);
        }
    }

    public void RemoveServer(Route clientRoute) throws BalancerException {
        Route route = GetInternalRoute(clientRoute);
        if (route.Role == ServerRole.Slave) {
            _clientToServerRouteMap.remove(route);
            route.Master.Slaves.remove(route);
        } else {
            route.Role = ServerRole.Slave;
            Route firstSlave = GetFirstSlave(route, null);
            if (firstSlave != null) {
                firstSlave.Master = null;
                route.Slaves.remove(firstSlave);
                firstSlave.Slaves = route.Slaves;
                UpdateServerRole(route);
                UpdateServerRole(firstSlave);
            } else if (route.Slaves.size() != 0) {
                //means that maybe some slaves are not ready or not available yet
                //TODO: implement
            } else {
                //the only master without slaves
                if (!ReplicateFrom(route))
                    throw new BalancerException("Failed start replication from: " + route);
            }
        }
    }

    public void UpdateServer(Route clientRoute) throws BalancerException {
        Route route = GetInternalRoute(clientRoute);
        UpdateServerRole(route);
    }

    public void Replicate(Route clientRouteFrom, Route clientRouteTo, int fromId, boolean removeFromCluster) throws BalancerException {
        Route from = GetInternalRoute(clientRouteFrom);
        Route to = GetInternalRoute(clientRouteTo);
        ReplicateInternal(from, to, fromId, removeFromCluster);
    }

    public boolean Ping(Route clientRoute) {
        if (!_clientToServerRouteMap.containsKey(clientRoute))
            return false;
        Route route = _clientToServerRouteMap.get(clientRoute);
        EvaluationResult<StringSizable, ServiceResult> serverAnswer;
        try {
            serverAnswer = _sender.Send(new ServiceCommand(RequestCommand.PING, route), route);
        } catch (ConnectionException e) {
            route.IsAlive = false;
            return route.IsAlive;
        }
        if (serverAnswer.HasError || !serverAnswer.HasReturnResult) {
            route.IsAlive = false;
            return route.IsAlive;
        }

        route.IsAlive = serverAnswer.Result.IsAlive;
        route.IsReady = serverAnswer.Result.IsReady;

        if(serverAnswer.Result.ReadyToBeRemoved){
            _routes.remove(route);
            _clientToServerRouteMap.remove(route);
            return false;
        }

        return route.IsAlive;
    }

    private void FailedStartReplication(Route addedRoute) throws BalancerException {
        if(addedRoute.Role == ServerRole.Slave){
            addedRoute.Master.Slaves.remove(addedRoute);
            _clientToServerRouteMap.remove(addedRoute);
        }
        else
        {
            _clientToServerRouteMap.remove(addedRoute);
            _routes.remove(addedRoute);
        }
        throw new BalancerException("Failed start replication to: " + addedRoute);
    }

    private void Heartbeat() {
        Iterator<Route> iterator = _clientToServerRouteMap.values().iterator();
        while (iterator.hasNext()) {
            Route current = iterator.next();
            Ping(current);
        }
    }

    private Route GetFirstSlave(Route route, List<Route> triedRoutes) {
        if (route.Slaves.size() == 0)
            return null;
        Iterator<Route> iterator = route.Slaves.iterator();
        while (iterator.hasNext()) {
            Route current = iterator.next();
            if ((triedRoutes == null || !triedRoutes.contains(current)) && current.IsAlive && current.IsReady && Ping(current))
                return current;
        }

        return null;
    }

    private void ReplicateInternal(Route from, Route to, int fromId, boolean removeFromCluster) throws BalancerException {
        try {
            _sender.Send(new ReplicateCommand(from, to, fromId, removeFromCluster), from);
        } catch (ConnectionException e) {
            throw new BalancerException(e.getMessage(), e);
        }
    }

    private boolean ReplicateTo(Route addedRoute) throws BalancerException {
        if (addedRoute.Role == ServerRole.Slave) {
            if(!Ping(addedRoute.Master))
                return false;
            ReplicateInternal(addedRoute.Master, addedRoute, 0, false);
        } else {
            Route from = _routes.getPrevious(addedRoute);
            if (from.equals(addedRoute))
                return true;
            if(!Ping(from))
                return false;
            ReplicateInternal(from, addedRoute, addedRoute.Index, false);
        }

        return true;
    }

    private boolean ReplicateFrom(Route removedRoute) throws BalancerException {
        Route to = _routes.getNext(removedRoute);
        if (to.equals(removedRoute))
            return true;
        if(!Ping(to))
            return false;
        //throw new BalancerException("Warning! Data can be loss");
        ReplicateInternal(removedRoute, to, 0, true);

        return true;
    }

    private void UpdateServerRole(Route route) throws BalancerException {
        try {
            _sender.Send(new ServiceCommand(RequestCommand.UPDATE_SERVER, route), route);
        } catch (ConnectionException e) {
            throw new BalancerException(e.getMessage(), e);
        }
    }

    private Route GetRouteFromMaster(Route master, CommandKeyNode command, List<Route> triedRoutes) throws BalancerException {
        if (master == null)
            return null;
        switch (command.GetCommand()) {
            case ADD:
            case ADD_OR_UPDATE:
            case DELETE:
            case UPDATE:
                if (master.Role == ServerRole.Slave || triedRoutes.contains(master) || !master.IsAlive || !Ping(master))
                    return null;
                return master;
            case GET:
                Route slave = GetFirstSlave(master, triedRoutes);
                if (slave == null) {
                    if (triedRoutes.contains(master) || !master.IsAlive || !Ping(master))
                        return null;
                    return master;
                }
                return slave;
            default:
                throw new BalancerException("Unexpected request command: " + command.GetCommand());
        }
    }

    private Route GetInternalRoute(Route clientRoute) throws BalancerException {
        if (clientRoute == null)
            throw new IllegalArgumentException("Route can not be null");
        if (!_clientToServerRouteMap.containsKey(clientRoute))
            throw new BalancerException("Route " + clientRoute + " does not belong to cluster");
        Route route = _clientToServerRouteMap.get(clientRoute);
        if (!Ping(route)) {
            throw new BalancerException("Route " + clientRoute + " is not responded");
        }

        return route;
    }
}
