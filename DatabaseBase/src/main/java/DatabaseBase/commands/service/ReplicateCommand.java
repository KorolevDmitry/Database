package DatabaseBase.commands.service;

import DatabaseBase.commands.RequestCommand;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.Route;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 7:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReplicateCommand extends ServiceCommand {
    public Route ToRoute;
    public Integer StartIndex;
    public Integer EndIndex;
    public Boolean RemoveFromCluster;
    //if not null queries will be executed on current server
    public List<Query> Queries;
    public boolean IsLast;

    public ReplicateCommand(Route fromRoute, Route toRoute, Integer startIndex, Integer endIndex, Boolean removeFromCluster) {
        super(RequestCommand.REPLICATE, fromRoute);
        ToRoute = toRoute;
        StartIndex = startIndex;
        EndIndex = endIndex;
        RemoveFromCluster = removeFromCluster;
    }
}
