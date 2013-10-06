import dataStorage.FileBasedDataStorage;
import dataStorage.WrappedKeyValue;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 4:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileBasedDataStorageTest {
    private FileBasedDataStorage<String, String> _storage;
    private String defaultKey1 = "defaultKey1";
    private String defaultKey2 = "defaultKey2";
    private String defaultValue1 = "defaultValue1";
    private String defaultValue2 = "defaultValue2";

    @Before
    public void setUp() throws Exception {
        _storage = new FileBasedDataStorage<String, String>(System.getProperty("user.dir"), 1);
    }

    @Test
    public void Get_ItemExists_Returned() throws Exception {
        //arrange
        _storage.AddOrUpdate(defaultKey1, defaultValue1);

        //act
        WrappedKeyValue item = _storage.Get(defaultKey1);

        //assert
        assertEquals(defaultValue1, item.Value);
    }

    @Test
    public void Delete_ItemExist_ItemMarkedAsDeleted() throws Exception {
        //arrange
        _storage.AddOrUpdate(defaultKey1, defaultValue1);

        //act
        _storage.Delete(defaultKey1);

        //assert
        assertTrue(_storage.Get(defaultKey1).IsDeleted);
    }
}
