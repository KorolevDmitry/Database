package parser;

import exceptions.LexerException;
import exceptions.ParserException;
import parser.nodes.*;

import java.util.ArrayList;
import java.util.Iterator;

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

    public ParsedTree Parse(String str) throws LexerException, ParserException {
        ArrayList<Lexem> lexems = _lexer.Lex(str);
        return Parse(lexems.iterator());
    }

    private ParsedTree Parse(Iterator<Lexem> lexems) throws LexerException, ParserException {
        ParsedTree tree = new ParsedTree();
        tree.Command = ParseCommand(lexems);
        return tree;
    }

    private static CommandNode ParseCommand(Iterator<Lexem> lexems) throws ParserException {
        while (true) {
            if (!lexems.hasNext())
                throw new ParserException();
            Lexem lexem = lexems.next();
            String key;
            String value;
            switch (lexem.LexemType) {
                case WHITESPACE:
                    break;
                case LITERAL:
                    try {
                        RequestCommand requestCommand = RequestCommand.valueOf(lexem.Value.toUpperCase());
                        switch (requestCommand) {
                            case QUIT:
                            case HELP:
                                return new CommandSingleNode(requestCommand);
                            case GET:
                            case DELETE:
                                key = ParseNextLiteral(lexems);
                                return new CommandKeyNode(requestCommand, key);
                            case ADD:
                            case UPDATE:
                            case ADD_OR_UPDATE:
                                key = ParseNextLiteral(lexems);
                                value = ParseNextLiteral(lexems);
                                return new CommandKeyValueNode(requestCommand, key, value);
                            default:
                                throw new ParserException();
                        }
                    } catch (IllegalArgumentException exception) {
                        throw new ParserException();
                    }
            }
        }
    }

    private static String ParseNextLiteral(Iterator<Lexem> lexems) throws ParserException {
        while (true) {
            if (!lexems.hasNext())
                throw new ParserException();
            Lexem lexem = lexems.next();
            switch (lexem.LexemType) {
                case WHITESPACE:
                    break;
                case LITERAL:
                    return lexem.Value;
                default:
                    throw new ParserException();
            }
        }
    }
}
