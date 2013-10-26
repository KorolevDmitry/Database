package DatabaseBase.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class EvaluateException extends Exception {
    public EvaluateException(String message, Exception innerException) {
        super(message, innerException);
    }

    public EvaluateException(String message) {
        super(message);
    }
}
