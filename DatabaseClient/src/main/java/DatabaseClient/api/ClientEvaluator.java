package DatabaseClient.api;

import DatabaseBase.DatabaseServer.utils.ArgumentsHelper;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.exceptions.ConnectionException;
import DatabaseBase.exceptions.EvaluateException;
import DatabaseBase.exceptions.LexerException;
import DatabaseBase.exceptions.ParserException;
import DatabaseBase.interfaces.IEvaluator;
import DatabaseClient.parser.Parser;
import DatabaseClient.parser.ServerCommand;
import DatabaseClient.parser.commands.CommandSingleNode;
import DatabaseClient.parser.commands.RequestCommand;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 8:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientEvaluator<TKey, TValue> implements IEvaluator {
    private TcpSender<TKey, TValue> _sender;
    private Parser _parser;

    public ClientEvaluator(TcpSender<TKey, TValue> sender, Parser parser) {
        _sender = sender;
        _parser = parser;
    }

    private void Evaluate(Query tree, EvaluationResult<TKey, TValue> evaluationResult) {
        evaluationResult.HasReturnResult = true;
        try {
            if (tree.Command instanceof ServerCommand)
                Evaluate((ServerCommand<TKey>) tree.Command, evaluationResult);
            else if (tree.Command instanceof CommandSingleNode) {
                Evaluate((CommandSingleNode) tree.Command, evaluationResult);
                evaluationResult.HasReturnResult = false;
            }
        } catch (EvaluateException evaluateException) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = evaluateException.getMessage();
        }
    }

    private void Evaluate(CommandSingleNode command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        evaluationResult.HasReturnResult = false;
        switch (command.GetCommand()) {
            case HELP:
                PrintHelp();
                break;
            case QUIT:
                evaluationResult.Quit = true;
                break;
            default:
                throw new EvaluateException("Unexpected requestCommand in SingleNode: " + command.GetCommand());
        }
    }

    private void Evaluate(ServerCommand<TKey> command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        evaluationResult.HasReturnResult = false;
        try {
            EvaluationResult<TKey, TValue> serverEvaluationResult = _sender.Send(command);
            evaluationResult.Result = serverEvaluationResult.Result;
            evaluationResult.HasReturnResult = serverEvaluationResult.HasReturnResult;
            evaluationResult.HasError = serverEvaluationResult.HasError;
            evaluationResult.ErrorDescription = serverEvaluationResult.ErrorDescription;
        } catch (ConnectionException e) {
            e.printStackTrace();
            throw new EvaluateException("Connection problem", e);
        }
    }

    private static void PrintHelp() {
        System.out.println("You can use next commands:");
        ArgumentsHelper.PrintDescription(RequestCommand.values());
    }

    @Override
    public EvaluationResult Evaluate(String query) {
        EvaluationResult<TKey, TValue> evaluationResult = new EvaluationResult<TKey, TValue>();
        evaluationResult.ExecutionQuery = query;
        try{
           Evaluate(_parser.Parse(query), evaluationResult);
        } catch (LexerException e) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = e.getMessage();
        } catch (ParserException e) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = e.getMessage();
        }

        return evaluationResult;
    }
}
