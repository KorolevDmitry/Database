package DatabaseBase.parser;

import DatabaseBase.exceptions.LexerException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class Lexer {
    public ArrayList<Lexem> Lex(String input) throws LexerException {

        String[] data = input.split("/s");

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
            /*if (matcher.group(LexemType.NUMBER.name()) != null) {
                tokens.add(new Lexem(LexemType.NUMBER, matcher.group(LexemType.NUMBER.name())));
                continue;
            } else*/
            if (matcher.group(LexemType.LITERAL.name()) != null) {
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
}
