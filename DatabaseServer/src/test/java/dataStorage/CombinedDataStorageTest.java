package dataStorage;

import entities.WrappedKeyValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/8/13
 * Time: 1:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class CombinedDataStorageTest {
    private CombinedDataStorage<String, String> _storage;
    private String defaultKey1 = "defaultKey1";
    private String defaultKey2 = "defaultKey2";
    private String defaultValue1 = "defaultValue1";
    private String defaultValue2 = "defaultValue2";

    @Before
    public void setUp() throws Exception {
        _storage = new CombinedDataStorage<String, String>(System.getProperty("user.dir"), "_fileStorage", 1, 1024);
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

    @Test
    public void StressTest_16MbKeysAnd1KbValues()
    {
        //extend memory size for this test 4x16Mb
        _storage = new CombinedDataStorage<String, String>(System.getProperty("user.dir"), "fileStorage", 1, 67108864);
        byte[] keyBytes = new byte[16777216]; //16Mb
        byte[] valueBytes = new byte[1024]; //1Kb
        int elementsCount = 10;
        String[] arrayOfKeys = new String[elementsCount];
        String[] arrayOfValues = new String[elementsCount];
        Random random = new Random();

        for (int i = 0; i < elementsCount; i++) {
            random.nextBytes(keyBytes);
            arrayOfKeys[i] = new String(keyBytes);
            random.nextBytes(valueBytes);
            arrayOfValues[i] = new String(valueBytes);
        }

        for (int i = 0; i < elementsCount; i++) {
            _storage.AddOrUpdate(arrayOfKeys[i], arrayOfValues[i]);
        }

        for (int i = 0; i < elementsCount; i++) {
            WrappedKeyValue value = _storage.Get(arrayOfKeys[i]);
            assertEquals(arrayOfValues[i], value.Value);
        }
    }
}
