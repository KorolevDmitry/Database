package DatabaseClient;

import DatabaseBase.components.StaticBalancer;
import DatabaseBase.entities.EvaluationResult;
import DatabaseBase.entities.StringSizable;
import DatabaseBase.interfaces.IBalancer;
import DatabaseBase.interfaces.INameUsageDescriptionPattern;
import DatabaseBase.parser.Lexer;
import DatabaseBase.parser.Parser;
import DatabaseBase.parser.ParserStringString;
import DatabaseBase.utils.ArgumentsHelper;
import DatabaseClient.api.ClientEvaluator;
import DatabaseBase.components.TcpSender;
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


    public static void main(String args[]) {
        String sentence;
        String answer;
        IBalancer balancer;
        ClientEvaluator<StringSizable, StringSizable> evaluator;
        Parser parser;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        try {
            HashMap<INameUsageDescriptionPattern, String> arguments = ArgumentsHelper.ParseArguments(args, ClientArguments.values());
            String listOfServers = ArgumentsHelper.GetStringArgument(arguments, ClientArguments.LIST_OF_SERVERS);
            balancer = new StaticBalancer(listOfServers);
            TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
            parser = new ParserStringString(new Lexer());
            evaluator = new ClientEvaluator<StringSizable, StringSizable>(sender, parser, balancer);
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
                } else if (result.HasError) {
                    System.out.println(result.ErrorDescription);
                } else if (result.HasReturnResult) {
                    System.out.println(result.Result == null ? "There is no such element" : result.Result.Value);
                } else {
                    System.out.println("Done");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
