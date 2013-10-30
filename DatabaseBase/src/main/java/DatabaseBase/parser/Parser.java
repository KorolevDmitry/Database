package DatabaseBase.parser;

import DatabaseBase.commands.*;
import DatabaseBase.commands.service.ReplicateCommand;
import DatabaseBase.commands.service.ServiceCommand;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.Route;
import DatabaseBase.entities.ServerRole;
import DatabaseBase.exceptions.LexerException;
import DatabaseBase.exceptions.ParserException;
import DatabaseBase.interfaces.ISizable;

import java.util.ArrayList;
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

    public Query Parse(String str) throws LexerException, ParserException {
        ArrayList<Lexem> lexems = _lexer.Lex(str);
        return Parse(lexems.iterator(), str);
    }

    protected abstract TKey GetKey(String key) throws ParserException;

    protected abstract TValue GetValue(String value) throws ParserException;

    protected CommandNode ParseGET(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        String key = ParseNextLiteral(lexems);
        CheckEnd(lexems);

        return new CommandKeyNode(RequestCommand.GET, GetKey(key));
    }

    protected CommandNode ParseADD(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        String key = ParseNextLiteral(lexems);
        String value = ParseNextLiteral(lexems);
        CheckEnd(lexems);

        return new CommandKeyValueNode(RequestCommand.ADD, GetKey(key), GetValue(value));
    }

    protected CommandNode ParseUPDATE(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        String key = ParseNextLiteral(lexems);
        String value = ParseNextLiteral(lexems);
        CheckEnd(lexems);

        return new CommandKeyValueNode(RequestCommand.UPDATE, GetKey(key), GetValue(value));
    }

    protected CommandNode ParseADD_OR_UPDATE(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        String key = ParseNextLiteral(lexems);
        String value = ParseNextLiteral(lexems);
        CheckEnd(lexems);

        return new CommandKeyValueNode(RequestCommand.ADD_OR_UPDATE, GetKey(key), GetValue(value));
    }

    protected CommandNode ParseDELETE(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        String key = ParseNextLiteral(lexems);
        CheckEnd(lexems);

        return new CommandKeyNode(RequestCommand.DELETE, GetKey(key));
    }

    protected CommandNode ParseADD_SERVER(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        String serverHost = ParseNextLiteral(lexems);
        String serverRole = ParseNextLiteral(lexems);
        ServerRole role;
        try {
            role = ServerRole.valueOf(serverRole.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ParserException("Unknown serverRole: " + serverRole);
        }

        try {
            Route server = new Route(serverHost, role, null);
            Route master = null;
            if (role == ServerRole.Slave) {
                String masterHost = ParseNextLiteral(lexems);
                master = new Route(masterHost, ServerRole.Master, null);
            }
            server.Master = master;
            CheckEnd(lexems);

            return new ServiceCommand(RequestCommand.ADD_SERVER, server);
        } catch (IllegalArgumentException e) {
            throw new ParserException(e.getMessage(), e);
        }
    }

    protected CommandNode ParseREMOVE_SERVER(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        String serverHost = ParseNextLiteral(lexems);

        try {
            Route server = new Route(serverHost, ServerRole.Master, null);
            CheckEnd(lexems);

            return new ServiceCommand(RequestCommand.REMOVE_SERVER, server);
        } catch (IllegalArgumentException e) {
            throw new ParserException(e.getMessage(), e);
        }
    }

    protected CommandNode ParseGET_SERVERS_LIST(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        CheckEnd(lexems);

        return new ServiceCommand(RequestCommand.GET_SERVERS_LIST, null);
    }

    protected CommandNode ParsePING(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        String serverHost = ParseNextLiteral(lexems);

        try {
            Route server = new Route(serverHost, ServerRole.Master, null);
            CheckEnd(lexems);

            return new ServiceCommand(RequestCommand.PING, server);
        } catch (IllegalArgumentException e) {
            throw new ParserException(e.getMessage(), e);
        }
    }

    protected CommandNode ParseREPLICATE(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        String serverHostFrom = ParseNextLiteral(lexems);
        String serverHostTo = ParseNextLiteral(lexems);
        String startIndex = ParseNextLiteral(lexems);
        String removeAfterReplicationCompleted = ParseNextLiteral(lexems);

        try {
            Route serverFrom = new Route(serverHostFrom, ServerRole.Master, null);
            Route serverTo = new Route(serverHostTo, ServerRole.Master, null);
            int index = Integer.parseInt(startIndex);
            boolean remove = Boolean.parseBoolean(removeAfterReplicationCompleted);
            CheckEnd(lexems);

            return new ReplicateCommand(serverFrom, serverTo, index, remove);
        } catch (NumberFormatException e) {
            throw new ParserException("startIndex should be integer, removeAfterReplicationCompleted should be boolean");
        } catch (IllegalArgumentException e) {
            throw new ParserException(e.getMessage(), e);
        }
    }

    protected CommandNode ParseUPDATE_SERVER(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        String serverHost = ParseNextLiteral(lexems);

        try {
            Route server = new Route(serverHost, ServerRole.Master, null);
            CheckEnd(lexems);

            return new ServiceCommand(RequestCommand.UPDATE_SERVER, server);
        } catch (IllegalArgumentException e) {
            throw new ParserException(e.getMessage(), e);
        }
    }

    protected CommandNode ParseHELP(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        CheckEnd(lexems);
        return new CommandSingleNode(RequestCommand.HELP);
    }

    protected CommandNode ParseQUIT(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        CheckEnd(lexems);
        return new CommandSingleNode(RequestCommand.QUIT);
    }




    private Query Parse(Iterator<Lexem> lexems, String wholeRequest) throws LexerException, ParserException {
        Query tree = new Query();
        tree.Command = ParseCommand(lexems, wholeRequest);
        return tree;
    }

    private CommandNode ParseCommand(Iterator<Lexem> lexems, String wholeRequest) throws ParserException {
        while (true) {
            if (!lexems.hasNext())
                throw new ParserException("Unexpected count of parameters");
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
                            return ParseQUIT(lexems, wholeRequest);
                        case HELP:
                            return ParseHELP(lexems, wholeRequest);
                        case GET:
                            return ParseGET(lexems, wholeRequest);
                        case DELETE:
                            return ParseDELETE(lexems, wholeRequest);
                        case ADD:
                            return ParseADD(lexems, wholeRequest);
                        case UPDATE:
                            return ParseUPDATE(lexems, wholeRequest);
                        case ADD_OR_UPDATE:
                            return ParseADD_OR_UPDATE(lexems, wholeRequest);
                        case ADD_SERVER:
                            return ParseADD_SERVER(lexems, wholeRequest);
                        case REMOVE_SERVER:
                            return ParseREMOVE_SERVER(lexems, wholeRequest);
                        case GET_SERVERS_LIST:
                            return ParseGET_SERVERS_LIST(lexems, wholeRequest);
                        case PING:
                            return ParsePING(lexems, wholeRequest);
                        case REPLICATE:
                            return ParseREPLICATE(lexems, wholeRequest);
                        case UPDATE_SERVER:
                            return ParseUPDATE_SERVER(lexems, wholeRequest);
                        default:
                            throw new ParserException("Unexpected requestCommand: " + requestCommand);
                    }
            }
        }
    }

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

    protected void CheckEnd(Iterator<Lexem> lexems) throws ParserException {
        if (lexems.hasNext())
            throw new ParserException("Unexpected count of parameters");
    }
}
