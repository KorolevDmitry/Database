package DatabaseServer;

import DatabaseBase.interfaces.ISizable;
import DatabaseBase.parser.Parser;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/28/13
 * Time: 7:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseEngine<TKey extends ISizable, TValue extends ISizable> {
    private Parser<TKey, TValue> _parser;

    public DatabaseEngine(){

    }
}
