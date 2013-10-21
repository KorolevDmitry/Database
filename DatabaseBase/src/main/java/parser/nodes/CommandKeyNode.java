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
public class CommandKeyNode<TKey, TValue> extends CommandSingleNode<TKey, TValue> {
    TKey Key;

    public CommandKeyNode(RequestCommand requestCommand, TKey key) {
        super(requestCommand);
        Key = key;
    }

    @Override
    public String toString() {
        return super.toString() + SEPARATOR + Key;
    }

    @Override
    public TValue Evaluate(IDataStorage<TKey, TValue> dataStorage) throws EvaluateException, InvalidKeyException, IOException {
        switch (GetCommand())
        {
            case GET:
                WrappedKeyValue<TKey, TValue> value = dataStorage.Get(Key);
                if (value == null || value.IsDeleted)
                    throw new InvalidKeyException();
                return value.Value;
            case DELETE:
                dataStorage.Delete(Key);
                return null;
            default:
                throw new EvaluateException();
        }
    }

    @Override
    public TKey GetKey() {
        return Key;
    }
}
