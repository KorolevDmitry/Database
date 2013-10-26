package DatabaseBase.interfaces;

import DatabaseBase.entities.WrappedKeyValue;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 2:08 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IDataStorage<TKey, TValue> {
    WrappedKeyValue<TKey, TValue> Get(TKey key) throws IOException;

    //TODO: replace with iterator
    List<WrappedKeyValue<TKey, TValue>> GetElements();

    void AddOrUpdate(TKey key, TValue value) throws IOException;

    void AddOrUpdate(List<WrappedKeyValue<TKey, TValue>> values) throws IOException;

    void Delete(TKey key) throws IOException;

    void Clear();

    void Close() throws IOException;
}
