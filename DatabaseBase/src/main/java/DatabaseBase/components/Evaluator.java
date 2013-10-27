package DatabaseBase.components;

import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.interfaces.IEvaluator;
import DatabaseBase.interfaces.ISizable;
import DatabaseBase.utils.Observable;
import DatabaseBase.utils.Observer;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 1:59 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Evaluator<TKey extends ISizable, TValue extends ISizable> implements IEvaluator<TKey, TValue> {
    protected Observable<Query> _messageReceived;
    protected Observable<EvaluationResult<TKey, TValue>> _messageExecuted;

    protected Evaluator() {
        _messageReceived = new Observable<Query>();
        _messageExecuted = new Observable<EvaluationResult<TKey, TValue>>();
    }

    @Override
    public void AddMessageReceivedObserver(Observer<Query> observer) {
        _messageReceived.addObserver(observer);
    }

    @Override
    public void RemoveMessageReceivedObserver(Observer<Query> observer) {
        _messageReceived.removeObserver(observer);
    }

    @Override
    public void AddMessageExecutedObserver(Observer<EvaluationResult<TKey, TValue>> observer) {
        _messageExecuted.addObserver(observer);
    }

    @Override
    public void RemoveMessageExecutedObserver(Observer<EvaluationResult<TKey, TValue>> observer) {
        _messageExecuted.removeObserver(observer);
    }
}
