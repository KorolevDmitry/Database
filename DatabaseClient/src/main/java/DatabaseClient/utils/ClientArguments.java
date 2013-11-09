package DatabaseClient.utils;

import DatabaseBase.interfaces.INameUsageDescriptionPattern;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/13/13
 * Time: 8:00 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ClientArguments implements INameUsageDescriptionPattern {
    LIST_OF_SERVERS ("--list-of-servers", "--list-of-servers {hostname:port[;hostname:port]*}", "List of servers to connect to."),
    NUMBER_OF_ROUTES_TO_WRITE ("--number-of-routes-to-write", "--number-of-routes-to-write {number}", "Number of routes that must be updated during write command"),
    NUMBER_OF_ROUTES_TO_READ ("--number-of-routes-to-read", "--number-of-routes-to-read {number}", "Number of routes from which info must be got");

    private String _name;
    private String _usage;
    private String _description;

    public String GetName()
    {
        return _name;
    }

    public String GetUsage()
    {
        return _usage;
    }

    public String GetDescription()
    {
        return _description;
    }

    ClientArguments(String parameterName, String usage, String description) {
        _name = parameterName;
        _usage = usage;
        _description = description;
    }
}
