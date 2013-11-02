package DatabaseBase.entities;

import DatabaseBase.interfaces.ISizable;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 7:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class EvaluationResult<TKey extends ISizable, TValue extends ISizable> implements Serializable {
    public Query ExecutionQuery;
    public TValue Result;
    public boolean HasBalancerResult;
    public boolean HasReturnResult;
    public boolean HasError;
    public String ErrorDescription;
    public boolean Quit;
    public ServiceResult ServiceResult;
}
