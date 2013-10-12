package parser;

import parser.nodes.CommandNode;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParsedTree<TKey, TValue> {
    public CommandNode<TKey, TValue> Command;

    @Override
    public String toString() {
        return Command.toString();
    }
}
