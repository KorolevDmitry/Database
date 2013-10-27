package DatabaseBase.parser;

import DatabaseBase.entities.Query;
import DatabaseBase.exceptions.LexerException;
import DatabaseBase.exceptions.ParserException;
import DatabaseBase.interfaces.ISizable;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Parser<TKey extends ISizable, TValue extends ISizable> {
    protected Lexer _lexer;

    protected Parser(Lexer lexer) {
        if (lexer == null)
            throw new IllegalArgumentException("lexer");
        _lexer = lexer;
    }

    public abstract Query Parse(String str) throws LexerException, ParserException;

    public abstract TKey GetKey(String str);

    public abstract TValue GetValue(String str);

    protected String ParseNextLiteral(Iterator<Lexem> lexems) throws ParserException {
        while (true) {
            if (!lexems.hasNext())
                throw new ParserException("Unexpactable count of parameters");
            Lexem lexem = lexems.next();
            switch (lexem.LexemType) {
                case WHITESPACE:
                    break;
                case LITERAL:
                    return lexem.Value;
                default:
                    throw new ParserException("Unexpected lexemType: " + lexem.LexemType);
            }
        }
    }
}