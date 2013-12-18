package DatabaseClient.api;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandMultiKeyNode;
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

import java.util.List;

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
    private int _numberToRead;
    private int _numberToWrite;

    public ClientEvaluator(TcpSender<TKey, TValue> sender, Parser parser, Route balancer, int numberToRead, int numberToWrite) {
        super(parser);
        _sender = sender;
        _balancer = balancer;
        _numberToRead = numberToRead;
        _numberToWrite = numberToWrite;
    }

    public ClientEvaluator(TcpSender<TKey, TValue> sender, Parser parser, Route balancer) {
        this(sender, parser, balancer, 1, 1);
    }

    private void EvaluateInternal(CommandSingleNode command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
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
        switch (query.Command.GetCommand()) {
            case GET_KEY_INDEX:
            case UPDATE_SERVER:
            case ADD_SERVER:
            case REMOVE_SERVER:
            case GET_SERVERS_LIST:
            case PING:
            case REPLICATE:
                EvaluateBalancerQuery(query, evaluationResult);
                break;
            case GET:
                EvaluateRead(query, evaluationResult);
                break;
            case ADD:
            case UPDATE:
            case ADD_OR_UPDATE:
            case DELETE:
                EvaluateWrite(query, evaluationResult);
                break;
            default:
                throw new EvaluateException("Unexpected command: " + query.Command.GetCommand());
        }
    }

    private void EvaluateBalancerQuery(Query query, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        EvaluationResult<TKey, TValue> balancerResult = null;
        try {
            balancerResult = _sender.Send(query, _balancer);
        } catch (ConnectionException e) {
            throw new EvaluateException("Balancer " + _balancer + " is not available", e);
        }
        if (balancerResult == null)
            throw new EvaluateException("Balancer " + _balancer + " is not available");
        CopyResults(balancerResult, evaluationResult, true);
    }

    private void EvaluateWrite(Query query, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        EvaluationResult<TKey, TValue> balancerResult = null;
        EvaluationResult<TKey, TValue> serverEvaluationResult = null;
        try {
            balancerResult = _sender.Send(query, _balancer);
        } catch (ConnectionException e) {
            throw new EvaluateException("Balancer " + _balancer + " is not available", e);
        }
        if (balancerResult == null)
            throw new EvaluateException("Balancer " + _balancer + " is not available");

        if (balancerResult.HasError) {
            throw new EvaluateException(balancerResult.ErrorDescription);
        }
        List<Route> routes = balancerResult.ServiceResult.Routes;
        if (routes == null || routes.isEmpty())
            throw new EvaluateException("Route for save info is not available for a while");
        try {
            for (int j = 0; j < routes.size(); j++){
                serverEvaluationResult = _sender.Send(query, routes.get(j));
            }
        } catch (ConnectionException e) {
            serverEvaluationResult = new EvaluationResult<TKey, TValue>();
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = e.getMessage();
        }

        CopyResults(balancerResult, evaluationResult, true);
        CopyResults(serverEvaluationResult, evaluationResult, false);
    }

    private void EvaluateRead(Query query, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        EvaluationResult<TKey, TValue> balancerResult = null;
        EvaluationResult<TKey, TValue> previousEvaluationResult = null;
        EvaluationResult<TKey, TValue> serverEvaluationResult = null;
        for (int i = 0; i < query.NumberToRead; i++) {
            try {
                balancerResult = _sender.Send(query, _balancer);
            } catch (ConnectionException e) {
                throw new EvaluateException("Balancer " + _balancer + " is not available", e);
            }
            if (balancerResult == null)
                throw new EvaluateException("Balancer " + _balancer + " is not available");

            if (balancerResult.HasError) {
                throw new EvaluateException(balancerResult.ErrorDescription);
            }
            List<Route> routes = balancerResult.ServiceResult.Routes;
            if (routes == null || routes.isEmpty())
                throw new EvaluateException("There are " + i + " available routes of " +
                        query.NumberToRead + " requested");

            try {
                for (int j = 0; j < routes.size(); j++){
                    serverEvaluationResult = AccumulateResults(serverEvaluationResult, _sender.Send(query, routes.get(j)));
                    if(serverEvaluationResult.HasError){
                        //implement properly
                        CopyResults(balancerResult, evaluationResult, true);
                        CopyResults(previousEvaluationResult, evaluationResult, false);
                        return;
                    }
                }
            } catch (ConnectionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if (serverEvaluationResult != null && !serverEvaluationResult.HasError) {
                if (previousEvaluationResult != null && !previousEvaluationResult.equals(serverEvaluationResult)) {
                    throw new EvaluateException("Different answer from routes");
                }
                previousEvaluationResult = serverEvaluationResult;
                serverEvaluationResult = null;
                query.ExecutionRoutes.addAll(balancerResult.ServiceResult.Routes);
            }
        }
        if (previousEvaluationResult == null) {
            previousEvaluationResult = serverEvaluationResult;
        }

        CopyResults(balancerResult, evaluationResult, true);
        CopyResults(previousEvaluationResult, evaluationResult, false);
    }

    private void CopyResults(EvaluationResult<TKey, TValue> source, EvaluationResult<TKey, TValue> target, boolean copyService) {
        if (source == null) {
            target.HasReturnResult = false;
            target.HasError = true;
            target.ErrorDescription = "Null result";
            return;
        }

        target.Result = source.Result;
        target.HasReturnResult = source.HasReturnResult;
        target.HasError = source.HasError;
        target.ErrorDescription = source.ErrorDescription;
        target.Quit = source.Quit;
        if (copyService) {
            target.ServiceResult = source.ServiceResult;
            target.HasBalancerResult = source.HasBalancerResult;
        }
    }

    private EvaluationResult<TKey, TValue> AccumulateResults(EvaluationResult<TKey, TValue> oldOne, EvaluationResult<TKey, TValue> newOne) {
        EvaluationResult<TKey, TValue> result = oldOne;
        if (oldOne == null) {
            result = newOne;
        }
        else if (newOne == null) {
            result.HasReturnResult = false;
            result.HasError = true;
            result.ErrorDescription = "Null result";
        } else {
            result.Result.addAll(newOne.Result);
        }

        return result;
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
        query.NumberToRead = _numberToRead;
        query.NumberToWrite = _numberToWrite;

        try {
            if (query.Command instanceof CommandMultiKeyNode)
                EvaluateSend(query, evaluationResult);
            else if (query.Command instanceof CommandKeyNode)
                EvaluateSend(query, evaluationResult);
            else if (query.Command instanceof ServiceCommand)
                EvaluateSend(query, evaluationResult);
            else if (query.Command instanceof CommandSingleNode) {
                EvaluateInternal((CommandSingleNode) query.Command, evaluationResult);
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
