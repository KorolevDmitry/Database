package parser.nodes;

import interfaces.ICommand;
import interfaces.IEvaluator;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CommandNode<TKey, TValue> implements ICommand<TKey>, IEvaluator<TKey, TValue>{
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
