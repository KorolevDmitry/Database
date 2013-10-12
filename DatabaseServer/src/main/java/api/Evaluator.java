package api;

import exceptions.EvaluateException;
import interfaces.IDataStorage;
import interfaces.IEvaluator;
import parser.ParsedTree;

import java.security.InvalidKeyException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 7:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator<TKey, TValue> {
    private IDataStorage<TKey, TValue> _dataStorage;

    public Evaluator(IDataStorage<TKey, TValue> dataStorage) {
        _dataStorage = dataStorage;
    }

    public TValue Evaluate(ParsedTree tree) throws EvaluateException, InvalidKeyException {
        return Evaluate(tree.Command);
    }

    private TValue Evaluate(IEvaluator<TKey, TValue> evaluator) throws EvaluateException, InvalidKeyException {
        return evaluator.Evaluate(_dataStorage);
    }
}
