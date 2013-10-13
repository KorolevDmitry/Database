import api.Evaluator;
import dataStorage.CombinedDataStorage;
import dataStorage.DataStorageType;
import dataStorage.FileBasedDataStorage;
import dataStorage.MemoryBasedDataStorage;
import exceptions.EvaluateException;
import exceptions.LexerException;
import exceptions.ParserException;
import interfaces.IDataStorage;
import interfaces.INameUsageDescriptionPattern;
import parser.Lexer;
import parser.ParsedTree;
import parser.Parser;
import utils.ArgumentsHelper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
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

    private static void PrintMainHelp() {
        System.out.println("You can use program with next arguments:");
        ArgumentsHelper.PrintDescription(ServerArguments.values());
    }

    private static IDataStorage<String, String> InitDatabase(HashMap<INameUsageDescriptionPattern, String> arguments) {
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

    private static void InitTestData(IDataStorage<String, String> database, HashMap<INameUsageDescriptionPattern, String> arguments) {
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
                random.nextBytes(r);
                database.AddOrUpdate(s, new String(r));
                if ((i + 1) % 100 == 0)
                    System.out.println((i + 1) + " of " + elementsCount + " items generated.");
            }
            System.out.println("Initialized.");
        }
    }

    public static void main(String[] args) throws IOException {
        String clientSentence;
        String capitalizedSentence = null;
        ServerSocket welcomeSocket;
        IDataStorage<String, String> storage;
        try {
            HashMap<INameUsageDescriptionPattern, String> arguments = ArgumentsHelper.ParseArguments(args, ServerArguments.values());
            int port = ArgumentsHelper.GetPositiveIntArgument(arguments, ServerArguments.Port);
            storage = InitDatabase(arguments);
            System.out.println("Database initialized.");
            InitTestData(storage, arguments);
            Parser parser = new Parser(new Lexer());
            Evaluator evaluator = new Evaluator(storage);
            welcomeSocket = new ServerSocket(port);
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                clientSentence = inFromClient.readLine();
                if (clientSentence == null || clientSentence.isEmpty())
                    continue;
                System.out.println("Received: " + clientSentence);
                try {
                    ParsedTree parsedTree = parser.Parse(clientSentence);
                    Object value = evaluator.Evaluate(parsedTree);
                    capitalizedSentence = value == null ? "Done" : value.toString() + '\n';
                } catch (LexerException e) {
                    capitalizedSentence = e.toString();
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (ParserException e) {
                    capitalizedSentence = e.toString();
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (EvaluateException e) {
                    capitalizedSentence = e.toString();
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InvalidKeyException e) {
                    capitalizedSentence = e.toString();
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } finally {
                    if (outToClient != null && capitalizedSentence != null) {
                        outToClient.writeBytes(capitalizedSentence);
                        outToClient.close();
                    }
                }
            }
        } catch (IllegalArgumentException exception)
        {
            System.out.println(exception.getMessage());
            PrintMainHelp();
        }
    }
}
