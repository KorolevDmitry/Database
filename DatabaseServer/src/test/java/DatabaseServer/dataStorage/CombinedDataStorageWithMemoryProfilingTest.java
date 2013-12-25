package DatabaseServer.dataStorage;

import DatabaseBase.entities.StringSizable;
import DatabaseBase.exceptions.DataStorageException;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 12/25/13
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class CombinedDataStorageWithMemoryProfilingTest extends CombinedDataStorageTest {

    @Override
    protected void InitStorage(int memorySize) throws DataStorageException, IOException, ClassNotFoundException {
        _storage = new CombinedDataStorageWithMemoryProfiling<StringSizable, StringSizable>(System.getProperty("user.dir"), "_fileStorage", 1, memorySize);
    }
}
