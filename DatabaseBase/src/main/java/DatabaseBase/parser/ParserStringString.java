package DatabaseBase.parser;

import DatabaseBase.entities.StringSizable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/29/13
 * Time: 9:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParserStringString extends Parser<StringSizable, StringSizable> {

    public ParserStringString(Lexer lexer) {
        super(lexer);
    }

    @Override
    public StringSizable GetKey(String str) {
        return new StringSizable(str);
    }

    @Override
    public StringSizable GetValue(String str) {
        return new StringSizable(str);
    }
}
