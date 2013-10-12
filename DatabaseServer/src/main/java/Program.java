import api.Evaluator;
import dataStorage.CombinedDataStorage;
import dataStorage.DataStorageType;
import dataStorage.FileBasedDataStorage;
import dataStorage.MemoryBasedDataStorage;
import exceptions.EvaluateException;
import exceptions.LexerException;
import exceptions.ParserException;
import interfaces.IDataStorage;
import parser.*;
import parser.nodes.RequestCommand;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/19/13
 * Time: 10:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class Program {

    private static void PrintHelp() {
        System.out.println("You can use next commands:");
        System.out.println(RequestCommand.GET + " {key} - to get value from storage.");
        System.out.println(RequestCommand.ADD + " {key} {value} - to add value to storage.");
        System.out.println(RequestCommand.UPDATE + " {key} {value} - to update value from storage.");
        System.out.println(RequestCommand.DELETE + " {key} - to delete value from storage.");
        System.out.println(RequestCommand.HELP + " - to see list of accessible commands.");
        System.out.println(RequestCommand.QUIT + " - to quit.");
    }

    private static void PrintMainHelp() {
        System.out.println("Wrong usages of program. You can use program with next arguments:");
        System.out.println(DataStorageType.MEMORY + " - to store values only in memory.");
        System.out.println(DataStorageType.FILE + " {fileBaseDirectory} {fileSplitSize} - to store values only in file storage.");
        System.out.println(DataStorageType.COMBINED + " {fileBaseDirectory} {fileSplitSize} {memoryMaxSize} - to store values only in combined storage mode.");
        System.out.println("fileBaseDirectory - directory where to store files of database.");
        System.out.println("fileSplitSize - how many files will database be kept in.");
        System.out.println("memoryMaxSize - size of elements that will be kept in memory.");
    }

    private static IDataStorage<String, String> InitDatabase(String[] args, int startFrom) throws Exception {
        try {
            IDataStorage<String, String> storage = null;
            DataStorageType dataStorageType = DataStorageType.valueOf(args[startFrom].toUpperCase());
            switch (dataStorageType) {
                case MEMORY:
                    storage = new MemoryBasedDataStorage<String, String>();
                    break;
                case FILE:
                    String fileBaseDirectory = args[startFrom + 1];
                    int fileSplitSize = Integer.parseInt(args[startFrom + 2]);
                    storage = new FileBasedDataStorage<String, String>(fileBaseDirectory, fileSplitSize);
                    break;
                case COMBINED:
                    fileBaseDirectory = args[startFrom + 1];
                    fileSplitSize = Integer.parseInt(args[startFrom + 2]);
                    int memoryMaxSize = Integer.parseInt(args[startFrom + 3]);
                    storage = new CombinedDataStorage<String, String>(fileBaseDirectory, fileSplitSize, memoryMaxSize);
                    break;
            }
            return storage;
        } catch (ArrayIndexOutOfBoundsException exception) {
            PrintMainHelp();
            return null;
        }
    }

    private static void InitTestData(IDataStorage<String, String> database, String[] args) {
        DataStorageType dataStorageType = DataStorageType.valueOf(args[1].toUpperCase());
        int valueSize = 0;
        int elementsCount = 0;
        switch (dataStorageType) {
            case MEMORY:
                if (args.length >= 3) {
                    valueSize = Integer.parseInt(args[1]);
                    elementsCount = Integer.parseInt(args[2]);
                }
                break;
            case FILE:
                if (args.length >= 5) {
                    valueSize = Integer.parseInt(args[3]);
                    elementsCount = Integer.parseInt(args[4]);
                }
                break;
            case COMBINED:
                if (args.length >= 6) {
                    valueSize = Integer.parseInt(args[4]);
                    elementsCount = Integer.parseInt(args[5]);
                }
                break;
        }
        if (valueSize > 0 && elementsCount > 0) {
            System.out.println("Initializing " + elementsCount + " elements...");
            Random random = new Random();
            byte[] r = new byte[valueSize];
            for (int i = 0; i < elementsCount; i++) {
                String s = String.valueOf(i);
                random.nextBytes(r);
                database.AddOrUpdate(s, new String(r));
            }
            System.out.println("Initialized.");
        }
    }

    public static void main(String[] args) {
        String clientSentence;
        String capitalizedSentence = null;
        ServerSocket welcomeSocket = null;
        IDataStorage<String, String> storage = null;
        try {
            int port = Integer.parseInt(args[0]);
            storage = InitDatabase(args, 1);
            if (storage == null)
                return;
            System.out.println("Connection established.");
            PrintHelp();
            InitTestData(storage, args);
            Parser parser = new Parser(new Lexer());
            Evaluator evaluator = new Evaluator(storage);
            welcomeSocket = new ServerSocket(port);
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                clientSentence = inFromClient.readLine();
                if(clientSentence == null || clientSentence.isEmpty())
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
                }
                finally
                {
                    if(outToClient != null && capitalizedSentence != null)
                    {
                        outToClient.writeBytes(capitalizedSentence);
                        outToClient.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
