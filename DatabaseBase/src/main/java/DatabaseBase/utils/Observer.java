package DatabaseBase.utils;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 1:21 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Observer<ObservedType> {
    public void update(ObservedType data);
}
