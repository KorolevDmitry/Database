package parser;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 12:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Lexem {
    private LexemType _lexemType;
    private String _value;

    public Lexem(LexemType lexemType, String value) {
        _lexemType = lexemType;
        _value = value;
    }

    @Override
    public String toString() {
        return String.format("(%s %s)", _lexemType.name(), _value);
    }
}
