package DatabaseClient;

import DatabaseBase.components.TcpSender;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.Route;
import DatabaseBase.entities.ServerRole;
import DatabaseBase.entities.StringSizable;
import DatabaseBase.interfaces.INameUsageDescriptionPattern;
import DatabaseBase.parser.Lexer;
import DatabaseBase.parser.Parser;
import DatabaseBase.parser.ParserStringString;
import DatabaseBase.utils.ArgumentsHelper;
import DatabaseClient.api.ClientEvaluator;
import DatabaseClient.utils.ClientArguments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private static void InitDefaultParameters(HashMap<INameUsageDescriptionPattern, String> arguments){
        if(!arguments.containsKey(ClientArguments.NUMBER_OF_ROUTES_TO_READ)){
            arguments.put(ClientArguments.NUMBER_OF_ROUTES_TO_READ, "1");
        }
        if(!arguments.containsKey(ClientArguments.NUMBER_OF_ROUTES_TO_WRITE)){
            arguments.put(ClientArguments.NUMBER_OF_ROUTES_TO_WRITE, "1");
        }
    }

    public static void main(String args[]) {
        String sentence;
        String answer;
        ClientEvaluator<StringSizable, StringSizable> evaluator;
        Parser parser;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        try {
            HashMap<INameUsageDescriptionPattern, String> arguments = ArgumentsHelper.ParseArguments(args, ClientArguments.values());
            String listOfServers = ArgumentsHelper.GetStringArgument(arguments, ClientArguments.LIST_OF_SERVERS);
            int numberToRead = ArgumentsHelper.GetPositiveIntArgument(arguments, ClientArguments.NUMBER_OF_ROUTES_TO_READ);
            int numberToWrite = ArgumentsHelper.GetPositiveIntArgument(arguments, ClientArguments.NUMBER_OF_ROUTES_TO_WRITE);
            //balancer = new StaticBalancer(listOfServers);
            Route balancer = new Route(listOfServers, ServerRole.MASTER, null);
            TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
            parser = new ParserStringString(new Lexer());
            evaluator = new ClientEvaluator<StringSizable, StringSizable>(sender, parser, balancer, numberToRead, numberToWrite);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
            PrintMainHelp();
            return;
        }
        while (true) {
            try {
                sentence = inFromUser.readLine();
                EvaluationResult<StringSizable, StringSizable> result = evaluator.Evaluate(sentence);
                if (result.Quit) {
                    return;
                }
                PrintResult(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void PrintResult(EvaluationResult<StringSizable, StringSizable> result){
        if (result.HasError) {
            System.out.println(result.ErrorDescription);
        } else if (result.HasReturnResult) {
            if (result.HasBalancerResult) {
                if (result.ServiceResult.Servers != null) {
                    for (int i = 0; i < result.ServiceResult.Servers.size(); i++) {
                        System.out.println(result.ServiceResult.Servers.get(i));
                        for (int j = 0; j < result.ServiceResult.Servers.get(i).Slaves.size(); j++) {
                            System.out.println("\t" + result.ServiceResult.Servers.get(i).Slaves.get(j));
                        }
                    }
                } else {
                    System.out.println(result.ServiceResult.Index);
                }
            } else {
                if(result.Result == null || result.Result.isEmpty()){
                    System.out.println("There is no such elements");
                }
                for(int i = 0 ; i<result.Result.size();i++){
                    System.out.println(result.Result.get(i).Key + " " + result.Result.get(i).Value);
                }
            }
        } else {
            System.out.println("Done");
        }
    }
}
