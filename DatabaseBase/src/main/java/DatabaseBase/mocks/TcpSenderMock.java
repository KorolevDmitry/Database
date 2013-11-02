package DatabaseBase.mocks;

import DatabaseBase.commands.CommandNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.components.TcpSender;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.Route;
import DatabaseBase.exceptions.ConnectionException;
import DatabaseBase.interfaces.ISizable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 7:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class TcpSenderMock<TKey extends ISizable, TValue extends ISizable> extends TcpSender<TKey, TValue> {
    public List<CommandNode> SequenceOfSentCommands;
    private ConcurrentHashMap<Route, ConcurrentHashMap<RequestCommand, EvaluationResult<TKey, TValue>>> _expectedBehaviour;

    public TcpSenderMock()
    {
        SequenceOfSentCommands = new ArrayList<CommandNode>();
        _expectedBehaviour = new ConcurrentHashMap<Route, ConcurrentHashMap<RequestCommand, EvaluationResult<TKey, TValue>>>();
    }

    public void AddExpectedBehavior(Route route, RequestCommand requestCommand, EvaluationResult<TKey, TValue> result) {
        if (!_expectedBehaviour.containsKey(route))
            _expectedBehaviour.put(route, new ConcurrentHashMap<RequestCommand, EvaluationResult<TKey, TValue>>());
        _expectedBehaviour.get(route).put(requestCommand, result);
    }

    @Override
    public EvaluationResult<TKey, TValue> Send(Query query, Route route, int timeout) throws ConnectionException {
        SequenceOfSentCommands.add(query.Command);
        if (!_expectedBehaviour.containsKey(route))
            throw new ConnectionException("Unexpected route: " + route);
        if (!_expectedBehaviour.get(route).containsKey(query.Command.GetCommand()))
            throw new ConnectionException("Unexpected command: route " + route + " command " + query.Command.GetCommand());
        return _expectedBehaviour.get(route).get(query.Command.GetCommand());
    }

    @Override
    public EvaluationResult<TKey, TValue> Send(Query query, Route route) throws ConnectionException {
        return Send(query, route, 0);
    }
}
