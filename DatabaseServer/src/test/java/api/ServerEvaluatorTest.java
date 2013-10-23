package api;

import dataStorage.MemoryBasedDataStorage;
import entities.EvaluationResult;
import entities.Query;
import interfaces.IDataStorage;
import org.junit.Before;
import org.junit.Test;
import parser.commands.CommandKeyNode;
import parser.commands.CommandKeyValueNode;
import parser.commands.CommandSingleNode;
import parser.commands.RequestCommand;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/23/13
 * Time: 11:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerEvaluatorTest {
    private ServerEvaluator<String, String> _serverEvaluator;

    @Before
    public void setUp()
    {
        IDataStorage<String, String> dataStorage = new MemoryBasedDataStorage<String, String>();
        _serverEvaluator = new ServerEvaluator<String, String>(dataStorage);
    }

    @Test
    public void Evaluate_QueryNull_HasError() {
        //arrange

        //act
        EvaluationResult<String, String> result = _serverEvaluator.Evaluate(null);

        //assert
        assertTrue(result.HasError);
    }

    @Test
    public void Evaluate_CommandNull_HasError() {
        //arrange
        Query query = new Query();

        //act
        EvaluationResult<String, String> result = _serverEvaluator.Evaluate(query);

        //assert
        assertTrue(result.HasError);
    }

    @Test
    public void Evaluate_CommandSingleNodeQuit_Quit() {
        //arrange
        Query query = new Query();
        query.Command = new CommandSingleNode(RequestCommand.QUIT);

        //act
        EvaluationResult<String, String> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        assertTrue(result.Quit);
    }

    @Test
    public void Evaluate_CommandSingleNodeHelp_Help() {
        //arrange
        Query query = new Query();
        query.Command = new CommandSingleNode(RequestCommand.HELP);

        //act
        EvaluationResult<String, String> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
    }

    @Test
    public void Evaluate_CommandSingleNodeOther_HasError() {
        //arrange
        Query query = new Query();
        query.Command = new CommandSingleNode(RequestCommand.ADD);

        //act
        EvaluationResult<String, String> result = _serverEvaluator.Evaluate(query);

        //assert
        assertTrue(result.HasError);
    }

    @Test
    public void Evaluate_CommandKeyNodeGet_NoErrorResultNull() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyNode<String>(RequestCommand.GET, "x");

        //act
        EvaluationResult<String, String> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        assertTrue(result.HasReturnResult);
        assertNull(result.Result);
    }

    @Test
    public void Evaluate_CommandKeyNodeDelete_NoErrorNoResult() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyNode<String>(RequestCommand.DELETE, "x");

        //act
        EvaluationResult<String, String> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        assertFalse(result.HasReturnResult);
    }

    @Test
    public void Evaluate_CommandKeyNodeOther_HasError() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyNode<String>(RequestCommand.ADD, "x");

        //act
        EvaluationResult<String, String> result = _serverEvaluator.Evaluate(query);

        //assert
        assertTrue(result.HasError);
    }

    @Test
    public void Evaluate_CommandKeyValueNodeAdd_NoErrorNoResult() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyValueNode<String, String>(RequestCommand.ADD, "x", "x");

        //act
        EvaluationResult<String, String> result = _serverEvaluator.Evaluate(query);

        //assert
        assertFalse(result.HasError);
        assertFalse(result.HasReturnResult);
    }

    @Test
    public void Evaluate_CommandKeyValueNodeOther_HasError() {
        //arrange
        Query query = new Query();
        query.Command = new CommandKeyValueNode<String, String>(RequestCommand.GET, "x", "x");

        //act
        EvaluationResult<String, String> result = _serverEvaluator.Evaluate(query);

        //assert
        assertTrue(result.HasError);
    }
}
