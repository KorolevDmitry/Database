package api;

import entities.EvaluationResult;
import entities.Query;
import entities.WrappedKeyValue;
import exceptions.EvaluateException;
import interfaces.IDataStorage;
import interfaces.IEvaluator;
import parser.commands.CommandKeyNode;
import parser.commands.CommandKeyValueNode;
import parser.commands.CommandSingleNode;

import java.io.IOException;
import java.security.InvalidKeyException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 7:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerEvaluator<TKey, TValue> implements IEvaluator {
    IDataStorage<TKey, TValue> _dataStorage;

    public ServerEvaluator(IDataStorage<TKey, TValue> dataStorage)
    {
        _dataStorage = dataStorage;
    }

    @Override
    public EvaluationResult Evaluate(Query tree) {
        EvaluationResult<TKey, TValue> evaluationResult = new EvaluationResult<TKey, TValue>();
        evaluationResult.ExecutionQuery = tree;
        evaluationResult.HasReturnResult = true;
        try {
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
        }

        return evaluationResult;
    }

    private void Evaluate(CommandKeyNode<TKey> command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        try {
            switch (command.GetCommand()) {
                case GET:
                    WrappedKeyValue<TKey, TValue> value = _dataStorage.Get(command.Key);
                    if (value == null || value.IsDeleted)
                        throw new InvalidKeyException();
                    evaluationResult.Result = value.Value;
                    break;
                case DELETE:
                    _dataStorage.Delete(command.Key);
                    evaluationResult.Result = null;
                    break;
                default:
                    throw new EvaluateException();
            }
        } catch (InvalidKeyException invalidKeyException) {
            throw new EvaluateException();
        } catch (IOException iOException) {
            throw new EvaluateException();
        }
    }

    private void Evaluate(CommandSingleNode command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        evaluationResult.HasReturnResult = false;
        try {
            switch (command.GetCommand()) {
                case QUIT:
                    _dataStorage.Close();
                    evaluationResult.Result = null;
                    evaluationResult.Exit = true;
                    break;
                default:
                    throw new EvaluateException();
            }
        } catch (IOException iOException) {
            throw new EvaluateException();
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
                        throw new InvalidKeyException();
                    _dataStorage.AddOrUpdate(command.Key, command.Value);
                    evaluationResult.Result = null;
                    break;
                case UPDATE:
                    item = _dataStorage.Get(command.Key);
                    if (item == null || item.IsDeleted)
                        throw new InvalidKeyException();
                    _dataStorage.AddOrUpdate(command.Key, command.Value);
                    evaluationResult.Result = null;
                    break;
                default:
                    throw new EvaluateException();
            }
        } catch (InvalidKeyException invalidKeyException) {
            throw new EvaluateException();
        } catch (IOException iOException) {
            throw new EvaluateException();
        }
    }
}
