package DatabaseServer.parser;

import DatabaseBase.commands.*;
import DatabaseBase.entities.Query;
import DatabaseBase.exceptions.LexerException;
import DatabaseBase.exceptions.ParserException;
import DatabaseBase.interfaces.ISizable;
import DatabaseBase.parser.Lexem;
import DatabaseBase.parser.Lexer;
import DatabaseBase.parser.Parser;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 5:05 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ServerParser<TKey extends ISizable, TValue extends ISizable> extends Parser<TKey, TValue> {

    public ServerParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public Query Parse(String str) throws LexerException, ParserException {
        ArrayList<Lexem> lexems = _lexer.Lex(str);
        return Parse(lexems.iterator());
    }

    private Query Parse(Iterator<Lexem> lexems) throws LexerException, ParserException {
        Query tree = new Query();
        tree.Command = ParseCommand(lexems);
        return tree;
    }

    protected CommandNode ParseCommand(Iterator<Lexem> lexems) throws ParserException {
        while (true) {
            if (!lexems.hasNext())
                throw new ParserException("Unexpactable count of parameters");
            Lexem lexem = lexems.next();
            String key;
            String value;
            switch (lexem.LexemType) {
                case WHITESPACE:
                    break;
                case LITERAL:
                    RequestCommand requestCommand;
                    try {
                        requestCommand = RequestCommand.valueOf(lexem.Value.toUpperCase());
                    } catch (IllegalArgumentException exception) {
                        throw new ParserException("Unknown requestCommand: " + lexem.Value);
                    }
                    switch (requestCommand) {
                        case QUIT:
                        case HELP:
                            if (lexems.hasNext())
                                throw new ParserException("Unexpactable count of parameters");
                            return new CommandSingleNode(requestCommand);
                        case GET:
                        case DELETE:
                            key = ParseNextLiteral(lexems);
                            if (lexems.hasNext())
                                throw new ParserException("Unexpactable count of parameters");
                            return new CommandKeyNode(requestCommand, GetKey(key));
                        case ADD:
                        case UPDATE:
                        case ADD_OR_UPDATE:
                            key = ParseNextLiteral(lexems);
                            value = ParseNextLiteral(lexems);
                            if (lexems.hasNext())
                                throw new ParserException("Unexpected count of arguments");
                            return new CommandKeyValueNode(requestCommand, GetKey(key), GetValue(value));
                        default:
                            throw new ParserException("Unexpected requestCommand: " + requestCommand);
                    }
            }
        }
    }
}
