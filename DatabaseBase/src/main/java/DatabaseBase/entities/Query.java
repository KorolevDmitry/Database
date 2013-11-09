package DatabaseBase.entities;

import DatabaseBase.commands.CommandNode;
import DatabaseBase.interfaces.ISizable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 9/28/13
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Query implements ISizable {
    public CommandNode Command;
    public Integer UniqueId = 0;
    public boolean Completed;
    public boolean Success;
    public int NumberToRead;
    public int NumberToWrite;
    public List<Route> ExecutionRoutes;

    public Query(){
        ExecutionRoutes = new ArrayList<Route>();
    }


    @Override
    public String toString() {
        return Command == null ? "NULL" : Command.toString();
    }

    @Override
    public long GetSize() {
        //TODO: implement
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(!(obj instanceof Query))
            return false;
        if(UniqueId == null)
            return ((Query)obj).UniqueId == null;
        return UniqueId.equals(((Query)obj).UniqueId);
    }

    @Override
    public int hashCode(){
        return UniqueId.hashCode();
    }
}
