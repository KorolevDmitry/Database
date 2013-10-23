import api.ClientEvaluator;
import components.Balancer;
import entities.EvaluationResult;
import entities.Query;
import exceptions.LexerException;
import exceptions.ParserException;
import interfaces.INameUsageDescriptionPattern;
import parser.ClientParser;
import parser.Lexer;
import parser.Parser;
import utils.ArgumentsHelper;

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
        Balancer<String, String> balancer;
        ClientEvaluator<String, String> evaluator;
        Parser parser;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        try {
            HashMap<INameUsageDescriptionPattern, String> arguments = ArgumentsHelper.ParseArguments(args, ClientArguments.values());
            String listOfServers = ArgumentsHelper.GetStringArgument(arguments, ClientArguments.LIST_OF_SERVERS);
            balancer = new Balancer<String, String>(listOfServers);
            evaluator = new ClientEvaluator<String, String>(balancer);
            parser = new ClientParser(new Lexer());
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
            PrintMainHelp();
            return;
        }
        while (true) {
            try {
                sentence = inFromUser.readLine();
                Query tree = parser.Parse(sentence);
                EvaluationResult result = evaluator.Evaluate(tree);
                if (result.Exit)
                {
                    return;
                }
                else if(result.HasError)
                {
                    System.out.println(result.ErrorDescription);
                }
                else if(result.HasReturnResult)
                {
                    System.out.println(result.Result.toString());
                }
                else
                {
                    System.out.println("Done");
                }
            } catch (LexerException e) {
                System.out.println("Unrecognized lexem");
                e.printStackTrace();
            } catch (ParserException e) {
                System.out.println("Wrong command");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
