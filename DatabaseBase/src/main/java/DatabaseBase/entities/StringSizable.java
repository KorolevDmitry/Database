package DatabaseBase.entities;

import DatabaseBase.interfaces.ISizable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 3:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class StringSizable implements ISizable {
    public String Value;

    public StringSizable(String value) {
        Value = value;
    }

    @Override
    public long GetSize() {
        return Value == null ? Integer.SIZE : 8 * (int) ((((Value.length()) * 2) + 45) / 8);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof StringSizable))
            return false;
        if (Value == null)
            return ((StringSizable) obj).Value == null;
        return (Value.equals(((StringSizable) obj).Value));
    }

    @Override
    public int hashCode() {
        return Value.hashCode();
    }

    @Override
    public String toString()
    {
        return Value;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == null)
            return -1;
        if (!(obj instanceof StringSizable))
            return -1;
        if (Value == null)
            return ((StringSizable) obj).Value == null ? 0 : -1;
        return (Value.compareTo(((StringSizable) obj).Value));
    }
}
