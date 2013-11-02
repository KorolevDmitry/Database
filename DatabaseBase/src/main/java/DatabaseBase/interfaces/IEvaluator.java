package DatabaseBase.interfaces;

import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.utils.Observer;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 7:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IEvaluator<TKey extends ISizable, TValue extends ISizable> {
    EvaluationResult<TKey, TValue> Evaluate(String query);
    EvaluationResult<TKey, TValue> Evaluate(Query query);
    void Close();

    void AddMessageReceivedObserver(Observer<Query> observer);
    void RemoveMessageReceivedObserver(Observer<Query> observer);

    void AddMessageExecutedObserver(Observer<EvaluationResult<TKey, TValue>> observer);
    void RemoveMessageExecutedObserver(Observer<EvaluationResult<TKey, TValue>> observer);

}
