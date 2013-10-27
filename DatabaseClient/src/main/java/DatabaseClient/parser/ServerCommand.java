package DatabaseClient.parser;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.interfaces.ISizable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 8:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerCommand<TKey extends ISizable> extends CommandKeyNode<TKey> {
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
