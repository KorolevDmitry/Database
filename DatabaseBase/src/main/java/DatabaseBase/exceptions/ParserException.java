package DatabaseBase.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParserException extends Exception {
    public ParserException(String message, Exception innerException) {
        super(message, innerException);
    }

    public ParserException(String message) {
        super(message);
    }
}
