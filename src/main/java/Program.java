
import api.Database;
import api.RequestCommand;
import dataStorage.*;

import java.io.*;
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

    private static Database<String, String> InitDatabase(String[] args) throws Exception {
        try {
            IDataStorage<String, String> storage = null;
            DataStorageType dataStorageType= DataStorageType.valueOf(args[0].toUpperCase());
            switch (dataStorageType)
            {
                case MEMORY:
                    storage = new MemoryBasedDataStorage<String, String>();
                    break;
                case FILE:
                    String fileBaseDirectory = args[1];
                    int fileSplitSize = Integer.parseInt(args[2]);
                    storage = new FileBasedDataStorage<String, String>(fileBaseDirectory, fileSplitSize);
                    break;
                case COMBINED:
                    fileBaseDirectory = args[1];
                    fileSplitSize = Integer.parseInt(args[2]);
                    int memoryMaxSize = Integer.parseInt(args[3]);
                    storage = new CombinedDataStorage<String, String>(fileBaseDirectory, fileSplitSize, memoryMaxSize);
                    break;
            }
            Database<String, String> database = new Database<String, String>(storage);
            return database;
        }
        catch (ArrayIndexOutOfBoundsException exception)
        {
            PrintMainHelp();
            return null;
        }
    }

    private static void InitTestData(Database<String, String> database, String[] args)
    {
        DataStorageType dataStorageType= DataStorageType.valueOf(args[0].toUpperCase());
        int valueSize = 0;
        int elementsCount = 0;
        switch (dataStorageType)
        {
            case MEMORY:
                if(args.length >= 3)
                {
                    valueSize = Integer.parseInt(args[1]);
                    elementsCount = Integer.parseInt(args[2]);
                }
                break;
            case FILE:
                if(args.length >= 5)
                {
                    valueSize = Integer.parseInt(args[3]);
                    elementsCount = Integer.parseInt(args[4]);
                }
                break;
            case COMBINED:
                if(args.length >= 6)
                {
                    valueSize = Integer.parseInt(args[4]);
                    elementsCount = Integer.parseInt(args[5]);
                }
                break;
        }
        if(valueSize > 0 && elementsCount > 0)
        {
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
        Database<String, String> database = null;
        try {
            database = InitDatabase(args);
            if (database == null)
                return;
            System.out.println("Connection established.");
            PrintHelp();
            InitTestData(database, args);
            System.out.println("Please enter command:");
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                boolean isQuit = false;
                String command = bufferRead.readLine();
                String[] words = command.split(" ");
                try {
                    RequestCommand requestCommand = RequestCommand.UNKNOWN;
                    if (words.length > 0) {
                        requestCommand = RequestCommand.valueOf(words[0].toUpperCase());
                    }
                    if (words.length == 0)
                        continue;
                    String name;
                    String number;
                    switch (requestCommand) {
                        case UNKNOWN:
                            System.out.println("Unrecognized command: " + words[0] + ".");
                            break;
                        case GET:
                            name = words[1];
                            String record = database.Get(name);
                            if (record != null) {
                                System.out.println(record);
                            } else {
                                System.out.println("Given name does not exists");
                            }
                            break;
                        case ADD:
                            name = words[1];
                            number = words[2];
                            database.Add(name, number);
                            break;
                        case DELETE:
                            name = words[1];
                            database.Delete(name);
                            break;
                        case UPDATE:
                            name = words[1];
                            number = words[2];
                            database.Update(name, number);
                            break;
                        case HELP:
                            PrintHelp();
                            break;
                        case QUIT:
                            isQuit = true;
                            break;
                    }
                    if (isQuit)
                        break;
                } catch (InvalidKeyException e) {
                    System.out.println("Key \"" + words[1] + "\" is invalid");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Insufficient number of parameters");
                } catch (IllegalArgumentException e) {
                    System.out.println("Unrecognized command: " + words[0] + ".");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.Close();
            }
        }
    }
}
