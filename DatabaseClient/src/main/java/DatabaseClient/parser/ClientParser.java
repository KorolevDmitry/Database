package DatabaseClient.parser;

import DatabaseBase.commands.CommandNode;
import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.commands.ServerCommand;
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
 * Time: 5:09 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ClientParser<TKey extends ISizable, TValue extends ISizable> extends Parser<TKey, TValue> {
    public ClientParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public Query Parse(String str) throws LexerException, ParserException {
        ArrayList<Lexem> lexems = _lexer.Lex(str);
        return Parse(lexems.iterator(), str);
    }

    private Query Parse(Iterator<Lexem> lexems, String wholeRequest) throws LexerException, ParserException {
        Query tree = new Query();
        tree.Command = ParseCommand(lexems, wholeRequest);
        return tree;
    }

    private CommandNode ParseCommand(Iterator<Lexem> lexems, String wholeRequest) throws LexerException, ParserException {
        while (true) {
            if (!lexems.hasNext())
                throw new ParserException("Unexpactable count of parameters");
            Lexem lexem = lexems.next();
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
                        case ADD:
                        case UPDATE:
                        case ADD_OR_UPDATE:
                            String key = ParseNextLiteral(lexems);
                            return new ServerCommand(requestCommand, GetKey(key), wholeRequest);
                        default:
                            throw new ParserException("Unexpected requestCommand: " + requestCommand);
                    }
            }
        }
    }
}
