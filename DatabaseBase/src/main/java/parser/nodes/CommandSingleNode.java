package parser.nodes;

import exceptions.EvaluateException;
import interfaces.IDataStorage;

import java.io.IOException;
import java.security.InvalidKeyException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 7:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandSingleNode<TKey, TValue> extends CommandNode<TKey, TValue> {
    public CommandSingleNode(RequestCommand requestCommand) {
        super(requestCommand);
    }

    @Override
    public TValue Evaluate(IDataStorage<TKey, TValue> dataStorage) throws EvaluateException, InvalidKeyException, IOException {
        switch (GetCommand())
        {
            case QUIT:
                dataStorage.Close();
            default:
                throw new EvaluateException();
        }
    }

    @Override
    public TKey GetKey() {
        return null;
    }
}
