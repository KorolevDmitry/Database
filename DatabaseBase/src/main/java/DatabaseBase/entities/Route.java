package DatabaseBase.entities;

import DatabaseBase.interfaces.ISizable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/12/13
 * Time: 12:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Route implements ISizable {
    public String Host;
    public int Port;
    public ServerRole Role;
    public List<Route> Slaves;
    public Route Master;
    public boolean IsAlive;
    public boolean IsReady;
    public Integer StartIndexPending;
    public Integer EndIndexPending;
    private Integer _startIndex;
    private Integer _endIndex;

    public Route(String serverHost, ServerRole role, Route master) {
        if (serverHost == null) {
            throw new IllegalArgumentException("Route can not be null");
        }
        String[] route = serverHost.split(":");
        if (route.length != 2) {
            throw new IllegalArgumentException("Route should looks like: {host:pot} instead of: " + serverHost);
        }
        try {
            Host = route[0];
            Port = Integer.parseInt(route[1]);
            Role = role;
            Master = master;
            Slaves = new ArrayList<Route>();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Route should looks like: {host:pot} instead of: " + serverHost);
        }
    }

    public Route(String host, int port) {
        this(host, port, ServerRole.MASTER, null);
    }

    public Route(String host, int port, ServerRole role, Route master) {
        if (host == null)
            throw new IllegalArgumentException("Host can not be null");
        Host = host;
        Port = port;
        Role = role;
        Master = master;
        Slaves = new ArrayList<Route>();
    }

    @Override
    public String toString() {
        return Host + ":" + Port + (IsAlive ? " alive" : " not available")
                + (IsReady ? " ready" : " busy") + " StartIndex:" + _startIndex + " EndIndex:" + _endIndex
                + (Master == null ? "" : (" Master:" + Master.Host + ":" + Master.Port));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Route))
            return false;
        Route route = (Route) obj;
        return Host.equals(route.Host) && Port == route.Port;
    }

    @Override
    public int hashCode() {
        return (Host + Port).hashCode();
    }

    @Override
    public long GetSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getStartIndex() {
        return _startIndex;
    }

    public void setStartIndex(Integer _startIndex) {
        this._startIndex = _startIndex;
    }

    public Integer getEndIndex() {
        return _endIndex;
    }

    public void setEndIndex(Integer _endIndex) {
        this._endIndex = _endIndex;
    }

    public static Route Clone(Route route)
    {
        if(route == null)
            return null;
        Route clone = new Route(route.Host, route.Port);
        clone.Role = route.Role;
        //for(int i=0;i<route.Slaves.size();i++)
        //    clone.Slaves.add(Route.Clone(route.Slaves.get(i)));
        clone.Master = Route.Clone(route.Master);
        clone.IsAlive = route.IsAlive;
        clone.IsReady = route.IsReady;
        clone.StartIndexPending = route.StartIndexPending;
        clone.EndIndexPending = route.EndIndexPending;
        clone.setEndIndex(route.getEndIndex());
        clone.setStartIndex(route.getStartIndex());

        return clone;
    }
}
