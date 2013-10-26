package DatabaseBase.interfaces;

import DatabaseBase.entities.EvaluationResult;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 7:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IEvaluator {
    EvaluationResult Evaluate(String query);
}
