package DatabaseClient.parser;

import DatabaseBase.commands.ServerCommand;
import DatabaseBase.entities.Query;
import DatabaseBase.exceptions.ParserException;
import DatabaseBase.parser.Lexer;
import org.junit.Before;
import org.junit.Test;
import DatabaseBase.commands.CommandSingleNode;
import DatabaseBase.commands.RequestCommand;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/24/13
 * Time: 12:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class ClientParserTest {
    private ClientParserStringString _parser;

    @Before
    public void setUp() throws Exception {
        _parser = new ClientParserStringString(new Lexer());
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
    public void Parse_ServerCommand_ServerCommand() throws Exception {
        //arrange
        String str = "add d";

        //act
        Query query = _parser.Parse(str);

        //assert
        assertTrue(query.Command instanceof ServerCommand);
    }
}
