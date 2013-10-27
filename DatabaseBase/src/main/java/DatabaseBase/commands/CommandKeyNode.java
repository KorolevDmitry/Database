package DatabaseBase.commands;

import DatabaseBase.interfaces.ISizable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandKeyNode<TKey extends ISizable> extends CommandSingleNode {
    public TKey Key;

    public CommandKeyNode(RequestCommand requestCommand, TKey key) {
        super(requestCommand);
        Key = key;
    }

    @Override
    public String toString() {
        return super.toString() + SEPARATOR + Key;
    }
}
