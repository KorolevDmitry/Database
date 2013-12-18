package DatabaseBalancer;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandMultiKeyNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.commands.service.ReplicateCommand;
import DatabaseBase.commands.service.ServiceCommand;
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
public class DynamicBalancer implements IBalancer, Runnable {
    ConsistentHash<Route> _routes;
    ConcurrentHashMap<Route, Route> _clientToServerRouteMap;
    List<Route> _serversInReplicationState;
    //TcpListener<TKey, TValue> _routesListener;
    TcpSender<StringSizable, StringSizable> _sender;
    int _heartbeatTimeout;
    Thread _heartBeatThread;
    private boolean _stopRequest;
    private boolean _isWorking;

    public DynamicBalancer(TcpSender<StringSizable, StringSizable> sender, int heartbeatTimeout) {
        _routes = new ConsistentHash<Route>(new HashFunction(), new ArrayList<Route>());
        _serversInReplicationState = new ArrayList<Route>();
        _clientToServerRouteMap = new ConcurrentHashMap<Route, Route>();
        _sender = sender;
        _heartbeatTimeout = heartbeatTimeout;
        if (_heartbeatTimeout > 0) {
            _heartBeatThread = new Thread(this);
            _heartBeatThread.start();
        }
    }

    public int GetIndex(Object key) {
        return _routes.getIndex(key);
    }

    public List<Route> GetMultiKeyRoutes(CommandMultiKeyNode command, List<Route> triedRoutes) throws BalancerException {
        CommandKeyNode startKeyCommand = new CommandKeyNode(command.GetCommand(), command.StartKey);
        CommandKeyNode endKeyCommand = new CommandKeyNode(command.GetCommand(), command.EndKey);
        Route startRoute = GetMasterRoute(startKeyCommand, triedRoutes);
        Route endRoute = GetMasterRoute(endKeyCommand, triedRoutes);
        if (startRoute == null || endRoute == null)
            return null;
        if (startRoute.equals(endRoute)) {
            int startIndex = _routes.getIndex(command.StartKey);
            int endIndex = _routes.getIndex(command.EndKey);
            if (startIndex > endIndex)
                startRoute = _routes.getNext(startRoute);
        }
        List<Route> routes = new ArrayList<Route>();
        routes.add(startRoute);
        while (!startRoute.equals(endRoute)) {
            startRoute = _routes.getNext(startRoute);
            if (startRoute == null)
                throw new BalancerException("Unexpected routes behavior. Try again later.");
            routes.add(startRoute);
        }

        List<Route> routesFromMaster = new ArrayList<Route>(routes.size());
        for(Route route : routes){
            routesFromMaster.add(GetRouteFromMaster(route, startKeyCommand, triedRoutes));
        }

        return routes;
    }

    public Route GetRoute(CommandKeyNode command, List<Route> triedRoutes) throws BalancerException {
        if (command == null)
            throw new IllegalArgumentException("Command can not be null");
        if (triedRoutes == null) {
            triedRoutes = new ArrayList<Route>();
        }

        Route route = GetMasterRoute(command, triedRoutes);
        return GetRouteFromMaster(route, command, triedRoutes);
    }

    private Route GetMasterRoute(CommandKeyNode command, List<Route> triedRoutes) throws BalancerException {
        Route route = _routes.get(command.Key);
        if (route == null)
            return null;
        //if master is busy - slaves can not have actual info
        if (!route.IsReady)
            route = null;
        /*while (!route.IsReady) {
            route = _routes.getNext(route);
            if (route.equals(firstRoute)) {
                route = null;
                break;
            }
        }*/
        return route;
    }

