package api;

import dataStorage.IDataStorage;
import dataStorage.WrappedKeyValue;

import java.security.InvalidKeyException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 3:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class Database<TKey, TValue> {

    private IDataStorage<TKey, TValue> _dataStorage;

    public Database(IDataStorage<TKey, TValue> dataStorage) {
        if (dataStorage == null)
            throw new IllegalArgumentException("dataStorage");
        _dataStorage = dataStorage;
    }

    public TValue Get(TKey tKey) throws InvalidKeyException {
        WrappedKeyValue<TKey, TValue> value = _dataStorage.Get(tKey);
        if (value == null || value.IsDeleted)
            throw new InvalidKeyException();
        return value.Value;
    }

    public void AddOrUpdate(TKey tKey, TValue tValue) {
        _dataStorage.AddOrUpdate(tKey, tValue);
    }

    public void Add(TKey tKey, TValue tValue) throws InvalidKeyException {
        WrappedKeyValue<TKey, TValue> item = _dataStorage.Get(tKey);
        if (item != null && !item.IsDeleted)
            throw new InvalidKeyException();
        _dataStorage.AddOrUpdate(tKey, tValue);
    }

    public void Update(TKey tKey, TValue tValue) throws InvalidKeyException {
        WrappedKeyValue<TKey, TValue> item = _dataStorage.Get(tKey);
        if (item == null || item.IsDeleted)
            throw new InvalidKeyException();
        _dataStorage.AddOrUpdate(tKey, tValue);
    }

    public void Delete(TKey tKey) {
        _dataStorage.Delete(tKey);
    }

    public void Close() {
        _dataStorage.Close();
    }
}