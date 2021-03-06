package DatabaseBase.entities;

import DatabaseBase.interfaces.ISizable;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceResult implements ISizable {
    public boolean ReadyToBeRemoved;
    public Integer Index;
    public List<Route> Servers;
    //multi and single queries
    public List<Route> Routes;
    //ping
    public Route PingRoute;

    @Override
    public long GetSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean equals(Object obj) {
        //TODO: Implement!!!
        if (obj == null)
            return false;
        if (!(obj instanceof ServiceResult))
            return false;
        ServiceResult temp = (ServiceResult) obj;
        if(ReadyToBeRemoved != temp.ReadyToBeRemoved)
            return false;
        return true;
    }

    @Override
    public int compareTo(Object obj) {
        //TODO: Implement!!!
        return equals(obj) ? 0 : -1;
    }
}
