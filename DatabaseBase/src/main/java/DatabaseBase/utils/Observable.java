package DatabaseBase.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 1:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class Observable<ObservedType> {

    private List<Observer<ObservedType>> _observers =
            new LinkedList<Observer<ObservedType>>();

    public void addObserver(Observer<ObservedType> obs) {
        if (obs == null) {
            throw new IllegalArgumentException("Null observer");
        }
        _observers.add(obs);
    }

    public void removeObserver(Observer<ObservedType> obs) {
        if (obs == null) {
            throw new IllegalArgumentException("Null observer");
        }
        Iterator<Observer<ObservedType>> iterator = _observers.iterator();
        while (iterator.hasNext())
        {
            Observer<ObservedType> current = iterator.next();
            if (obs.equals(current))
            {
                iterator.remove();
            }
        }
    }

    public void notifyObservers(ObservedType data) {
        for (Observer<ObservedType> obs : _observers) {
            obs.update(data);
        }
    }
}
