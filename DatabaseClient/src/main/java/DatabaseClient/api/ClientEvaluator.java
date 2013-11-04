package DatabaseClient.api;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.commands.service.ServiceCommand;
import DatabaseBase.components.Evaluator;
import DatabaseBase.components.TcpSender;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.Route;
import DatabaseBase.exceptions.ConnectionException;
import DatabaseBase.exceptions.EvaluateException;
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
    private Route _balancer;

    public ClientEvaluator(TcpSender<TKey, TValue> sender, Parser parser, Route balancer) {
        super(parser);
        _sender = sender;
        _balancer = balancer;
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

    private void EvaluateSend(Query query, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        evaluationResult.HasReturnResult = false;
        try {
            //TODO: analyze
            EvaluationResult<TKey, TValue> balancerResult = _sender.Send(query, _balancer);
            EvaluationResult<TKey, TValue> serverEvaluationResult;
            if (balancerResult == null)
                throw new EvaluateException("Balancer " + _balancer + " is not available");
            if (!balancerResult.HasError && !balancerResult.HasBalancerResult) {
                Route route = balancerResult.ServiceResult.Route;
                CommandKeyNode<TKey> command = (CommandKeyNode<TKey>) query.Command;
                if (route == null)
                    throw new EvaluateException("Can not execute " + command.GetCommand() + " for a while");

                serverEvaluationResult = _sender.Send(query, route);
            } else {
                serverEvaluationResult = balancerResult;
            }

            evaluationResult.HasBalancerResult = balancerResult.HasBalancerResult;
            evaluationResult.ServiceResult = balancerResult.ServiceResult;
            evaluationResult.Result = serverEvaluationResult.Result;
            evaluationResult.HasReturnResult = serverEvaluationResult.HasReturnResult;
            evaluationResult.HasError = serverEvaluationResult.HasError;
            evaluationResult.ErrorDescription = serverEvaluationResult.ErrorDescription;
        } catch (ConnectionException e) {
            throw new EvaluateException(e.getMessage(), e);
        }
    }

    private static void PrintHelp() {
        System.out.println("You can use next commands:");
        ArgumentsHelper.PrintDescription(RequestCommand.values());
    }

    @Override
    public EvaluationResult<TKey, TValue> Evaluate(Query query) {
        EvaluationResult<TKey, TValue> evaluationResult = new EvaluationResult<TKey, TValue>();
        evaluationResult.ExecutionQuery = query;
        evaluationResult.HasReturnResult = true;
        if (query == null) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = "Null query";
            return evaluationResult;
        }
        if (query.Command == null) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = "Null command";
            return evaluationResult;
        }

        try {
            if (query.Command instanceof CommandKeyNode)
                EvaluateSend(query, evaluationResult);
            else if (query.Command instanceof ServiceCommand)
                EvaluateSend(query, evaluationResult);
            else if (query.Command instanceof CommandSingleNode) {
                Evaluate((CommandSingleNode) query.Command, evaluationResult);
                evaluationResult.HasReturnResult = false;
            }
        } catch (EvaluateException evaluateException) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = evaluateException.getMessage();
        }

        return evaluationResult;
    }

    @Override
    public void Close() {
    }
}
