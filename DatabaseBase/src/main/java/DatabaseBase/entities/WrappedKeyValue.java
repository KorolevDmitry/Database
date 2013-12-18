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
public class WrappedKeyValue<TKey extends ISizable, TValue extends ISizable> implements Serializable,
        Comparable<WrappedKeyValue<TKey, TValue>> {
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

    @Override
    public int compareTo(WrappedKeyValue<TKey, TValue> o) {
        if(o == null)
            return -1;
        if(Key.hashCode() > o.Key.hashCode())
            return 1;
        else if(Key.hashCode() == o.Key.hashCode())
            return 0;
        else
            return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof WrappedKeyValue))
            return false;
        WrappedKeyValue temp = (WrappedKeyValue) obj;

        if (Key == null)
            if(temp.Key != null)
                return false;
        if (Value == null)
            if(temp.Value != null)
                return false;

        return Key.equals(temp.Key) && Value.equals(temp.Value);
    }
}
