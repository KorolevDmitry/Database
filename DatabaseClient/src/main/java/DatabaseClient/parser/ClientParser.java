package DatabaseClient.parser;

import DatabaseBase.entities.Query;
import DatabaseBase.exceptions.LexerException;
import DatabaseBase.exceptions.ParserException;
import DatabaseClient.parser.commands.*;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 7:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientParser extends Parser {
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
                            return new ServerCommand(requestCommand, key, wholeRequest);
                        default:
                            throw new ParserException("Unexpected requestCommand: " + requestCommand);
                    }
            }
        }
    }

}
