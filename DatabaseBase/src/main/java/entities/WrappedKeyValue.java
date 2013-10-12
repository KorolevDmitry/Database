package entities;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 2:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class WrappedKeyValue<TKey, TValue> implements Serializable {
    public TKey Key;
    public TValue Value;
    public int Size;
    public boolean IsDeleted;

    public WrappedKeyValue(TKey key, TValue value) {
        this(key, value, false);
    }

    public WrappedKeyValue(TKey key, TValue value, boolean isDeleted) {
        Key = key;
        Value = value;
        IsDeleted = isDeleted;
        //TODO: implement another counting
        Size = key == null ? Integer.SIZE : 8 * (int) ((((key.toString().length()) * 2) + 45) / 8);
        Size += value == null ? Integer.SIZE : 8 * (int) ((((value.toString().length()) * 2) + 45) / 8);
        Size += 1;
    }
}
