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
    public Route Route;

    @Override
    public long GetSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
