package entities;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/12/13
 * Time: 12:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Route {
    public String Address;
    public int Port;

    public Route(String address, int port)
    {
        Address = address;
        Port = port;
    }
}
