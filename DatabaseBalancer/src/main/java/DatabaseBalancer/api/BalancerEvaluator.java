package DatabaseBalancer.api;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.commands.service.ReplicateCommand;
import DatabaseBase.commands.service.ServiceCommand;
import DatabaseBase.components.Evaluator;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.ServiceResult;
import DatabaseBase.exceptions.BalancerException;
import DatabaseBase.exceptions.EvaluateException;
import DatabaseBase.interfaces.IBalancer;
import DatabaseBase.interfaces.ISizable;
import DatabaseBase.parser.Parser;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 6:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class BalancerEvaluator<TKey extends ISizable, TValue extends ISizable> extends Evaluator<TKey, TValue> {
    IBalancer _balancer;
    int _currentId;

    public BalancerEvaluator(IBalancer balanser, Parser<TKey, TValue> parser) {
        super(parser);
        _balancer = balanser;
    }

    @Override
    public EvaluationResult<TKey, TValue> Evaluate(Query query) {
        EvaluationResult<TKey, TValue> evaluationResult = new EvaluationResult<TKey, TValue>();
        evaluationResult.ExecutionQuery = query;
        evaluationResult.HasReturnResult = true;
        evaluationResult.ExecutionQuery.UniqueId = ++ _currentId;
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
            QueryExecutionStarted(query);
            if (query.Command instanceof ServiceCommand)
                EvaluateServiceCommand((ServiceCommand) query.Command, evaluationResult);
            else if (query.Command instanceof CommandKeyNode)
                EvaluateDatabaseCommand((CommandKeyNode) query.Command, evaluationResult);
            else if (query.Command instanceof CommandSingleNode) {
                EvaluateInternalCommand((CommandSingleNode) query.Command, evaluationResult);
            }
            QueryExecutionEnded(evaluationResult);
        } catch (EvaluateException evaluateException) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = evaluateException.getMessage();
        }

        return evaluationResult;
    }

    @Override
    public void Close() {
        if(_balancer != null)
        {
            _balancer.Close();
            _balancer = null;
        }
    }

    private void EvaluateServiceCommand(ServiceCommand command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        try {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasBalancerResult = true;
            switch (command.GetCommand()) {
                case ADD_SERVER:
                    _balancer.AddServer(command.Route);
                    break;
                case REMOVE_SERVER:
                    _balancer.RemoveServer(command.Route);
                    break;
                case UPDATE_SERVER:
                    _balancer.UpdateServer(command.Route);
                    break;
                case PING:
                    _balancer.Ping(command.Route);
                    break;
                case REPLICATE:
                    ReplicateCommand replicateCommand = (ReplicateCommand) command;
                    _balancer.Replicate(replicateCommand.Route, replicateCommand.ToRoute, replicateCommand.StartIndex, replicateCommand.EndIndex, replicateCommand.RemoveFromCluster);
                    break;
                case GET_SERVERS_LIST:
                    evaluationResult.HasReturnResult = true;
                    evaluationResult.ServiceResult = new ServiceResult();
                    evaluationResult.ServiceResult.Servers = _balancer.GetServersList();
                    break;
                default:
                    throw new EvaluateException("Unexpected requestCommand in ServiceCommand: " + command.GetCommand());
            }
        } catch (BalancerException e) {
            throw new EvaluateException(e.getMessage(), e);
        }
    }

    private void EvaluateDatabaseCommand(CommandKeyNode command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        try {
            evaluationResult.HasBalancerResult = false;
            evaluationResult.HasReturnResult = true;
            evaluationResult.ServiceResult = new ServiceResult();
            evaluationResult.ServiceResult.Route = _balancer.GetRoute(command, null);
        } catch (BalancerException e) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = e.getMessage();
        }
    }

    private void EvaluateInternalCommand(CommandSingleNode command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        evaluationResult.HasBalancerResult = true;
        switch (command.GetCommand()) {
            case QUIT:
                //TODO: implement
                //_balancer.Close();
                evaluationResult.Result = null;
                evaluationResult.Quit = true;
                break;
            case HELP:
                PrintHelp();
                break;
            default:
                throw new EvaluateException("Unexpected requestCommand in SingleNode: " + command.GetCommand());
        }
    }

    private void QueryExecutionStarted(Query query) {
        _messageReceived.notifyObservers(query);
    }

    private void QueryExecutionEnded(EvaluationResult<TKey, TValue> result) {
        _messageExecuted.notifyObservers(result);
    }

    private void PrintHelp() {

    }
}
