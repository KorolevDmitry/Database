package DatabaseBase.commands.service;

import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.entities.Route;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceCommand extends CommandSingleNode {
    public Route Route;

    public ServiceCommand(RequestCommand requestCommand, Route route) {
        super(requestCommand);
        Route = route;
    }
}
