package DatabaseServer.api;

import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandKeyValueNode;
import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.components.Evaluator;
import DatabaseBase.components.TransactionLogger;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.IntegerSizable;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.WrappedKeyValue;
import DatabaseBase.exceptions.EvaluateException;
import DatabaseBase.exceptions.LexerException;
import DatabaseBase.exceptions.ParserException;
import DatabaseBase.exceptions.TransactionException;
import DatabaseBase.interfaces.IDataStorage;
import DatabaseBase.interfaces.ISizable;
import DatabaseBase.parser.Parser;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;

import java.io.IOException;
import java.security.InvalidKeyException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 7:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerEvaluator<TKey extends ISizable, TValue extends ISizable> extends Evaluator<TKey, TValue> {
    IDataStorage<TKey, TValue> _dataStorage;
    Parser _parser;
    TransactionLogger _transactionLogger;

    public ServerEvaluator(IDataStorage<TKey, TValue> dataStorage, Parser parser) throws IOException {
        this(dataStorage, parser, new TransactionLogger(new MemoryBasedDataStorage<IntegerSizable, Query>()));
    }

    public ServerEvaluator(IDataStorage<TKey, TValue> dataStorage, Parser parser, TransactionLogger transactionLogger) {
        _dataStorage = dataStorage;
        _parser = parser;
        _transactionLogger = transactionLogger;
    }

    @Override
    public EvaluationResult Evaluate(String query) {
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
        evaluationResult.ExecutionQuery = tree;
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
            if (tree.Command instanceof CommandKeyValueNode)
                Evaluate((CommandKeyValueNode<TKey, TValue>) tree.Command, evaluationResult);
            else if (tree.Command instanceof CommandKeyNode)
                Evaluate((CommandKeyNode<TKey>) tree.Command, evaluationResult);
            else if (tree.Command instanceof CommandSingleNode) {
                Evaluate((CommandSingleNode) tree.Command, evaluationResult);
            }
        } catch (EvaluateException evaluateException) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = evaluateException.getMessage();
        } catch (TransactionException e) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = e.getMessage();
        }
        finally {
            try {
                QueryExecutionEnded(evaluationResult);
            } catch (TransactionException e) {
                e.printStackTrace();
            }
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
        _transactionLogger.AddTransaction(query);
        _messageReceived.notifyObservers(query);
    }

    private void QueryExecutionEnded(EvaluationResult<TKey, TValue> result) throws TransactionException {
        _transactionLogger.CommitTransaction(result.ExecutionQuery, !result.HasError);
        _messageExecuted.notifyObservers(result);
    }
}
