package DatabaseBase.commands.service;

import DatabaseBase.commands.RequestCommand;
import DatabaseBase.entities.Route;

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
    public Boolean RemoveFromCluster;

    public ReplicateCommand(Route fromRoute, Route toRoute, Integer startIndex, Boolean removeFromCluster) {
        super(RequestCommand.REPLICATE, fromRoute);
        ToRoute = toRoute;
        StartIndex = startIndex;
        RemoveFromCluster = removeFromCluster;
    }
}
