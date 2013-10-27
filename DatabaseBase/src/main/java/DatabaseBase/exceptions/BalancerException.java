package DatabaseBase.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class BalancerException extends Exception {
    public BalancerException(String message, Exception innerException) {
        super(message, innerException);
    }

    public BalancerException(String message) {
        super(message);
    }
}
