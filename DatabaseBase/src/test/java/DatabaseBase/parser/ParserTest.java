package DatabaseBase.parser;

import DatabaseBase.commands.*;
import DatabaseBase.entities.Query;
import DatabaseBase.exceptions.ParserException;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/24/13
 * Time: 12:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class ParserTest {
    private ParserStringString _parser;

    @Before
    public void setUp() throws Exception {
        _parser = new ParserStringString(new Lexer());
    }

    @Test
    public void Parse_Help_HelpCommand() throws Exception {
        //arrange
        String str = "help";

        //act
        Query query = _parser.Parse(str);

        //assert
        assertTrue(query.Command instanceof CommandSingleNode);
        assertEquals(RequestCommand.HELP, query.Command.GetCommand());
    }

    @Test
    public void Parse_Quit_QuitCommand() throws Exception {
        //arrange
        String str = "quit";

        //act
        Query query = _parser.Parse(str);

        //assert
        assertTrue(query.Command instanceof CommandSingleNode);
        assertEquals(RequestCommand.QUIT, query.Command.GetCommand());
    }

    @Test
    public void Parse_Help1_ParseException() throws Exception {
        //arrange
        String str = "help1";
        boolean hasParseException = false;

        //act
        try{
        Query query = _parser.Parse(str);
        }catch (ParserException exception)
        {
            hasParseException = true;
        }

        //assert
        assertTrue(hasParseException);
    }

    @Test
    public void Parse_QuitWithLiteral_ParseException() throws Exception {
        //arrange
        String str = "quit asd";
        boolean hasParseException = false;

        //act
        try{
            Query query = _parser.Parse(str);
        }catch (ParserException exception)
        {
            hasParseException = true;
        }

        //assert
        assertTrue(hasParseException);
    }

    @Test
    public void Parse_GetWithoutKey_ParseException() throws Exception {
        //arrange
        String str = "get";
        boolean hasParseException = false;

        //act
        try{
            Query query = _parser.Parse(str);
        }catch (ParserException exception)
        {
            hasParseException = true;
        }

        //assert
        assertTrue(hasParseException);
    }

    @Test
    public void Parse_GetWithKey_CommandKeyNode() throws Exception {
        //arrange
        String str = "get x";

        //act
        Query query = _parser.Parse(str);

        //assert
        assertTrue(query.Command instanceof CommandKeyNode);
        assertEquals(RequestCommand.GET, query.Command.GetCommand());
    }

    @Test
    public void Parse_GetWithKeyRangeAndAdditionalLiteral_CommandMultiKeyNode() throws Exception {
        //arrange
        String str = "get x x";
        boolean hasParseException = false;

        //act
        Query query = _parser.Parse(str);

        //assert
        assertTrue(query.Command instanceof CommandMultiKeyNode);
        assertEquals(RequestCommand.GET, query.Command.GetCommand());
    }

    @Test
    public void Parse_GetWithKeyAndAdditionalLiteral_ParseException() throws Exception {
        //arrange
        String str = "get x x x";
        boolean hasParseException = false;

        //act
        try{
            Query query = _parser.Parse(str);
        }catch (ParserException exception)
        {
            hasParseException = true;
        }

        //assert
        assertTrue(hasParseException);
    }

    @Test
    public void Parse_AddWithoutKey_ParseException() throws Exception {
        //arrange
        String str = "add";
        boolean hasParseException = false;

        //act
        try{
            Query query = _parser.Parse(str);
        }catch (ParserException exception)
        {
            hasParseException = true;
        }

        //assert
        assertTrue(hasParseException);
    }

    @Test
    public void Parse_AddWithKey_ParseException() throws Exception {
        //arrange
        String str = "add x";
        boolean hasParseException = false;

        //act
        try{
            Query query = _parser.Parse(str);
        }catch (ParserException exception)
        {
            hasParseException = true;
        }

        //assert
        assertTrue(hasParseException);
    }

    @Test
    public void Parse_AddWithKeyAndValue_CommandKeyValueNode() throws Exception {
        //arrange
        String str = "add x x";

        //act
        Query query = _parser.Parse(str);

        //assert
        assertTrue(query.Command instanceof CommandKeyValueNode);
        assertEquals(RequestCommand.ADD, query.Command.GetCommand());
    }

    @Test
    public void Parse_AddWithKeyRangeAndValueAndAdditionalLiteral_CommandMultiKeyValueNode() throws Exception {
        //arrange
        String str = "add x x x";
        boolean hasParseException = false;

        //act
        Query query = _parser.Parse(str);

        //assert
        assertTrue(query.Command instanceof CommandMultiKeyValueNode);
        assertEquals(RequestCommand.ADD, query.Command.GetCommand());
    }

    @Test
    public void Parse_AddWithKeyAndValueAndAdditionalLiteral_ParseException() throws Exception {
        //arrange
        String str = "add x x x x";
        boolean hasParseException = false;

        //act
        try{
            Query query = _parser.Parse(str);
        }catch (ParserException exception)
        {
            hasParseException = true;
        }

        //assert
        assertTrue(hasParseException);
    }
}
