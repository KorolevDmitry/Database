package DatabaseBase.commands;

import DatabaseBase.interfaces.ISizable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 12/8/13
 * Time: 10:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandMultiKeyValueNode<TKey extends ISizable, TValue extends ISizable> extends CommandMultiKeyNode<TKey> {
    public TValue Value;

    public CommandMultiKeyValueNode(RequestCommand requestCommand, TKey startKey, TKey endKey, TValue value) {
        super(requestCommand, startKey, endKey);
        Value = value;
    }

    @Override
    public String toString() {
        return super.toString() + SEPARATOR + Value;
    }
}
