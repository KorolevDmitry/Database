package parser;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class Lexer {

    public static void main(String[] args) {
        String input = "open \"C:\\temp\\test.txt\"";

        // Create tokens and print them
        Lexer lexer = new Lexer(input);
        Lexem lexem = lexer.getNextLexem();
        while (lexem != null) {
            System.out.println(lexem);
            lexem = lexer.getNextLexem();
        }
    }

    private ArrayList<Lexem> _lexems;
    private int _curPos;

    public Lexer(String input) {
        _lexems = lex(input);
    }

    private ArrayList<Lexem> lex(String input) {
        // The tokens to return
        ArrayList<Lexem> tokens = new ArrayList<Lexem>();

        // Lexer logic begins here
        StringBuffer tokenPatternsBuffer = new StringBuffer();
        for (LexemType tokenType : LexemType.values())
            tokenPatternsBuffer.append(String.format("|(?<%s>%s)", tokenType.name(), tokenType.pattern));
        Pattern tokenPatterns = Pattern.compile(new String(tokenPatternsBuffer.substring(1)));

        // Begin matching tokens
        Matcher matcher = tokenPatterns.matcher(input);
        while (matcher.find()) {
            if (matcher.group(LexemType.NUMBER.name()) != null) {
                tokens.add(new Lexem(LexemType.NUMBER, matcher.group(LexemType.NUMBER.name())));
                continue;
            } else if (matcher.group(LexemType.LITERAL.name()) != null) {
                tokens.add(new Lexem(LexemType.LITERAL, matcher.group(LexemType.LITERAL.name())));
                continue;
            } else if (matcher.group(LexemType.STRING.name()) != null) {
                tokens.add(new Lexem(LexemType.STRING, matcher.group(LexemType.STRING.name())));
                continue;
            } else if (matcher.group(LexemType.WHITESPACE.name()) != null)
                continue;
        }

        return tokens;
    }

    public Lexem getNextLexem() {
        if (_curPos == _lexems.size())
            return null;
        return _lexems.get(_curPos++);
    }
}
