package api;

import components.Balancer;
import entities.EvaluationResult;
import entities.Query;
import entities.Route;
import exceptions.EvaluateException;
import interfaces.IEvaluator;
import parser.ServerCommand;
import parser.commands.CommandSingleNode;
import parser.commands.RequestCommand;
import utils.ArgumentsHelper;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 8:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientEvaluator<TKey, TValue> implements IEvaluator {
    private Balancer<TKey, TValue> _balancer;

    public ClientEvaluator(Balancer<TKey, TValue> balancer) {
        _balancer = balancer;
    }

    @Override
    public EvaluationResult Evaluate(Query tree) {
        EvaluationResult<TKey, TValue> evaluationResult = new EvaluationResult<TKey, TValue>();
        evaluationResult.ExecutionQuery = tree;
        evaluationResult.HasReturnResult = true;
        try {
            if (tree.Command instanceof ServerCommand)
                Evaluate((ServerCommand<TKey>) tree.Command, evaluationResult);
            else if (tree.Command instanceof CommandSingleNode) {
                Evaluate((CommandSingleNode) tree.Command, evaluationResult);
                evaluationResult.HasReturnResult = false;
            }
        } catch (EvaluateException evaluateException) {
            evaluationResult.HasReturnResult = false;
            evaluationResult.HasError = true;
            evaluationResult.ErrorDescription = evaluateException.getMessage();
        }

        return evaluationResult;
    }

    private void Evaluate(CommandSingleNode command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        evaluationResult.HasReturnResult = false;
        switch (command.GetCommand()) {
            case HELP:
                PrintHelp();
                break;
            case QUIT:
                evaluationResult.Quit = true;
                break;
            default:
                throw new EvaluateException();
        }
    }

    private void Evaluate(ServerCommand<TKey> command, EvaluationResult<TKey, TValue> evaluationResult) throws EvaluateException {
        evaluationResult.HasReturnResult = false;
        Socket clientSocket = null;

        try {
            Route route = _balancer.GetRoute(command);
            clientSocket = new Socket(route.Address, route.Port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());
            outToServer.writeBytes(command.toString() + '\n');
            EvaluationResult<TKey, TValue> serverEvaluationResult = (EvaluationResult<TKey, TValue>) inFromServer.readObject();
            if (serverEvaluationResult == null)
                throw new EvaluateException();
            evaluationResult.Result = serverEvaluationResult.Result;
            evaluationResult.HasReturnResult = serverEvaluationResult.HasReturnResult;
            evaluationResult.HasError = serverEvaluationResult.HasError;
            evaluationResult.ErrorDescription = serverEvaluationResult.ErrorDescription;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new EvaluateException();
        } catch (IOException e) {
            e.printStackTrace();
            throw new EvaluateException();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new EvaluateException();
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

    private static void PrintHelp() {
        System.out.println("You can use next commands:");
        ArgumentsHelper.PrintDescription(RequestCommand.values());
    }
}
