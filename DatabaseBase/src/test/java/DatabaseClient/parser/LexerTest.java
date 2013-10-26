package DatabaseClient.parser;

import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/11/13
 * Time: 1:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class LexerTest {
    @Test
    public void Lex_Literal_Literal() throws Exception {
        //arrange
        Lexer lexer = new Lexer();

        //act
        ArrayList<Lexem> lexems = lexer.Lex("add");

        //assert
        assertEquals(LexemType.LITERAL, lexems.get(0).LexemType);
    }

    @Test
    public void Lex_String_String() throws Exception {
        //arrange
        Lexer lexer = new Lexer();

        //act
        ArrayList<Lexem> lexems = lexer.Lex("\"C:\\deploy\\add\"");

        //assert
        assertEquals(LexemType.STRING, lexems.get(0).LexemType);
    }

    @Test
    public void Lex_Whitespace_Whitespace() throws Exception {
        //arrange
        Lexer lexer = new Lexer();

        //act
        ArrayList<Lexem> lexems = lexer.Lex("   ");

        //assert
        assertEquals(0, lexems.size());
    }

    @Test
    public void Lex_Combination_Combination() throws Exception {
        //arrange
        Lexer lexer = new Lexer();
        String str = "open \"C:\\deploy\\add\\out.txt\" -verbose";
        //act
        ArrayList<Lexem> lexems = lexer.Lex(str);

        //assert
        assertEquals(LexemType.LITERAL, lexems.get(0).LexemType);
        assertEquals(LexemType.STRING, lexems.get(1).LexemType);
        assertEquals(LexemType.LITERAL, lexems.get(2).LexemType);
    }
}
