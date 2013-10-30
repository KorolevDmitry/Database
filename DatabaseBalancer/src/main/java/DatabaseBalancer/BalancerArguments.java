package DatabaseBalancer;

import DatabaseBase.interfaces.INameUsageDescriptionPattern;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/29/13
 * Time: 9:36 PM
 * To change this template use File | Settings | File Templates.
 */
public enum BalancerArguments implements INameUsageDescriptionPattern {
    Help ("--help", "--help", "Print description of acceptable arguments"),
    Port ("--port", "--port {port}", "Port number to listen requests."),
    Directory ("--directory", "--directory {directory}", "Directory for storing files of balancer.");

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

    BalancerArguments(String parameterName, String usage, String description) {
        _name = parameterName;
        _usage = usage;
        _description = description;
    }
}
