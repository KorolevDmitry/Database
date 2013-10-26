package DatabaseBase.interfaces;

import DatabaseClient.parser.commands.RequestCommand;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 7:09 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ICommand {
    RequestCommand GetCommand();
}
