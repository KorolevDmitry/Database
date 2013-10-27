package DatabaseServer.utils;

import DatabaseBase.interfaces.INameUsageDescriptionPattern;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/13/13
 * Time: 8:03 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ServerArguments implements INameUsageDescriptionPattern {
    Help ("--help", "--help", "Print description of acceptable arguments"),
    Port ("--port", "--port {port}", "Port number to listen requests."),
    Mode ("--mode", "--mode {mode}", "Opportunity to run server in different modes: " +
            "\n\t\tMemory - to store values only in memory." +
            "\n\t\tFile - to store values only in file storage." +
            "\n\t\tCombined - to store values only in combined storage mode."),
    Directory ("--directory", "--directory {directory}", "Directory for storing files of database. Applied only for Memory and Combined modes."),
    SplitRate ("--split-rate", "--split-rate {split-rate}", "Count of files to store database in. Applied only for Memory and Combined modes."),
    MemoryMaxSize ("--memory-max-size", "--memory-max-size {memory-max-size}", "Size of database in bytes that will be cached in memory. Applied only for Combined mode."),
    GenerateElementsCount ("--generate-elements-count", "--generate-elements-count {generate-elements-count}", "Count of sample items to generate (key is integer from 0 till count-1)."),
    GenerateValueSize("--generate-value_size", "--generate-value_size {generate-value_size}", "Size of values in bytes to generate.");

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

    ServerArguments(String parameterName, String usage, String description) {
        _name = parameterName;
        _usage = usage;
        _description = description;
    }
}
