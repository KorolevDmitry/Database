package DatabaseClient.parser.commands;

import DatabaseBase.interfaces.ICommand;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CommandNode implements ICommand, Serializable {
    protected final String SEPARATOR = " ";
    private RequestCommand _command;

    public CommandNode(RequestCommand requestCommand) {
        _command = requestCommand;
    }

    @Override
    public String toString() {
        return GetCommand().toString();
    }

    public RequestCommand GetCommand() {
        return _command;
    }
}
