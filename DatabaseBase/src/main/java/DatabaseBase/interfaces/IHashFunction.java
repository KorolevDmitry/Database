package DatabaseBase.interfaces;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 4:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IHashFunction {
    int hash(Object key);
    void associate(Object key, Integer hash);
    void removeAssociation(Object key);
}
