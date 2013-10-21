package interfaces;

import exceptions.EvaluateException;

import java.io.IOException;
import java.security.InvalidKeyException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 7:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IEvaluator<TKey, TValue> {
    TValue Evaluate(IDataStorage<TKey, TValue> dataStorage) throws EvaluateException, InvalidKeyException, IOException;
}
