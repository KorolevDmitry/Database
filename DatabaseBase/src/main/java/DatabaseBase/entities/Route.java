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
    public Integer Index;

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
        this(host, port, ServerRole.Master, null);
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
        return Host + ":" + Port;
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
}
