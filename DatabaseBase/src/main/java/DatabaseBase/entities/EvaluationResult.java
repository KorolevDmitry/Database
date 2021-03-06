package DatabaseBase.entities;

import DatabaseBase.interfaces.ISizable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 7:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class EvaluationResult<TKey extends ISizable, TValue extends ISizable> implements Serializable {
    public Query ExecutionQuery;
    public List<WrappedKeyValue<TKey, TValue>> Result;
    public boolean HasBalancerResult;
    public boolean HasReturnResult;
    public boolean HasError;
    public String ErrorDescription;
    public boolean Quit;
    public ServiceResult ServiceResult;

    public EvaluationResult(){
        Result = new ArrayList<WrappedKeyValue<TKey, TValue>>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof EvaluationResult))
            return false;
        EvaluationResult temp = (EvaluationResult) obj;
        if (Quit != temp.Quit)
            return false;
        if (Quit && temp.Quit)
            return true;
        if (HasError != temp.HasError)
            return false;
        if (HasError && temp.HasError)
            return (ErrorDescription == null && temp.ErrorDescription == null) || ErrorDescription.equals(temp.ErrorDescription);
        if (HasBalancerResult != temp.HasBalancerResult)
            return false;
        if (HasBalancerResult && temp.HasBalancerResult)
            return (ServiceResult == null && temp.ServiceResult == null) || ServiceResult.equals(temp.ServiceResult);
        if (HasReturnResult != temp.HasReturnResult)
            return false;
        if (HasReturnResult && temp.HasReturnResult) {
            if (Result == null)
                return temp.Result == null;
            if (temp.Result == null)
                return false;
            return Arrays.equals(Result.toArray(), temp.Result.toArray());
        }

        return true;
    }
}
