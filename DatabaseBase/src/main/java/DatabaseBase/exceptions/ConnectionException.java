package DatabaseBase.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/26/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionException extends Exception {
    public ConnectionException(String message, Exception innerException) {
        super(message, innerException);
    }

    public ConnectionException(String message) {
        super(message);
    }
}
