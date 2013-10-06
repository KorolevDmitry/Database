
import dataStorage.MemoryBasedDataStorage;
import dataStorage.WrappedKeyValue;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class MemoryBasedDataStorageTest {
    private MemoryBasedDataStorage<String, String> _storage;
    private String defaultKey1 = "defaultKey1";
    private String defaultKey2 = "defaultKey2";
    private String defaultValue1 = "defaultValue1";
    private String defaultValue2 = "defaultValue2";


    @Before
    public void setUp() throws Exception {
        _storage = new MemoryBasedDataStorage<String, String>();
    }

    @Test
    public void RemoveCompletly_ItemExist_GetReturnNull() throws Exception {
        //arrange
        _storage.AddOrUpdate(defaultKey1, defaultValue1);

        //act
        _storage.RemoveCompletly(defaultKey1);

        //assert
        assertNull(_storage.Get(defaultKey1));
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
