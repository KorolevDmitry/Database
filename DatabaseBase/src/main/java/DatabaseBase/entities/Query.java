package DatabaseBase.entities;

import DatabaseClient.parser.commands.CommandNode;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Query implements Serializable {
    public CommandNode Command;

    @Override
    public String toString() {
        return Command.toString();
    }
}
