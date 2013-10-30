package DatabaseBase.components;

import DatabaseBase.entities.IntegerSizable;
import DatabaseBase.entities.Query;
import DatabaseBase.entities.WrappedKeyValue;
import DatabaseBase.exceptions.TransactionException;
import DatabaseBase.interfaces.IDataStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 1:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionLogger {
    IDataStorage<IntegerSizable, Query> _log;
    int _currentId;

    public TransactionLogger(IDataStorage<IntegerSizable, Query> storage) throws IOException {
        _log = storage;
        _currentId = GetCurrentId();
    }

    public void AddTransaction(Query query) throws TransactionException {
        try {
            query.UniqueId = ++_currentId;
            _log.AddOrUpdate(new IntegerSizable(query.UniqueId), query);
        } catch (IOException e) {
            throw new TransactionException("Can not add transaction", e);
        }
    }

    public void CommitTransaction(Query query, boolean success) throws TransactionException {
        try {
            query.Completed = true;
            IntegerSizable key = new IntegerSizable(query.UniqueId);
            WrappedKeyValue<IntegerSizable, Query> value = _log.Get(key);
            if (value == null || value.IsDeleted)
                throw new TransactionException("No transaction to commit");
            _log.AddOrUpdate(new IntegerSizable(query.UniqueId), query);
        } catch (IOException e) {
            throw new TransactionException("Can not commit transaction", e);
        }
    }

    public List<Query> GetUnCommittedTransactions() throws IOException {
        //TODO: Implement in different way
        List<WrappedKeyValue<IntegerSizable, Query>> elements = _log.GetElements();
        List<Query> unCommittedElements = new ArrayList<Query>();
        for (int i = 0; i < elements.size(); ++i) {
            WrappedKeyValue<IntegerSizable, Query> element = elements.get(i);
            if (!element.IsDeleted && (element.Value != null) && !element.Value.Completed) {
                unCommittedElements.add(element.Value);
            }
        }
        return unCommittedElements;
    }

    public List<Query> GetCommittedTransactionsAfter(int id) throws IOException {
        //TODO: Implement in different way
        List<WrappedKeyValue<IntegerSizable, Query>> elements = _log.GetElements();
        List<Query> unCommittedElements = new ArrayList<Query>();
        for (int i = 0; i < elements.size(); ++i) {
            WrappedKeyValue<IntegerSizable, Query> element = elements.get(i);
            if (!element.IsDeleted && (element.Value != null) && element.Value.Completed && element.Value.UniqueId > id) {
                unCommittedElements.add(element.Value);
            }
        }
        return unCommittedElements;
    }

    private int GetCurrentId() throws IOException {
        //TODO: Implement in different way
        int id = 0;
        List<WrappedKeyValue<IntegerSizable, Query>> elements = _log.GetElements();
        for (int i = 0; i < elements.size(); ++i) {
            WrappedKeyValue<IntegerSizable, Query> element = elements.get(i);
            if (!element.IsDeleted && !(element.Value != null) && element.Value.UniqueId > id) {
                id = element.Value.UniqueId;
            }
        }

        return id;
    }
}
