package DatabaseBase.entities;

import DatabaseBase.interfaces.ISizable;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 2:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class WrappedKeyValue<TKey extends ISizable, TValue extends ISizable> implements Serializable {
    public TKey Key;
    public TValue Value;
    public Long Size;
    public Boolean IsDeleted;

    public WrappedKeyValue(TKey key, TValue value) {
        this(key, value, false);
    }

    public WrappedKeyValue(TKey key, TValue value, boolean isDeleted) {
        Key = key;
        Value = value;
        IsDeleted = isDeleted;
        Size = key == null ? 0 : key.GetSize();
        Size += value == null ? 0 : value.GetSize();
        Size += 0;
    }
}
