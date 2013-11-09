import DatabaseBalancer.DynamicBalancer;
import DatabaseBalancer.api.BalancerEvaluator;
import DatabaseBase.commands.CommandKeyNode;
import DatabaseBase.commands.CommandKeyValueNode;
import DatabaseBase.commands.RequestCommand;
import DatabaseBase.commands.service.ReplicateCommand;
import DatabaseBase.commands.service.ServiceCommand;
import DatabaseBase.components.TcpListener;
import DatabaseBase.components.TcpSender;
import DatabaseBase.entities.*;
import DatabaseBase.exceptions.BalancerException;
import DatabaseBase.interfaces.IBalancer;
import DatabaseBase.parser.Lexer;
import DatabaseBase.parser.ParserStringString;
import DatabaseClient.api.ClientEvaluator;
import DatabaseServer.api.ServerEvaluator;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 11/4/13
 * Time: 8:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientServerReplicationTests {

    @Test
    public void AddServer_SomeValuesReplicated_NoOneLost() throws IOException, BalancerException, InterruptedException {
        //arrange
        Route routeServer1 = new Route("localhost:1107", ServerRole.MASTER, null);
        Route routeServer2 = new Route("localhost:1108", ServerRole.MASTER, null);
        Route routeBalancer = new Route("localhost:1111", ServerRole.MASTER, null);
        TcpListener<StringSizable, StringSizable> server1 = CreateServer(routeServer1);
        TcpListener<StringSizable, StringSizable> server2 = CreateServer(routeServer2);
        TcpListener<StringSizable, StringSizable> balancer = CreateBalancer(routeBalancer);
        ClientEvaluator<StringSizable, StringSizable> client1 = CreateClient(routeBalancer);
        server1.Start();
        server2.Start();
        balancer.Start();
        Query queryAddServer1 = new Query();
        queryAddServer1.Command = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer1);
        Query queryAddServer2 = new Query();
        queryAddServer2.Command = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer2);

        Query query2 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> command2 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x2"), new StringSizable("x2"));
        query2.Command = command2;
        Query query3 = new Query();
        CommandKeyNode<StringSizable> command3 = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x1"));
        query3.Command = command3;
        EvaluationResult<StringSizable, StringSizable> resultAddServer1 = null;
        EvaluationResult<StringSizable, StringSizable> resultAddServer2 = null;
        resultAddServer1 = client1.Evaluate(queryAddServer1);
        Thread.sleep(1000);
        for(int i = 0;i<1000;i++){
            Query queryAdd = new Query();
            queryAdd.Command = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("key" + i), new StringSizable("value" + i));
            client1.Evaluate(queryAdd);
        }

        //act
        resultAddServer2 = client1.Evaluate(queryAddServer2);
        Thread.sleep(1000);

        //assert
        assertFalse(resultAddServer1.HasError);
        assertFalse(resultAddServer2.HasError);
        for(int i = 0;i<1000;i++){
            Query queryGet = new Query();
            queryGet.Command = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("key" + i));
            EvaluationResult<StringSizable, StringSizable> resultGet = client1.Evaluate(queryGet);
            assertFalse(resultGet.ErrorDescription, resultGet.HasError);
            assertEquals(new StringSizable("value" + i), resultGet.Result);
        }

        server1.Stop();
        server2.Stop();
        balancer.Stop();
    }

    @Test
    public void RemoveServer_SomeValuesReplicated_NoOneLost() throws IOException, BalancerException, InterruptedException {
        //arrange
        Route routeServer1 = new Route("localhost:1107", ServerRole.MASTER, null);
        Route routeServer2 = new Route("localhost:1108", ServerRole.MASTER, null);
        Route routeBalancer = new Route("localhost:1111", ServerRole.MASTER, null);
        TcpListener<StringSizable, StringSizable> server1 = CreateServer(routeServer1);
        TcpListener<StringSizable, StringSizable> server2 = CreateServer(routeServer2);
        TcpListener<StringSizable, StringSizable> balancer = CreateBalancer(routeBalancer);
        ClientEvaluator<StringSizable, StringSizable> client1 = CreateClient(routeBalancer);
        server1.Start();
        server2.Start();
        balancer.Start();
        Query queryAddServer1 = new Query();
        queryAddServer1.Command = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer1);
        Query queryAddServer2 = new Query();
        queryAddServer2.Command = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer2);
        Query queryRemoveServer2 = new Query();
        queryRemoveServer2.Command = new ServiceCommand(RequestCommand.REMOVE_SERVER, routeServer2);

        Query query2 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> command2 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x2"), new StringSizable("x2"));
        query2.Command = command2;
        Query query3 = new Query();
        CommandKeyNode<StringSizable> command3 = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x1"));
        query3.Command = command3;
        EvaluationResult<StringSizable, StringSizable> resultAddServer1 = null;
        EvaluationResult<StringSizable, StringSizable> resultAddServer2 = null;
        EvaluationResult<StringSizable, StringSizable> resultRemoveServer = null;
        resultAddServer1 = client1.Evaluate(queryAddServer1);
        Thread.sleep(1000);
        resultAddServer2 = client1.Evaluate(queryAddServer2);
        Thread.sleep(1000);
        for(int i = 0;i<1000;i++){
            Query queryAdd = new Query();
            queryAdd.Command = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("key" + i), new StringSizable("value" + i));
            client1.Evaluate(queryAdd);
        }

        //act
        resultRemoveServer = client1.Evaluate(queryRemoveServer2);
        Thread.sleep(1000);

        //assert
        assertFalse(resultAddServer1.HasError);
        assertFalse(resultAddServer2.HasError);
        assertFalse(resultRemoveServer.HasError);
        for(int i = 0;i<1000;i++){
            Query queryGet = new Query();
            queryGet.Command = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("key" + i));
            EvaluationResult<StringSizable, StringSizable> resultGet = client1.Evaluate(queryGet);
            assertFalse(resultGet.ErrorDescription, resultGet.HasError);
            assertEquals(new StringSizable("value" + i), resultGet.Result);
        }

        server1.Stop();
        server2.Stop();
        balancer.Stop();
    }

    @Test
    public void PartialReplication_SomeValuesReplicated_NoOneLost() throws IOException, BalancerException, InterruptedException {
        //arrange
        Route routeServer1 = new Route("localhost:1107", ServerRole.MASTER, null);
        Route routeServer2 = new Route("localhost:1108", ServerRole.MASTER, null);
        Route routeBalancer = new Route("localhost:1111", ServerRole.MASTER, null);
        TcpListener<StringSizable, StringSizable> server1 = CreateServer(routeServer1);
        TcpListener<StringSizable, StringSizable> server2 = CreateServer(routeServer2);
        TcpListener<StringSizable, StringSizable> balancer = CreateBalancer(routeBalancer);
        ClientEvaluator<StringSizable, StringSizable> client1 = CreateClient(routeBalancer);
        server1.Start();
        server2.Start();
        balancer.Start();
        Query queryAddServer1 = new Query();
        queryAddServer1.Command = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer1);
        Query queryAddServer2 = new Query();
        queryAddServer2.Command = new ServiceCommand(RequestCommand.ADD_SERVER, routeServer2);

        Query query2 = new Query();
        CommandKeyValueNode<StringSizable, StringSizable> command2 = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("x2"), new StringSizable("x2"));
        query2.Command = command2;
        Query query3 = new Query();
        CommandKeyNode<StringSizable> command3 = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("x1"));
        query3.Command = command3;
        EvaluationResult<StringSizable, StringSizable> resultAddServer1 = null;
        EvaluationResult<StringSizable, StringSizable> resultAddServer2 = null;
        EvaluationResult<StringSizable, StringSizable> resultGetServersList = null;
        EvaluationResult<StringSizable, StringSizable> resultReplicate = null;
        resultAddServer1 = client1.Evaluate(queryAddServer1);
        Thread.sleep(1000);
        resultAddServer2 = client1.Evaluate(queryAddServer2);
        Thread.sleep(1000);
        for(int i = 0;i<1000;i++){
            Query queryAdd = new Query();
            queryAdd.Command = new CommandKeyValueNode<StringSizable, StringSizable>(RequestCommand.ADD, new StringSizable("key" + i), new StringSizable("value" + i));
            client1.Evaluate(queryAdd);
        }
        Query queryGetServersList = new Query();
        queryGetServersList.Command = new ServiceCommand(RequestCommand.GET_SERVERS_LIST, null);
        resultGetServersList = client1.Evaluate(queryGetServersList);
        List<Route> servers = resultGetServersList.ServiceResult.Servers;
        Query queryReplicate = new Query();
        queryReplicate.Command = new ReplicateCommand(servers.get(0), servers.get(1), servers.get(0).getStartIndex(), servers.get(0).getEndIndex() - 1, false);


        //act
        resultReplicate = client1.Evaluate(queryReplicate);
        Thread.sleep(1000);

        //assert
        assertFalse(resultAddServer1.HasError);
        assertFalse(resultAddServer2.HasError);
        assertFalse(resultGetServersList.HasError);
        assertFalse(resultReplicate.HasError);
        for(int i = 0;i<1000;i++){
            Query queryGet = new Query();
            queryGet.Command = new CommandKeyNode<StringSizable>(RequestCommand.GET, new StringSizable("key" + i));
            EvaluationResult<StringSizable, StringSizable> resultGet = client1.Evaluate(queryGet);
            assertFalse(resultGet.ErrorDescription, resultGet.HasError);
            assertEquals(new StringSizable("value" + i), resultGet.Result);
        }

        server1.Stop();
        server2.Stop();
        balancer.Stop();
    }

    private TcpListener<StringSizable, StringSizable> CreateServer(Route route) throws IOException {
        MemoryBasedDataStorage<StringSizable, StringSizable> dataStorage = new MemoryBasedDataStorage<StringSizable, StringSizable>();
        TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
        ServerEvaluator<StringSizable, StringSizable> evaluator =
                new ServerEvaluator<StringSizable, StringSizable>(dataStorage,
                        new ParserStringString(new Lexer()), route, sender);
        TcpListener<StringSizable, StringSizable> listener = new TcpListener<StringSizable, StringSizable>(evaluator, route.Port);
        return listener;
    }

    private TcpListener<StringSizable, StringSizable> CreateBalancer(Route route) throws IOException {
        TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
        IBalancer balancer = new DynamicBalancer(sender, 500);
        BalancerEvaluator<StringSizable, StringSizable> evaluator = new BalancerEvaluator<StringSizable, StringSizable>(balancer, new ParserStringString(new Lexer()));
        TcpListener<StringSizable, StringSizable> listener = new TcpListener<StringSizable, StringSizable>(evaluator, route.Port);
        return listener;
    }

    private ClientEvaluator<StringSizable, StringSizable> CreateClient(Route balancer) {
        TcpSender<StringSizable, StringSizable> sender = new TcpSender<StringSizable, StringSizable>();
        ClientEvaluator<StringSizable, StringSizable> client = new ClientEvaluator<StringSizable, StringSizable>(sender,
                new ParserStringString(new Lexer()), balancer);
        return client;
    }
}