    public void AddServer(Route clientRoute) throws BalancerException {
        if (clientRoute == null)
            throw new IllegalArgumentException("Route can not be null");
        if (_clientToServerRouteMap.containsKey(clientRoute))
            throw new BalancerException("Route " + clientRoute + " is already exists");
        Route route = new Route(clientRoute.Host, clientRoute.Port);
        route.IsReady = false;
        route.Role = clientRoute.Role;
        _clientToServerRouteMap.put(clientRoute, route);
        if (!Ping(route)) {
            _clientToServerRouteMap.remove(clientRoute);
            throw new BalancerException("Route " + clientRoute + " is not responded");
        }
        if (clientRoute.Role == ServerRole.SLAVE) {
            if (clientRoute.Master == null)
                throw new BalancerException("Master route for slave can not be null");
            if (!_clientToServerRouteMap.containsKey(clientRoute.Master))
                throw new BalancerException("Unknown master route: " + clientRoute.Master);
            Route master = _clientToServerRouteMap.get(clientRoute.Master);
            if (!Ping(master)) {
                FailedStartReplication(route);
            }
            route.Master = master;
            route.setStartIndex(master.getStartIndex());
            route.setEndIndex(master.getEndIndex());
            master.Slaves.add(route);
            UpdateServerRole(route.Master);
        } else {
            route.StartIndexPending = _routes.getIndex(route);
            route.EndIndexPending = route.StartIndexPending;

            Route oldRoute = _routes.getPrevious(route);
            //_routes.add(route, route.StartIndex);
            //update end index
            if (oldRoute != null) {
                if (!Ping(oldRoute)) {
                    FailedStartReplication(route);
                }
                route.EndIndexPending = oldRoute.getStartIndex();
                oldRoute.EndIndexPending = route.StartIndexPending;
                UpdateServerRole(oldRoute);
            }
        }
        UpdateServerRole(route);
        if (!ReplicateTo(route)) {
            FailedStartReplication(route);
        }
    }

