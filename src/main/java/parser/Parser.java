package parser;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class Parser {
    private Lexer _lexer;

    public Parser(Lexer lexer) {
        if (lexer == null)
            throw new IllegalArgumentException("lexer");
        _lexer = lexer;
    }

    public ParsedTree Parse(String str) {
        return null;
    }
}
