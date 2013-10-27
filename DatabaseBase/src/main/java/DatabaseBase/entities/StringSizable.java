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

    public StringSizable(String value)
    {
        Value = value;
    }

    @Override
    public long GetSize() {
        return Value == null ? Integer.SIZE : 8 * (int) ((((Value.length()) * 2) + 45) / 8);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(!(obj instanceof StringSizable))
            return false;
        if(Value == null && ((StringSizable)obj).Value == null)
            return true;
        return (Value.equals(((StringSizable)obj).Value));
    }

    @Override
    public int hashCode(){
        return Value.hashCode();
    }
}
