package DatabaseBase.utils;

import DatabaseBase.interfaces.IHashFunction;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 4:36 PM
 * To change this template use File | Settings | File Templates.
 */

public class ConsistentHash<T> {

    private final IHashFunction _hashFunction;
    private final SortedMap<Integer, T> _circle = new TreeMap<Integer, T>();

    public ConsistentHash(IHashFunction hashFunction, Collection<T> nodes) {
        _hashFunction = hashFunction;

        for (T node : nodes) {
            add(node);
        }
    }

    public void add(T node) {
        _circle.put(_hashFunction.hash(node), node);
    }

    public void add(T node, Integer index) {
        _hashFunction.associate(node, index);
        _circle.put(_hashFunction.hash(node), node);
    }

    public void remove(T node) {
        _circle.remove(_hashFunction.hash(node));
        _hashFunction.removeAssociation(node);
    }

    public T get(Object key) {
        if (_circle.isEmpty()) {
            return null;
        }
        int hash = _hashFunction.hash(key);
        if (!_circle.containsKey(hash)) {
            SortedMap<Integer, T> headMap =
                    _circle.headMap(hash);
            hash = headMap.isEmpty() ?
                    _circle.lastKey() : headMap.lastKey();
        }
        return _circle.get(hash);
    }

    public T getNext(T node) {
        if (_circle.isEmpty())
            return null;
        int hash = _hashFunction.hash(node) + 1;
        SortedMap<Integer, T> tailMap =
                _circle.tailMap(hash);
        hash = tailMap.isEmpty() ?
                _circle.firstKey() : tailMap.firstKey();
        return _circle.get(hash);
    }

    public T getPrevious(T node) {
        if (_circle.isEmpty())
            return null;
        int hash = _hashFunction.hash(node);
        SortedMap<Integer, T> headMap =
                _circle.headMap(hash);
        hash = headMap.isEmpty() ?
                _circle.lastKey() : headMap.lastKey();
        return _circle.get(hash);
    }

    public boolean contains(T node) {
        return _circle.containsValue(node);
    }

    public int getIndex(Object key) {
        return _hashFunction.hash(key);
    }

    public List<T> getListOfValues() {
        return new ArrayList<T>(_circle.values());
    }
}
