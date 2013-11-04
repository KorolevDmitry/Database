package DatabaseBase.utils;

import DatabaseBase.entities.HashFunction;
import DatabaseBase.entities.Route;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 5:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsistentHashTest {
    ConsistentHash<Route> _routes;

    @Before
    public void setUp() {
        _routes = new ConsistentHash<Route>(new HashFunction(), new ArrayList<Route>());
    }

    @Test
    public void Add_One_GotItInAnyCase() {
        //arrange
        Route route = new Route("", 1);
        Route result = null;

        //act
        _routes.add(route);

        //assert
        result = _routes.get(route);
        assertEquals(route, result);
        result = _routes.get(1);
        assertEquals(route, result);
        result = _routes.get(2);
        assertEquals(route, result);
        result = _routes.get(3);
        assertEquals(route, result);
        result = _routes.getNext(route);
        assertEquals(route, result);
        result = _routes.getPrevious(route);
        assertEquals(route, result);
    }

    @Test
    public void AddWithIndex_One_GotItInAnyCase() {
        //arrange
        Route route = new Route("", 1);
        Route result = null;

        //act
        _routes.add(route, 1);

        //assert
        result = _routes.get(route);
        assertEquals(route, result);
        result = _routes.get(1);
        assertEquals(route, result);
        result = _routes.get(2);
        assertEquals(route, result);
        result = _routes.get(3);
        assertEquals(route, result);
        result = _routes.getNext(route);
        assertEquals(route, result);
        result = _routes.getPrevious(route);
        assertEquals(route, result);
    }

    @Test
    public void Add_Two_RoundBinding() {
        //arrange
        Route route1 = new Route("", 1);
        Route route2 = new Route("", 2);
        Route result = null;

        //act
        _routes.add(route1);
        _routes.add(route2);

        //assert
        result = _routes.get(route1);
        assertEquals(route1, result);
        result = _routes.get(route2);
        assertEquals(route2, result);
        result = _routes.getNext(route1);
        assertEquals(route2, result);
        result = _routes.getNext(result);
        assertEquals(route1, result);
        result = _routes.getPrevious(route2);
        assertEquals(route1, result);
        result = _routes.getPrevious(result);
        assertEquals(route2, result);
    }

    @Test
    public void AddedRouteWithSpecificIndex_AddedRouteBetweenTwoOthers_DidNotChangedRouteOfItem() {
        //arrange
        Route route1 = new Route("", 1123);
        Route route2 = new Route("", 2234);
        Route route3 = new Route("", 3333);
        _routes.add(route1);
        _routes.add(route2);
        int index1 = _routes.getIndex(route1);
        int index2 = _routes.getIndex(route2);
        Object item = new Object();
        Route routeOfItem = _routes.get(item);
        int specificIndex;
        if(!routeOfItem.equals(route2)){
            specificIndex = index2 + 1;
        }else{
            specificIndex = index1 + 1;
        }

        //act

        _routes.add(route3, specificIndex);

        //assert
        Route result = _routes.get(item);
        assertEquals(routeOfItem, result);
        assertEquals(index1, _routes.getIndex(route1));
        assertEquals(index2, _routes.getIndex(route2));
        assertEquals(specificIndex, _routes.getIndex(route3));
    }
}
