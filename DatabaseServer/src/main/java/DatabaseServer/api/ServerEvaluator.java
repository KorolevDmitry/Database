package DatabaseServer.api;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandKeyValueNode;
import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.commands.service.ReplicateCommand;
import DatabaseBase.commands.service.ServiceCommand;
import DatabaseBase.components.Evaluator;
import DatabaseBase.components.ReplicationQueue;
import DatabaseBase.components.TcpSender;
import DatabaseBase.components.TransactionLogger;
import DatabaseBase.entities.*;
import DatabaseBase.exceptions.ConnectionException;
import DatabaseBase.exceptions.EvaluateException;
import DatabaseBase.exceptions.TransactionException;
import DatabaseBase.interfaces.IDataStorage;
import DatabaseBase.interfaces.ISizable;
import DatabaseBase.parser.Parser;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 7:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerEvaluator<TKey extends ISizable, TValue extends ISizable> extends Evaluator<TKey, TValue> {
    IDataStorage<TKey, TValue> _dataStorage;
    TransactionLogger _transactionLogger;
    ReplicationQueue _replicationQueue;
    Route _current;
    boolean _isReadyToRemove;

    public ServerEvaluator(IDataStorage<TKey, TValue> dataStorage, Parser parser, Route current, TcpSender<TKey, TValue> sender) throws IOException {
        this(dataStorage, parser, current, sender, new TransactionLogger(new MemoryBasedDataStorage<IntegerSizable, Query>()));
    }

    public ServerEvaluator(IDataStorage<TKey, TValue> dataStorage, Parser parser, Route current, TcpSender<TKey, TValue> sender, TransactionLogger transactionLogger) {
        super(parser);
        _dataStorage = dataStorage;
        _transactionLogger = transactionLogger;
        _current = current;
        _replicationQueue = new ReplicationQueue(sender);
        _current.IsReady = false;
        _current.IsAlive = true;
    }

    @Override
    public EvaluationResult Evaluate(Query query) {
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
            QueryExecutionStarted(query);
            if (query.Command instanceof ServiceCommand)
                EvaluateService((ServiceCommand) query.Command, evaluationResult);
            else if (query.Command instanceof CommandKeyValueNode)
                Evaluate((CommandKeyValueNode<TKey, TValue>) query.Command, evaluationResult);
            else if (query.Command instanceof CommandKeyNode)
                Evaluate((CommandKeyNode<TKey>) query.Command, evaluationResult);
            else if (query.Command instanceof CommandSingleNode) {
                Evaluate((CommandSingleNode) query.Command, evaluationResult);
            }
        } catch (EvaluateException evaluateException) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = evaluateException.getMessage();
        } catch (TransactionException e) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = e.getMessage();
        } finally {
            try {
                QueryExecutionEnded(evaluationResult);
            } catch (TransactionException e) {
                e.printStackTrace();
            }
        }

        return evaluationResult;
    }

    @Override
    public void Close() {
        if (_dataStorage != null) {
            try {
                _dataStorage.Close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            _dataStorage = null;
        }
        if (_replicationQueue != null) {
            _replicationQueue.Stop();
            _replicationQueue = null;
        }
        if (_transactionLogger != null) {
            _transactionLogger.Close();
            _transactionLogger = null;
        }
    }

    private void Evaluate(CommandKeyNode<TKey> command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        try {
            switch (command.GetCommand()) {
                case GET:
                    WrappedKeyValue<TKey, TValue> value = _dataStorage.Get(command.Key);
//                    if (value == null || value.IsDeleted)
//                        throw new InvalidKeyException();
                    evaluationResult.Result = value == null ? null : value.Value;
                    break;
                case DELETE:
                    _dataStorage.Delete(command.Key);
                    evaluationResult.HasReturnResult = false;
                    evaluationResult.Result = null;
                    break;
                default:
                    throw new EvaluateException("Unexpected requestCommand in KeyNode: " + command.GetCommand());
            }
//        } catch (InvalidKeyException invalidKeyException) {
//            throw new EvaluateException();
        } catch (IOException iOException) {
            throw new EvaluateException("Internal database error occurred", iOException);
        }
    }

    private void Evaluate(CommandSingleNode command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        evaluationResult.HasReturnResult = false;
        try {
            switch (command.GetCommand()) {
                case QUIT:
                    _dataStorage.Close();
                    evaluationResult.Result = null;
                    evaluationResult.Quit = true;
                    break;
                case HELP:
                    PrintHelp();
                    break;
                default:
                    throw new EvaluateException("Unexpected requestCommand in SingleNode: " + command.GetCommand());
            }
        } catch (IOException iOException) {
            throw new EvaluateException("Internal database error occurred", iOException);
        }
    }

    private void Evaluate(CommandKeyValueNode<TKey, TValue> command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        try {
            evaluationResult.HasReturnResult = false;
            WrappedKeyValue<TKey, TValue> item;
            switch (command.GetCommand()) {
                case ADD_OR_UPDATE:
                    _dataStorage.AddOrUpdate(command.Key, command.Value);
                    evaluationResult.Result = null;
                    break;
                case ADD:
                    item = _dataStorage.Get(command.Key);
                    if (item != null && !item.IsDeleted)
                        throw new InvalidKeyException("Key is already exist: " + command.Key);
                    _dataStorage.AddOrUpdate(command.Key, command.Value);
                    evaluationResult.Result = null;
                    break;
                case UPDATE:
                    item = _dataStorage.Get(command.Key);
                    if (item == null || item.IsDeleted)
                        throw new InvalidKeyException("Key does not exist: " + command.Key);
                    _dataStorage.AddOrUpdate(command.Key, command.Value);
                    evaluationResult.Result = null;
                    break;
                default:
                    throw new EvaluateException("Unexpected requestCommand in KeyValueNode: " + command.GetCommand());
            }
        } catch (InvalidKeyException invalidKeyException) {
            throw new EvaluateException(invalidKeyException.getMessage(), invalidKeyException);
        } catch (IOException iOException) {
            throw new EvaluateException("Internal database error occurred", iOException);
        }
    }

    private void PrintHelp() {

    }

    private void QueryExecutionStarted(Query query) throws TransactionException {
        RequestCommand command = query.Command.GetCommand();
        if ((command == RequestCommand.ADD || command == RequestCommand.ADD_OR_UPDATE ||
                command == RequestCommand.UPDATE || command == RequestCommand.DELETE)) {
            _transactionLogger.AddTransaction(query);
        }
        _messageReceived.notifyObservers(query);
    }

    private void QueryExecutionEnded(EvaluationResult<TKey, TValue> result) throws TransactionException {
        RequestCommand command = result.ExecutionQuery.Command.GetCommand();
        if ((command == RequestCommand.ADD || command == RequestCommand.ADD_OR_UPDATE ||
                command == RequestCommand.UPDATE || command == RequestCommand.DELETE)) {
            if (!result.HasError) {
                int numberOfRoutesToReplicateSync = result.ExecutionQuery.NumberToWrite - 1;
                for (int i = 0; i < _current.Slaves.size(); i++) {
                    try {
                        SendReplicate(result.ExecutionQuery, _current.Slaves.get(i), numberOfRoutesToReplicateSync > 0, result);
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                        UndoQuery(result.ExecutionQuery);
                    }
                    numberOfRoutesToReplicateSync--;
                }
            }
            _transactionLogger.CommitTransaction(result.ExecutionQuery, !result.HasError);
        }
        _messageExecuted.notifyObservers(result);
    }

    private void UndoQuery(Query query){

    }

    private void EvaluateService(ServiceCommand command, EvaluationResult<TKey, TValue> evaluationResult) {
        if (command instanceof ReplicateCommand)
            EvaluateReplication((ReplicateCommand) command, evaluationResult);
        else if (command.GetCommand() == RequestCommand.PING) {
            EvaluatePing(evaluationResult);
        } else if (command.GetCommand() == RequestCommand.UPDATE_SERVER) {
            EvaluateUpdateServer(command, evaluationResult);
        }
    }

    private void EvaluateReplication(ReplicateCommand command, EvaluationResult<TKey, TValue> evaluationResult) {
        if (command.ToRoute.equals(_current)) {
            if (!command.Route.equals(_current)) {
                _current.IsReady = false;
                for (int i = 0; i < command.Queries.size(); i++) {
                    Evaluate(command.Queries.get(i));
                }
                _current.IsReady = command.IsLast;
            } else {
                _current.IsReady = true;
            }
        } else if (command.Route.equals(_current)) {
            try {
                List<Query> queriesToReplicate = _transactionLogger.GetCommittedTransactionsAfter(command.StartIndex,
                        command.EndIndex);
                command.Queries = new ArrayList<Query>();
                for (int i = 0; i < queriesToReplicate.size(); i++) {
                    Query query = queriesToReplicate.get(i);
                    if (query.Success && (query.Command.GetCommand() == RequestCommand.ADD ||
                            query.Command.GetCommand() == RequestCommand.UPDATE ||
                            query.Command.GetCommand() == RequestCommand.ADD_OR_UPDATE ||
                            query.Command.GetCommand() == RequestCommand.DELETE)) {
                        command.Queries.add(query);
                    }
                }
                command.IsLast = true;
                ProcessReplicate(command, true, evaluationResult);
                _isReadyToRemove = command.RemoveFromCluster;
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void EvaluatePing(EvaluationResult<TKey, TValue> evaluationResult) {
        evaluationResult.HasReturnResult = true;
        evaluationResult.ServiceResult = new ServiceResult();
        evaluationResult.ServiceResult.Route = Route.Clone(_current);
        evaluationResult.ServiceResult.ReadyToBeRemoved = _isReadyToRemove;
        System.out.println(_current.toString());
        if (_isReadyToRemove) {
            _isReadyToRemove = false;
            _current.IsReady = false;
        }
    }

    private void EvaluateUpdateServer(ServiceCommand command, EvaluationResult<TKey, TValue> evaluationResult) {
        boolean isReady = _current.IsReady;
        boolean isAlive = _current.IsAlive;
        _current = command.Route;
        evaluationResult.ServiceResult = new ServiceResult();
        _current.IsReady = isReady;
        _current.IsAlive = isAlive;
        evaluationResult.ServiceResult.Route = _current;
        System.out.println(_current.toString());
    }

    private void SendReplicate(Query query, Route route, boolean sync, EvaluationResult<TKey, TValue> evaluationResult) throws ConnectionException {
        ReplicateCommand command = new ReplicateCommand(_current, route, 0, 0, false);
        command.Queries = new ArrayList<Query>();
        command.Queries.add(query);
        command.IsLast = true;
        ProcessReplicate(command, sync, evaluationResult);
    }

    private void ProcessReplicate(ReplicateCommand command, boolean sync, EvaluationResult<TKey, TValue> evaluationResult) {
        if (sync) {
            _replicationQueue.ExecuteSync(command);
        } else {
            _replicationQueue.AddToExecution(command);
        }
    }
}
