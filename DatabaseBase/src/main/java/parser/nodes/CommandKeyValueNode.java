package parser.nodes;

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
public class CommandKeyValueNode<TKey, TValue> extends CommandKeyNode<TKey, TValue> {
    TValue Value;

    public CommandKeyValueNode(RequestCommand requestCommand, TKey key, TValue value) {
        super(requestCommand, key);
        Value = value;
    }

    @Override
    public TValue Evaluate(IDataStorage<TKey, TValue> dataStorage) throws EvaluateException, InvalidKeyException, IOException {
        WrappedKeyValue<TKey, TValue> item;
        switch (GetCommand())
        {
            case ADD_OR_UPDATE:
                dataStorage.AddOrUpdate(Key,  Value);
                return null;
            case ADD:
                item = dataStorage.Get(Key);
                if (item != null && !item.IsDeleted)
                    throw new InvalidKeyException();
                dataStorage.AddOrUpdate(Key, Value);
                return null;
            case UPDATE:
                item = dataStorage.Get(Key);
                if (item == null || item.IsDeleted)
                    throw new InvalidKeyException();
                dataStorage.AddOrUpdate(Key, Value);
                return null;
            default:
                throw new EvaluateException();
        }
    }

    @Override
    public String toString() {
        return super.toString() + SEPARATOR + Value;
    }
}
