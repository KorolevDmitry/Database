package DatabaseBase.entities;

import DatabaseBase.interfaces.IHashFunction;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 5:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class HashFunction implements IHashFunction {
    ConcurrentHashMap<Object, Integer> _customHashes;

    public HashFunction() {
        _customHashes = new ConcurrentHashMap<Object, Integer>();
        Recover();
    }

    @Override
    public int hash(Object key) {
        if(key == null)
            return 0;
        if(_customHashes.containsKey(key))
            return _customHashes.get(key);
        return key.hashCode();
    }

    @Override
    public void associate(Object key, Integer hash) {
        if(key == null)
            return;
        _customHashes.put(key, hash);
    }

    @Override
    public void removeAssociation(Object key) {
        _customHashes.remove(key);
    }

    private void Recover() {
    }

}