    public void RemoveServer(Route clientRoute) throws BalancerException {
        Route route = GetInternalRoute(clientRoute);
        if (route.Role == ServerRole.SLAVE) {
            _clientToServerRouteMap.remove(route);
            route.Master.Slaves.remove(route);
        } else {
            route.Role = ServerRole.SLAVE;
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

    public void Replicate(Route clientRouteFrom, Route clientRouteTo, int fromId, int endId, boolean removeFromCluster) throws BalancerException {
        Route from = GetInternalRoute(clientRouteFrom);
        Route to = GetInternalRoute(clientRouteTo);
        if (fromId == from.getStartIndex()) {
            if (endId > from.getEndIndex())
                throw new BalancerException("Range of indexes should be on one server");
            if (!_routes.getPrevious(from).equals(to))
                throw new BalancerException("Servers should be close in ring");
            to.StartIndexPending = null;
            to.EndIndexPending = endId;
            from.StartIndexPending = endId;
            from.EndIndexPending = null;
        } else if (endId == from.getEndIndex()) {
            if (fromId < from.getStartIndex())
                throw new BalancerException("Range of indexes should be on one server");
            if (!_routes.getNext(from).equals(to))
                throw new BalancerException("Servers should be close in ring");
            to.StartIndexPending = fromId;
            to.EndIndexPending = null;
            from.StartIndexPending = null;
            from.EndIndexPending = fromId;
        } else {
            throw new BalancerException("Range of indexes should be start or end of FromServer");
        }
        ReplicateInternal(from, to, fromId, endId, removeFromCluster);
    }

    public boolean Ping(Route clientRoute) throws BalancerException {
        if (!_clientToServerRouteMap.containsKey(clientRoute))
            return false;
        Route route = _clientToServerRouteMap.get(clientRoute);
        EvaluationResult<StringSizable, StringSizable> serverAnswer;
        try {
            Query query = new Query();
            query.Command = new ServiceCommand(RequestCommand.PING, route);
            serverAnswer = _sender.Send(query, route, 1000);
        } catch (ConnectionException e) {
            route.IsAlive = false;
            return route.IsAlive;
        }
        if (serverAnswer.HasError || !serverAnswer.HasReturnResult) {
            route.IsAlive = false;
            return route.IsAlive;
        }

        route.IsAlive = serverAnswer.ServiceResult.PingRoute.IsAlive;
        route.IsReady = serverAnswer.ServiceResult.PingRoute.IsReady;

        if (_serversInReplicationState.contains(route) && route.IsReady && route.Role == ServerRole.MASTER) {
            _serversInReplicationState.remove(route);
            if (route.StartIndexPending != null || route.EndIndexPending != null) {
                if (route.StartIndexPending != null) {
                    route.setStartIndex(route.StartIndexPending);
                    _routes.remove(route);
                    _routes.add(route, route.getStartIndex());
                }
                if (route.EndIndexPending != null)
                    route.setEndIndex(route.EndIndexPending);
                route.StartIndexPending = null;
                route.EndIndexPending = null;
                UpdateServerRole(route);
            }
            if (!_routes.contains(route)) {
                _routes.add(route, route.getStartIndex());
            }
            Route previous = _routes.getPrevious(route);
            if (previous.StartIndexPending != null || previous.EndIndexPending != null) {
                if (previous.StartIndexPending != null) {
                    previous.setStartIndex(previous.StartIndexPending);
                    _routes.remove(previous);
                    _routes.add(previous, previous.getStartIndex());
                }
                if (previous.EndIndexPending != null)
                    previous.setEndIndex(previous.EndIndexPending);
                previous.StartIndexPending = null;
                previous.EndIndexPending = null;
                UpdateServerRole(previous);
            }
        }

        if (serverAnswer.ServiceResult.ReadyToBeRemoved) {
            Route previous = _routes.getPrevious(route);
            previous.setEndIndex(route.getEndIndex());
            _routes.remove(route);
            _clientToServerRouteMap.remove(route);
            UpdateServerRole(previous);
            return false;
        }

        return route.IsAlive;
    }

    public List<Route> GetServersList() {
        return _routes.getListOfValues();
    }

    private void FailedStartReplication(Route addedRoute) throws BalancerException {
        if (addedRoute.Role == ServerRole.SLAVE) {
            addedRoute.Master.Slaves.remove(addedRoute);
            _clientToServerRouteMap.remove(addedRoute);
        } else {
            _clientToServerRouteMap.remove(addedRoute);
            _routes.remove(addedRoute);
        }
        throw new BalancerException("Failed start replication to: " + addedRoute);
    }

    private void Heartbeat() throws BalancerException {
        Iterator<Route> iterator = _clientToServerRouteMap.values().iterator();
        while (iterator.hasNext()) {
            Route current = iterator.next();
            Ping(current);
        }
    }

    private Route GetFirstSlave(Route route, List<Route> triedRoutes) throws BalancerException {
        if (route.Slaves.size() == 0)
            return null;
        Iterator<Route> iterator = route.Slaves.iterator();
        while (iterator.hasNext()) {
            Route current = iterator.next();
            if ((triedRoutes == null || !triedRoutes.contains(current)) && Ping(current) && current.IsAlive && current.IsReady)
                return current;
        }

        return null;
    }

    private void ReplicateInternal(Route from, Route to, int fromId, int endId, boolean removeFromCluster) throws BalancerException {
        try {
            to.IsReady = false;
            Query query = new Query();
            query.Command = new ReplicateCommand(from, to, fromId, endId, removeFromCluster);
            _serversInReplicationState.add(to);
            _sender.Send(query, from);
        } catch (ConnectionException e) {
            throw new BalancerException(e.getMessage(), e);
        }
    }

    private boolean ReplicateTo(Route addedRoute) throws BalancerException {
        if (addedRoute.Role == ServerRole.SLAVE) {
            ReplicateInternal(addedRoute.Master, addedRoute, 0, 0, false);
        } else {
            Route from = _routes.getPrevious(addedRoute);
            if (from == null)
                from = addedRoute;
            ReplicateInternal(from, addedRoute, addedRoute.StartIndexPending, addedRoute.EndIndexPending, false);
        }

        return true;
    }

    private boolean ReplicateFrom(Route removedRoute) throws BalancerException {
        Route to = _routes.getNext(removedRoute);
        if (to.equals(removedRoute))
            return true;
        if (!Ping(to))
            return false;
        //throw new BalancerException("Warning! Data can be loss");
        ReplicateInternal(removedRoute, to, 0, 0, true);

        return true;
    }

    private void UpdateServerRole(Route route) throws BalancerException {
        try {
            Query query = new Query();
            query.Command = new ServiceCommand(RequestCommand.UPDATE_SERVER, route);
            _sender.Send(query, route);
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
                if (master.Role == ServerRole.SLAVE || triedRoutes.contains(master) || !master.IsAlive || !Ping(master))
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

    @Override
    public void run() {
        _isWorking = true;
        while (!_stopRequest) {
            try {
                Heartbeat();
                Thread.sleep(_heartbeatTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                break;
            } catch (BalancerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        _isWorking = false;
    }

    public void Close() {
        _stopRequest = true;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } while (_isWorking);
        //TODO: store balancer data
    }
}
