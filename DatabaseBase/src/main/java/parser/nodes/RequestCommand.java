package parser.nodes;

import interfaces.INameUsageDescriptionPattern;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 9:45 AM
 * To change this template use File | Settings | File Templates.
 */
public enum RequestCommand implements INameUsageDescriptionPattern {
    GET ("get", "get {key}" , "Get value from storage."),
    ADD ("add", "add {key} {value}" , "Add value to storage."),
    UPDATE ("update", "update {key} {value}" , "Update value from storage."),
    ADD_OR_UPDATE ("add_or_update", "add_or_update {key} {value}" , "Add value to storage if it is not exists and update it in another case."),
    DELETE ("delete", "delete {key}" , "Delete value from storage."),
    HELP ("help", "help" , "Print list of accessible commands."),
    QUIT ("quit", "quit" , "Quit.");

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

    RequestCommand(String parameterName, String usage, String description) {
        _name = parameterName;
        _usage = usage;
        _description = description;
    }
}
