package parser;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public enum LexemType {
    STRING("\"[a-zA-Z\\\\:_\\.]+\""),
    LITERAL("[a-zA-Z_]+"),
    NUMBER("-?[0-9]+"),
    WHITESPACE("[ \\t\\f\\r\\n]+");

    public final String pattern;

    private LexemType(String pattern) {
        this.pattern = pattern;
    }
}
