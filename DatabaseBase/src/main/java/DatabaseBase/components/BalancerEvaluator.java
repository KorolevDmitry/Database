package DatabaseBase.components;

import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.interfaces.ISizable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 6:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class BalancerEvaluator<TKey extends ISizable, TValue extends ISizable> extends Evaluator<TKey, TValue> {
    @Override
    public EvaluationResult<TKey, TValue> Evaluate(String query) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
