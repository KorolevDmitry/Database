package DatabaseClient.api;

import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.commands.ServerCommand;
import DatabaseBase.components.Evaluator;
import DatabaseBase.components.TcpSender;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.Route;
import DatabaseBase.exceptions.*;
import DatabaseBase.interfaces.IBalancer;
import DatabaseBase.interfaces.ISizable;
import DatabaseBase.parser.Parser;
import DatabaseBase.utils.ArgumentsHelper;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 8:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientEvaluator<TKey extends ISizable, TValue extends ISizable> extends Evaluator<TKey, TValue> {
    private TcpSender<TKey, TValue> _sender;
    private IBalancer _balancer;
    private Parser _parser;

    public ClientEvaluator(TcpSender<TKey, TValue> sender, Parser parser, IBalancer balancer) {
        _sender = sender;
        _parser = parser;
        _balancer = balancer;
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
            //TODO: analyze
            Route route = _balancer.GetRoute(command, null);
            if(route == null)
                throw new EvaluateException("Can not execute " + command.GetCommand() + " for a while");
            EvaluationResult<TKey, TValue> serverEvaluationResult = _sender.Send(command, route);
            evaluationResult.Result = serverEvaluationResult.Result;
            evaluationResult.HasReturnResult = serverEvaluationResult.HasReturnResult;
            evaluationResult.HasError = serverEvaluationResult.HasError;
            evaluationResult.ErrorDescription = serverEvaluationResult.ErrorDescription;
        } catch (ConnectionException e) {
            throw new EvaluateException(e.getMessage(), e);
        } catch (BalancerException e) {
            throw new EvaluateException(e.getMessage(), e);
        }
    }

    private static void PrintHelp() {
        System.out.println("You can use next commands:");
        ArgumentsHelper.PrintDescription(RequestCommand.values());
    }

    @Override
    public EvaluationResult<TKey, TValue> Evaluate(String query) {
        EvaluationResult<TKey, TValue> evaluationResult = new EvaluationResult<TKey, TValue>();
        evaluationResult.ExecutionString = query;
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
