package DatabaseBalancer.api;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.commands.service.ReplicateCommand;
import DatabaseBase.commands.service.ServiceCommand;
import DatabaseBase.components.Evaluator;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.exceptions.BalancerException;
import DatabaseBase.exceptions.EvaluateException;
import DatabaseBase.exceptions.LexerException;
import DatabaseBase.exceptions.ParserException;
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
    Parser<TKey, TValue> _parser;
    IBalancer _balancer;

    public BalancerEvaluator(IBalancer balanser, Parser<TKey, TValue> parser) {
        _balancer = balanser;
        _parser = parser;
    }

    @Override
    public EvaluationResult<TKey, TValue> Evaluate(String query) {
        EvaluationResult<TKey, TValue> evaluationResult = new EvaluationResult<TKey, TValue>();
        evaluationResult.ExecutionString = query;
        if (query == null) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = "Null query";
            return evaluationResult;
        }
        try {
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

    void Evaluate(Query tree, EvaluationResult<TKey, TValue> evaluationResult) {
        evaluationResult.HasReturnResult = true;
        if (tree == null) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = "Null query";
            return;
        }
        if (tree.Command == null) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = "Null command";
            return;
        }
        try {
            QueryExecutionStarted(tree);
            if (tree.Command instanceof ServiceCommand)
                EvaluateServiceCommand((ServiceCommand) tree.Command, evaluationResult);
            else if (tree.Command instanceof CommandKeyNode)
                EvaluateDatabaseCommand((CommandKeyNode) tree.Command, evaluationResult);
            else if (tree.Command instanceof CommandSingleNode) {
                EvaluateInternalCommand((CommandSingleNode) tree.Command, evaluationResult);
            }
            QueryExecutionEnded(evaluationResult);
        } catch (EvaluateException evaluateException) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = evaluateException.getMessage();
        }
    }

    private void EvaluateServiceCommand(ServiceCommand command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        try {
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
                    _balancer.Replicate(replicateCommand.Route, replicateCommand.ToRoute, replicateCommand.StartIndex, replicateCommand.RemoveFromCluster);
                    break;
                default:
                    throw new EvaluateException("Unexpected requestCommand in SingleNode: " + command.GetCommand());
            }
        } catch (BalancerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void EvaluateDatabaseCommand(CommandKeyNode command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        try {
            evaluationResult.Route = _balancer.GetRoute(command, null);
        } catch (BalancerException e) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = e.getMessage();
        }
    }

    private void EvaluateInternalCommand(CommandSingleNode command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
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
            case GET_SERVERS_LIST:
                evaluationResult.HasReturnResult = true;
                //TODO: implement
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
