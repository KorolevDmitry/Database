package DatabaseClient.parser;

import DatabaseClient.parser.commands.CommandKeyNode;
import DatabaseClient.parser.commands.RequestCommand;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 8:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerCommand<TKey> extends CommandKeyNode<TKey> {
    private String _wholeRequest;

    public ServerCommand(RequestCommand requestCommand, TKey key, String wholeRequest) {
        super(requestCommand, key);
        _wholeRequest = wholeRequest;
    }

    @Override
    public String toString() {
        return _wholeRequest;
    }
}
