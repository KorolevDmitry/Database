import entities.Route;
import components.Balancer;
import exceptions.LexerException;
import exceptions.ParserException;
import interfaces.INameUsageDescriptionPattern;
import parser.*;
import parser.nodes.RequestCommand;
import utils.ArgumentsHelper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/10/13
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Program {

    private static void PrintMainHelp() {
        System.out.println("You can use program with next arguments:");
        ArgumentsHelper.PrintDescription(ClientArguments.values());
    }

    private static void PrintHelp() {
        System.out.println("You can use next commands:");
        ArgumentsHelper.PrintDescription(RequestCommand.values());
    }

    public static void main(String args[]) {
        String sentence;
        String answer;
        Balancer<String, String> balancer;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = null;
        try {
            HashMap<INameUsageDescriptionPattern, String> arguments = ArgumentsHelper.ParseArguments(args, ClientArguments.values());
            String listOfServers = ArgumentsHelper.GetStringArgument(arguments, ClientArguments.LIST_OF_SERVERS);
            balancer = new Balancer<String, String>(listOfServers);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
            PrintMainHelp();
            return;
        }
        PrintHelp();
        while (true) {
            try {
                Parser parser = new Parser(new Lexer());
                sentence = inFromUser.readLine();
                ParsedTree tree = parser.Parse(sentence);
                if (tree.Command.GetCommand() == RequestCommand.QUIT)
                    break;
                if (tree.Command.GetCommand() == RequestCommand.HELP) {
                    PrintHelp();
                    continue;
                }
                Route route = balancer.GetRoute(tree);
                clientSocket = new Socket(route.Address, route.Port);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outToServer.writeBytes(tree.toString() + '\n');
                answer = inFromServer.readLine();
                System.out.println("FROM SERVER: " + answer);
            } catch (LexerException e) {
                System.out.println("Unrecognized lexem");
                //e.printStackTrace();
            } catch (ParserException e) {
                System.out.println("Wrong command");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (clientSocket != null) {
                    try {
                        clientSocket.shutdownOutput();
                        clientSocket.shutdownInput();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }
    }
}
