package DatabaseBase.entities;

import DatabaseBase.interfaces.ISizable;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 3:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class IntegerSizable implements ISizable {
    private Integer _value;

    public IntegerSizable(int value) {
        _value = value;
    }

    @Override
    public long GetSize() {
        return Integer.SIZE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof IntegerSizable))
            return false;
        return (_value == ((IntegerSizable) obj)._value);
    }

    @Override
    public int hashCode() {
        return _value.hashCode();
    }
}
