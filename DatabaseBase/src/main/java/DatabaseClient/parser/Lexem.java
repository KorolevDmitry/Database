package DatabaseClient.parser;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 12:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Lexem {
    public LexemType LexemType;
    public String Value;

    public Lexem(LexemType lexemType, String value) {
        LexemType = lexemType;
        Value = value;
    }

    @Override
    public String toString() {
        return String.format("(%s %s)", LexemType.name(), Value);
    }
}
