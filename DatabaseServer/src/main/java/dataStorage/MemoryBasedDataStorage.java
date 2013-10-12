package dataStorage;

import interfaces.IDataStorage;
import entities.WrappedKeyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 2:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class MemoryBasedDataStorage<TKey, TValue> implements IDataStorage<TKey, TValue> {
    ConcurrentHashMap<TKey, WrappedKeyValue<TKey, TValue>> _storage;

    public MemoryBasedDataStorage() {
        _storage = new ConcurrentHashMap<TKey, WrappedKeyValue<TKey, TValue>>();
        InitialRecovery();
    }

    private void InitialRecovery() {
    }

    public WrappedKeyValue<TKey, TValue> RemoveCompletly(TKey key) {
        return _storage.remove(key);
    }

    @Override
    public WrappedKeyValue<TKey, TValue> Get(TKey tKey) {
        WrappedKeyValue<TKey, TValue> value = _storage.get(tKey);
        return value;
    }

    @Override
    public List<WrappedKeyValue<TKey, TValue>> GetElements() {
        return new ArrayList<WrappedKeyValue<TKey, TValue>>(_storage.values());
    }

    @Override
    public void AddOrUpdate(TKey tKey, TValue tValue) {
        _storage.put(tKey, new WrappedKeyValue<TKey, TValue>(tKey, tValue));
    }

    @Override
    public void AddOrUpdate(List<WrappedKeyValue<TKey, TValue>> wrappedKeyValues) {
        for (WrappedKeyValue<TKey, TValue> item : wrappedKeyValues) {
            _storage.put(item.Key, item);
        }
    }

    @Override
    public void Delete(TKey tKey) {
        WrappedKeyValue<TKey, TValue> item = _storage.get(tKey);
        if (item != null) {
            item.IsDeleted = true;
            item.Value = null;
        } else {
            _storage.put(tKey, new WrappedKeyValue<TKey, TValue>(tKey, null, true));
        }
    }

    @Override
    public void Clear() {
        _storage.clear();
    }

    @Override
    public void Close() {
        Clear();
    }
}
