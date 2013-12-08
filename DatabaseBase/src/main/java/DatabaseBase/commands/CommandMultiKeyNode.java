package DatabaseBase.commands;

import DatabaseBase.interfaces.ISizable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 12/8/13
 * Time: 8:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandMultiKeyNode<TKey extends ISizable> extends CommandNode {
    public TKey StartKey;
    public TKey EndKey;

    public CommandMultiKeyNode(RequestCommand requestCommand, TKey startKey, TKey endKey) {
        super(requestCommand);
        StartKey = startKey;
        EndKey = endKey;
    }

    @Override
    public String toString() {
        return super.toString() + SEPARATOR + StartKey + SEPARATOR + EndKey;
    }
}
