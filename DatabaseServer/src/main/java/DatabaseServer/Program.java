package DatabaseServer;

import DatabaseBase.DatabaseServer.utils.ArgumentsHelper;
import DatabaseBase.interfaces.IDataStorage;
import DatabaseBase.interfaces.INameUsageDescriptionPattern;
import DatabaseClient.parser.Lexer;
import DatabaseServer.api.ServerEvaluator;
import DatabaseServer.api.TcpListener;
import DatabaseServer.dataStorage.CombinedDataStorage;
import DatabaseServer.dataStorage.DataStorageType;
import DatabaseServer.dataStorage.FileBasedDataStorage;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;
import DatabaseServer.parser.ServerParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/19/13
 * Time: 10:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class Program {

    private static Random random = new Random();

    private static void PrintMainHelp() {
        System.out.println("You can use program with next arguments:");
        ArgumentsHelper.PrintDescription(ServerArguments.values());
    }

    private static IDataStorage<String, String> InitDatabase(HashMap<INameUsageDescriptionPattern, String> arguments) throws IOException, ClassNotFoundException {
        IDataStorage<String, String> storage = null;
        if (!arguments.containsKey(ServerArguments.Mode))
            throw new IllegalArgumentException("Missed required argument: " + ServerArguments.Mode.GetName());
        DataStorageType dataStorageType;
        try {
            dataStorageType = DataStorageType.valueOf(arguments.get(ServerArguments.Mode).toUpperCase());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Unrecognised mode: " + arguments.get(ServerArguments.Mode));
        }
        switch (dataStorageType) {
            case MEMORY:
                storage = new MemoryBasedDataStorage<String, String>();
                break;
            case FILE:
                String fileBaseDirectory = ArgumentsHelper.GetStringArgument(arguments, ServerArguments.Directory);
                int fileSplitSize = ArgumentsHelper.GetPositiveIntArgument(arguments, ServerArguments.SplitRate);
                int port = ArgumentsHelper.GetPositiveIntArgument(arguments, ServerArguments.Port);
                String prefix = "_fileStorage" + port;
                storage = new FileBasedDataStorage<String, String>(fileBaseDirectory, prefix, fileSplitSize);
                break;
            case COMBINED:
                fileBaseDirectory = ArgumentsHelper.GetStringArgument(arguments, ServerArguments.Directory);
                fileSplitSize = ArgumentsHelper.GetPositiveIntArgument(arguments, ServerArguments.SplitRate);
                int memoryMaxSize = ArgumentsHelper.GetPositiveIntArgument(arguments, ServerArguments.MemoryMaxSize);
                port = ArgumentsHelper.GetPositiveIntArgument(arguments, ServerArguments.Port);
                prefix = "_fileStorage" + port;
                storage = new CombinedDataStorage<String, String>(fileBaseDirectory, prefix, fileSplitSize, memoryMaxSize);
                break;
        }
        return storage;
    }

    private static void InitTestData(IDataStorage<String, String> database, HashMap<INameUsageDescriptionPattern, String> arguments) throws IOException {
        if (!arguments.containsKey(ServerArguments.GenerateElementsCount) && !arguments.containsKey(ServerArguments.GenerateValueSize))
            return;
        int elementsCount = ArgumentsHelper.GetPositiveIntArgument(arguments, ServerArguments.GenerateElementsCount);
        int valueSize = ArgumentsHelper.GetPositiveIntArgument(arguments, ServerArguments.GenerateValueSize);
        if (valueSize > 0 && elementsCount > 0) {
            System.out.println("Initializing " + elementsCount + " elements...");
            Random random = new Random();
            byte[] r = new byte[valueSize];
            for (int i = 0; i < elementsCount; i++) {
                String s = String.valueOf(i);
                database.AddOrUpdate(s, GenerateStringAllCharacters(valueSize));
                if ((i + 1) % 100 == 0)
                    System.out.println((i + 1) + " of " + elementsCount + " items generated.");
            }
            System.out.println("Initialized.");
        }
    }

    private static String GenerateStringAllCharacters(int countOfBytes) {
        byte[] bytes = new byte[countOfBytes];
        for (int i = 0; i < countOfBytes; i++) {
            bytes[i] = (byte) (i % 256);
        }
        String str = new String(bytes);
        str = str.replace("\0", "");

        return str;
    }

    public static void main(String[] args) throws IOException {
        IDataStorage<String, String> storage = null;
        try {
            HashMap<INameUsageDescriptionPattern, String> arguments = ArgumentsHelper.ParseArguments(args, ServerArguments.values());
            int port = ArgumentsHelper.GetPositiveIntArgument(arguments, ServerArguments.Port);
            storage = InitDatabase(arguments);
            System.out.println("Database initialized.");
            InitTestData(storage, arguments);
            ServerParser parser = new ServerParser(new Lexer());
            ServerEvaluator<String, String> evaluator = new ServerEvaluator<String, String>(storage, parser);
            TcpListener<String, String> listener = new TcpListener<String, String>(evaluator, port);
            listener.Start();
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
            PrintMainHelp();
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            PrintMainHelp();
        }
        finally {
            if(storage != null)
            {
                storage.Close();
            }
        }
    }
}
