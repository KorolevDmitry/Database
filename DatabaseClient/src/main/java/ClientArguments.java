import interfaces.INameUsageDescriptionPattern;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/13/13
 * Time: 8:00 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ClientArguments implements INameUsageDescriptionPattern {
    LIST_OF_SERVERS ("--list-of-servers", "--list-of-servers {hostname:port[;hostname:port]*}", "List of servers to connect to.");

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
