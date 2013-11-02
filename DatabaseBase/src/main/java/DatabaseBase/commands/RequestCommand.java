package DatabaseBase.commands;

import DatabaseBase.interfaces.INameUsageDescriptionPattern;

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
    QUIT ("quit", "quit" , "Quit."),
    ADD_SERVER("add_server", "add_server \"{serverHost}\" {serverRole} {master}", "Add serverHost to cluster. ServerHost - server:port, ServerRole - master/slave, MASTER - serverHost of master node if it is slave."),
    REMOVE_SERVER("remove_server", "remove_server \"{serverHost}\"", "Remove serverHost from cluster. If it was master - master will be one of slaves."),
    GET_SERVERS_LIST("get_servers_list", "get_servers_list", "Return cluster configuration"),
    PING("ping", "ping \"{serverHost}\"", "Check if serverHost is alive"),
    REPLICATE("replicate", "replicate \"{serverHostFrom}\" \"{serverHostTo}\" {startIndex} {endIndex} {removeAfterReplicationCompleted}", "Replicate data"),
    UPDATE_SERVER("update_server", "update_server \"{serverHost}\"", "Force update server info about cluster");

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
