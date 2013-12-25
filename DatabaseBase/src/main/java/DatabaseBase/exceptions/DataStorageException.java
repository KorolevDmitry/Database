package DatabaseBase.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 12/25/13
 * Time: 9:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataStorageException extends Exception {
    public DataStorageException(String message, Exception innerException) {
        super(message, innerException);
    }

    public DataStorageException(String message) {
        super(message);
    }
}
