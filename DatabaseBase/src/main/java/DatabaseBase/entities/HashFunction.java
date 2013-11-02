package DatabaseBase.entities;

import DatabaseBase.interfaces.IHashFunction;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    MessageDigest _md;

    public HashFunction() {
        try {
            _md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        _customHashes = new ConcurrentHashMap<Object, Integer>();
        Recover();
    }

    @Override
    public int hash(Object key) {
        if(key == null)
            return 0;
        if(_customHashes.containsKey(key))
            return _customHashes.get(key);
        BigInteger md5 = new BigInteger(_md.digest(key.toString().getBytes()));
        return Math.abs(md5.intValue());
    }

    @Override
    public void associate(Object key, Integer hash) {
        if(key == null)
            return;
        if(hash == null)
            return;
        if(hash < 0)
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
