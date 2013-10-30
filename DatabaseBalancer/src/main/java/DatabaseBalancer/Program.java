package DatabaseBalancer;

import DatabaseBalancer.api.BalancerEvaluator;
import DatabaseBase.components.ServiceResult;
import DatabaseBase.components.TcpListener;
import DatabaseBase.components.TcpSender;
import DatabaseBase.entities.StringSizable;
import DatabaseBase.interfaces.IBalancer;
import DatabaseBase.interfaces.INameUsageDescriptionPattern;
import DatabaseBase.parser.Lexer;
import DatabaseBase.parser.ParserStringString;
import DatabaseBase.utils.ArgumentsHelper;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/29/13
 * Time: 7:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class Program {
    private static void PrintMainHelp() {
        System.out.println("You can use program with next arguments:");
        ArgumentsHelper.PrintDescription(BalancerArguments.values());
    }

    public static void main(String[] args) throws IOException {
        TcpListener<StringSizable, StringSizable> listener = null;
        IBalancer balancer = null;
        try {
            HashMap<INameUsageDescriptionPattern, String> arguments = ArgumentsHelper.ParseArguments(args, BalancerArguments.values());
            int port = ArgumentsHelper.GetPositiveIntArgument(arguments, BalancerArguments.Port);
            balancer = InitBalancer(arguments);
            ParserStringString parser = new ParserStringString(new Lexer());
            BalancerEvaluator<StringSizable, StringSizable> evaluator = new BalancerEvaluator<StringSizable, StringSizable>(balancer, parser);
            listener = new TcpListener<StringSizable, StringSizable>(evaluator, port);
            listener.Start();
            System.out.println("Balancer initialized.");
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
            PrintMainHelp();
        } finally {
            if(listener != null)
            {
                listener.Stop();
            }
        }
    }

    private static IBalancer InitBalancer(HashMap<INameUsageDescriptionPattern, String> arguments) {
        TcpSender<StringSizable, ServiceResult> tcpSender = new TcpSender<StringSizable, ServiceResult>();
        return new DynamicBalancer(tcpSender, 0);
    }
}
