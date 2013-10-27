package DatabaseBase.components;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Route;
import DatabaseBase.entities.ServerRole;
import DatabaseBase.entities.StringSizable;
import DatabaseBase.exceptions.BalancerException;
import DatabaseBase.mocks.TcpSenderMock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 7:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class DynamicBalancerTest {
    TcpSenderMock<StringSizable, ServiceResult> _senderMock;
    DynamicBalancer _balancer;

    @Before
    public void setUp() {
        _senderMock = new TcpSenderMock<StringSizable, ServiceResult>();
        _balancer = new DynamicBalancer(_senderMock, 0);
    }

    @Test
    public void GetRoute_NullCommand_IllegalArgumentException() throws BalancerException {
        //arrange
        boolean hasException = false;

        //act
        try {
            Route result = _balancer.GetRoute(null, null);
        } catch (IllegalArgumentException e) {
            hasException = true;
        }

        //assert
        assertTrue(hasException);
    }

    @Test
    public void GetRoute_NoRoutes_NullReturned() throws BalancerException {
        //arrange

        //act
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, null), null);

        //assert
        assertNull(result);
    }

    @Test
    public void GetRoute_OneMasterWorkingRoute_RouteReturnedPingSent() throws BalancerException {
        //arrange
        Route route = AddMasterRoute(1111, true, true);
        _senderMock.AddExpectedBehavior(route, RequestCommand.PING, GetPingExpectedResult());

        //act
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, null), null);

        //assert
        assertEquals(route, result);
        assertEquals(1, _senderMock.SequenceOfSentCommands.size());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());

    }

    @Test
    public void GetRoute_OneMasterNotAliveRoute_NullReturned() throws BalancerException {
        //arrange
        Route route = AddMasterRoute(1111, false, true);
        _senderMock.AddExpectedBehavior(route, RequestCommand.PING, GetPingExpectedResult());

        //act
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, null), null);

        //assert
        assertNull(result);
        assertEquals(0, _senderMock.SequenceOfSentCommands.size());

    }

    @Test
    public void GetRoute_OneMasterNotReadyRoute_NullReturned() throws BalancerException {
        //arrange
        Route route = AddMasterRoute(1111, true, false);
        _senderMock.AddExpectedBehavior(route, RequestCommand.PING, GetPingExpectedResult());

        //act
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, null), null);

        //assert
        assertNull(result);
        assertEquals(0, _senderMock.SequenceOfSentCommands.size());

    }

    @Test
    public void GetRouteGetCommand_MasterAndSlaveWorkingRoute_SlaveRouteReturnedPingSent() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111, true, true);
        Route slave = AddSlaveRoute(2222, true, true, master);
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(slave, RequestCommand.PING, GetPingExpectedResult());

        //act
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, null), null);

        //assert
        assertEquals(slave, result);
        assertEquals(1, _senderMock.SequenceOfSentCommands.size());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
    }

    @Test
    public void GetRouteAddCommand_MasterAndSlaveWorkingRoute_MasterRouteReturnedPingSent() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111, true, true);
        Route slave = AddSlaveRoute(2222, true, true, master);
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(slave, RequestCommand.PING, GetPingExpectedResult());

        //act
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, null), null);

        //assert
        assertEquals(master, result);
        assertEquals(1, _senderMock.SequenceOfSentCommands.size());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
    }

    @Test
    public void GetRouteGetCommand_MasterWorkingAndSlaveNotAliveRoute_MasterRouteReturnedPingSent() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111, true, true);
        Route slave = AddSlaveRoute(2222, false, true, master);
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(slave, RequestCommand.PING, GetPingExpectedResult());

        //act
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, null), null);

        //assert
        assertEquals(master, result);
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
    }

    @Test
    public void GetRouteAddCommand_MasterNotAliveAndSlaveWorkingRoute_NullReturned() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111, false, true);
        Route slave = AddSlaveRoute(2222, true, true, master);
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(slave, RequestCommand.PING, GetPingExpectedResult());

        //act
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, null), null);

        //assert
        assertNull(result);
        assertEquals(0, _senderMock.SequenceOfSentCommands.size());
    }

    @Test
    public void GetRouteAddCommand_MasterAliveTried_NullReturnedMasterNotAlive() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111, false, true);
        List<Route> tried = new ArrayList<Route>();
        tried.add(master);
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());

        //act
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, null), tried);

        //assert
        assertNull(result);
        assertEquals(0, _senderMock.SequenceOfSentCommands.size());
        assertFalse(master.IsAlive);
    }

    @Test
    public void AddServer_MasterNoRoutes_CanGet() throws BalancerException {
        //arrange
        Route master = new Route("", 1111);
        master.Role = ServerRole.Master;
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());

        //act
        _balancer.AddServer(master);
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, null), null);

        //assert
        assertEquals(master, result);
        assertEquals(2, _senderMock.SequenceOfSentCommands.size());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(1).GetCommand());
    }

    @Test
    public void RemoveServer_MasterNoRoutes_NoRoutes() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111,true,true);
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());

        //act
        _balancer.RemoveServer(master);
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, null), null);

        //assert
        assertNull(result);
    }

    @Test
    public void AddServer_MasterSomeRoutes_CanGetOnlyAfterReadyPing() throws BalancerException {
        //arrange
        Route master1 = AddMasterRoute(1111, true, true);
        Route master2 = new Route("", 2222);
        master2.Role = ServerRole.Master;
        _senderMock.AddExpectedBehavior(master1, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(master1, RequestCommand.REPLICATE, GetReplicateExpectedResult());
        _senderMock.AddExpectedBehavior(master2, RequestCommand.PING, GetPingExpectedResult(true, false));

        //act
        _balancer.AddServer(master2);

        //assert
        assertEquals(3, _senderMock.SequenceOfSentCommands.size());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(1).GetCommand());
        assertEquals(RequestCommand.REPLICATE, _senderMock.SequenceOfSentCommands.get(2).GetCommand());
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, master2), null);
        assertEquals(master1, result);
        assertFalse(master2.IsReady);
        _senderMock.AddExpectedBehavior(master2, RequestCommand.PING, GetPingExpectedResult());
        _balancer.Ping(master2);
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(4).GetCommand());
        result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, master2), null);
        assertEquals(master2, result);
        assertTrue(result.IsReady);
    }

    @Test
    public void RemoveServer_MasterSomeRoutes_GetOtherOnlyAfterReadyPing() throws BalancerException {
        //arrange
        Route master1 = AddMasterRoute(1111, true, true);
        Route master2 = AddMasterRoute(2222, true, true);
        _senderMock.AddExpectedBehavior(master1, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(master2, RequestCommand.REPLICATE, GetReplicateExpectedResult());
        _senderMock.AddExpectedBehavior(master2, RequestCommand.PING, GetPingExpectedResult(true, true));

        //act
        _balancer.RemoveServer(master2);

        //assert
        assertTrue(master2.IsReady);
        assertEquals(3, _senderMock.SequenceOfSentCommands.size());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(1).GetCommand());
        assertEquals(RequestCommand.REPLICATE, _senderMock.SequenceOfSentCommands.get(2).GetCommand());
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, master2), null);
        assertEquals(master2, result);
        _senderMock.AddExpectedBehavior(master2, RequestCommand.PING, GetPingExpectedResult(true, true, true));
        _balancer.Ping(master2);
        result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, master2), null);
        assertEquals(master1, result);
        result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, master2), null);
        assertEquals(master1, result);
    }

    @Test
    public void RemoveServer_MasterSomeRoutes_CanNotAddUntilPingRemoved() throws BalancerException {
        //arrange
        Route master1 = AddMasterRoute(1111, true, true);
        Route master2 = AddMasterRoute(2222, true, true);
        _senderMock.AddExpectedBehavior(master1, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(master2, RequestCommand.REPLICATE, GetReplicateExpectedResult());
        _senderMock.AddExpectedBehavior(master2, RequestCommand.PING, GetPingExpectedResult(true, true));

        //act
        _balancer.RemoveServer(master2);

        //assert
        assertTrue(master2.IsReady);
        assertEquals(3, _senderMock.SequenceOfSentCommands.size());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(1).GetCommand());
        assertEquals(RequestCommand.REPLICATE, _senderMock.SequenceOfSentCommands.get(2).GetCommand());
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, master2), null);
        assertNull(result);
        _senderMock.AddExpectedBehavior(master2, RequestCommand.PING, GetPingExpectedResult(true, true, true));
        _balancer.Ping(master2);
        result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, master2), null);
        assertEquals(master1, result);
    }

    @Test
    public void AddServer_SlaveSomeRoutes_CanGetSlaveOnlyAfterReadyPing() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111, true, true);
        Route slave = new Route("", 2222);
        slave.Role = ServerRole.Slave;
        slave.Master = master;
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(master, RequestCommand.REPLICATE, GetReplicateExpectedResult());
        _senderMock.AddExpectedBehavior(slave, RequestCommand.PING, GetPingExpectedResult(true, false));

        //act
        _balancer.AddServer(slave);

        //assert
        assertEquals(3, _senderMock.SequenceOfSentCommands.size());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(1).GetCommand());
        assertEquals(RequestCommand.REPLICATE, _senderMock.SequenceOfSentCommands.get(2).GetCommand());
        assertFalse(slave.IsReady);
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, slave), null);
        assertEquals(master, result);
        _senderMock.AddExpectedBehavior(slave, RequestCommand.PING, GetPingExpectedResult());
        _balancer.Ping(slave);
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(4).GetCommand());
        result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, slave), null);
        assertEquals(slave, result);
    }

    @Test
    public void RemoveServer_SlaveSomeRoutes_CanGetMaster() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111, true, true);
        Route slave = AddSlaveRoute(2222, true, true, master);
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(slave, RequestCommand.PING, GetPingExpectedResult(true, false));

        //act
        _balancer.RemoveServer(slave);
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.GET, slave), null);

        //assert
        assertEquals(master, result);
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(1).GetCommand());
        assertFalse(slave.IsReady);
    }

    @Test
    public void Ping_SomeRoutes_PingCalled() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111, true, true);
        Route slave = AddSlaveRoute(2222, true, true, master);
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(slave, RequestCommand.PING, GetPingExpectedResult(true, false));

        //act
        _balancer.Ping(master);
        _balancer.Ping(slave);

        //assert
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(1).GetCommand());
    }

    @Test
    public void Ping_MasterPingReturnedWithNotAlive_CanNotGet() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111, true, true);
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult(false, true));

        //act
        _balancer.Ping(master);

        //assert
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, null), null);
        assertNull(result);
    }

    @Test
    public void Ping_SlavePingReturnedWithNotAlive_GetMaster() throws BalancerException {
        //arrange
        Route master = AddMasterRoute(1111, true, true);
        Route slave = AddSlaveRoute(2222, true, true, master);
        _senderMock.AddExpectedBehavior(master, RequestCommand.PING, GetPingExpectedResult());
        _senderMock.AddExpectedBehavior(slave, RequestCommand.PING, GetPingExpectedResult(false, true));

        //act
        _balancer.Ping(slave);

        //assert
        assertEquals(RequestCommand.PING, _senderMock.SequenceOfSentCommands.get(0).GetCommand());
        Route result = _balancer.GetRoute(new CommandKeyNode(RequestCommand.ADD, null), null);
        assertEquals(master, result);
    }




    private Route AddMasterRoute(int port, boolean isAlive, boolean isReady) {
        Route route = new Route("", port);
        route.Role = ServerRole.Master;
        route.IsAlive = isAlive;
        route.IsReady = isReady;
        _balancer._clientToServerRouteMap.put(route, route);
        _balancer._routes.add(route);

        return route;
    }

    private Route AddSlaveRoute(int port, boolean isAlive, boolean isReady, Route master) {
        Route route = new Route("", port);
        route.Role = ServerRole.Slave;
        route.IsAlive = isAlive;
        route.IsReady = isReady;
        route.Master = master;
        master.Slaves.add(route);
        _balancer._clientToServerRouteMap.put(route, route);

        return route;
    }

    private EvaluationResult<StringSizable, ServiceResult> GetPingExpectedResult()
    {
        return GetPingExpectedResult(true, true);
    }

    private EvaluationResult<StringSizable, ServiceResult> GetReplicateExpectedResult()
    {
        return GetPingExpectedResult(true, true);
    }

    private EvaluationResult<StringSizable, ServiceResult> GetPingExpectedResult(boolean isAlive, boolean isReady)
    {
        return GetPingExpectedResult(isAlive, isReady, false);
    }

    private EvaluationResult<StringSizable, ServiceResult> GetPingExpectedResult(boolean isAlive, boolean isReady, boolean readyToBeRemoved)
    {
        EvaluationResult<StringSizable, ServiceResult> result = new EvaluationResult<StringSizable, ServiceResult>();
        result.HasReturnResult = true;
        result.Result = new ServiceResult();
        result.Result.IsAlive = isAlive;
        result.Result.IsReady = isReady;
        result.Result.ReadyToBeRemoved = readyToBeRemoved;
        return result;
    }
}
