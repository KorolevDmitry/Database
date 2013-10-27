package DatabaseClient.parser;

import DatabaseBase.entities.StringSizable;
import DatabaseBase.parser.Lexer;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 7:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientParserStringString extends ClientParser<StringSizable, StringSizable> {
    public ClientParserStringString(Lexer lexer) {
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
