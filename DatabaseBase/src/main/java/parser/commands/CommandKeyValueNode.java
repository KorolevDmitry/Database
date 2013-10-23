package parser.commands;

import entities.WrappedKeyValue;
import exceptions.EvaluateException;
import interfaces.IDataStorage;

import java.io.IOException;
import java.security.InvalidKeyException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandKeyValueNode<TKey, TValue> extends CommandKeyNode<TKey> {
    public TValue Value;

    public CommandKeyValueNode(RequestCommand requestCommand, TKey key, TValue value) {
        super(requestCommand, key);
        Value = value;
    }

    @Override
    public String toString() {
        return super.toString() + SEPARATOR + Value;
    }
}
