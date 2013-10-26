package DatabaseBase.DatabaseServer.utils;

import DatabaseBase.interfaces.INameUsageDescriptionPattern;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/13/13
 * Time: 10:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class ArgumentsHelper {
    public static void PrintDescription(INameUsageDescriptionPattern[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            System.out.println("\t" + arguments[i].GetUsage() + " - " + arguments[i].GetDescription());
        }
    }

    public static HashMap<INameUsageDescriptionPattern, String> ParseArguments(String[] args, INameUsageDescriptionPattern[] acceptableArguments) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        HashMap<String, INameUsageDescriptionPattern> dictionaryOfAcceptableArguments = new HashMap<String, INameUsageDescriptionPattern>(acceptableArguments.length);
        for (int i = 0; i < acceptableArguments.length; i++) {
            dictionaryOfAcceptableArguments.put(acceptableArguments[i].GetName(), acceptableArguments[i]);
        }
        HashMap<INameUsageDescriptionPattern, String> arguments = new HashMap<INameUsageDescriptionPattern, String>(args.length / 2);
        for (int i = 0; i < args.length; i += 2) {
            if (!dictionaryOfAcceptableArguments.containsKey(args[i]))
                throw new IllegalArgumentException("Unrecognized argument: " + args[i]);
            INameUsageDescriptionPattern acceptedArgument = dictionaryOfAcceptableArguments.get(args[i]);
            if (dictionaryOfAcceptableArguments.containsKey(acceptedArgument))
                throw new IllegalArgumentException("Double argument definition: " + args[i]);
            arguments.put(acceptedArgument, args[i + 1]);
        }

        return arguments;
    }

    public static int GetPositiveIntArgument(HashMap<INameUsageDescriptionPattern, String> arguments, INameUsageDescriptionPattern argument) {
        if (!arguments.containsKey(argument))
            throw new IllegalArgumentException("Missed required argument: " + argument.GetName());
        int parsedValue;
        try {
            parsedValue = Integer.parseInt(arguments.get(argument));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to parse parameter: " + argument.GetName());
        }
        if (parsedValue <= 0)
            throw new IllegalArgumentException("Value should be positive: " + argument.GetName());

        return parsedValue;
    }

    public static String GetStringArgument(HashMap<INameUsageDescriptionPattern, String> arguments, INameUsageDescriptionPattern argument) {
        if (!arguments.containsKey(argument))
            throw new IllegalArgumentException("Missed required argument: " + argument.GetName());
        return arguments.get(argument);
    }
}
