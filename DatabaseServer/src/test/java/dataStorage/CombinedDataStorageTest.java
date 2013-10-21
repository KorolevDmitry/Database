package dataStorage;

import entities.WrappedKeyValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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
    private Random random = new Random();

    @Before
    public void setUp() throws Exception {
        _storage = new CombinedDataStorage<String, String>(System.getProperty("user.dir"), "_fileStorage", 1, 1024);
    }

    @After
    public void tearDown() throws Exception {
        _storage.Close();
        File file = new File(_storage.GetIndexFilePath());
        if(file.exists())
        {
            file.delete();
        }
        String[] paths = _storage.GetStorageFilePaths();
        for(int i=0;i<paths.length;i++)
        {
            file = new File(paths[i]);
            if(file.exists())
            {
                file.delete();
            }
        }
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
    public void Get_ItemWasUpdated_ReturnedUpdatedValue() throws Exception {
        //arrange
        _storage.AddOrUpdate(defaultKey1, defaultValue1);
        _storage.AddOrUpdate(defaultKey1, defaultValue2);

        //act
        WrappedKeyValue item = _storage.Get(defaultKey1);

        //assert
        assertEquals(defaultValue2, item.Value);
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
    public void StressTest_AddUpdateValues() throws IOException {
        int keyBytes = 1024; //1Kb
        int valueBytes = 1024; //1Kb
        int elementsCount = 10;
        String[] arrayOfKeys = new String[elementsCount];
        String[] arrayOfValues = new String[elementsCount];

        //Add
        for (int i = 0; i < elementsCount; i++) {
            arrayOfKeys[i] = GenerateStringAllCharacters(keyBytes);
            arrayOfValues[i] = GenerateStringAllCharacters(valueBytes);
        }
        for (int i = 0; i < elementsCount; i++) {
            _storage.AddOrUpdate(arrayOfKeys[i], arrayOfValues[i]);
        }

        //Update
        for (int i = 0; i < elementsCount; i++) {
            arrayOfValues[i] = GenerateStringAllCharacters(valueBytes);
        }
        for (int i = 0; i < elementsCount; i++) {
            _storage.AddOrUpdate(arrayOfKeys[i], arrayOfValues[i]);
        }

        //check
        for (int i = 0; i < elementsCount; i++) {
            WrappedKeyValue value = _storage.Get(arrayOfKeys[i]);
            assertEquals(arrayOfValues[i], value.Value);
        }
    }

    @Test
    public void StressTest_MemoryIsZero_Working() throws IOException, ClassNotFoundException {
        //extend memory size for this test 4x16Mb
        _storage = new CombinedDataStorage<String, String>(System.getProperty("user.dir"), "fileStorage", 1, 0);
        int keyBytes = 1024; //1Kb
        int valueBytes = 1024; //1Kb
        int elementsCount = 10;
        String[] arrayOfKeys = new String[elementsCount];
        String[] arrayOfValues = new String[elementsCount];

        for (int i = 0; i < elementsCount; i++) {
            arrayOfKeys[i] = GenerateStringAllCharacters(keyBytes);
            arrayOfValues[i] = GenerateStringAllCharacters(valueBytes);
        }

        for (int i = 0; i < elementsCount; i++) {
            _storage.AddOrUpdate(arrayOfKeys[i], arrayOfValues[i]);
        }

        for (int i = 0; i < elementsCount; i++) {
            WrappedKeyValue value = _storage.Get(arrayOfKeys[i]);
            assertEquals(arrayOfValues[i], value.Value);
        }
    }

    @Test
    public void StressTest_MemoryIsEnoughOnlyForOneElement_Working() throws IOException, ClassNotFoundException {
        //extend memory size for this test 4x16Mb
        _storage = new CombinedDataStorage<String, String>(System.getProperty("user.dir"), "fileStorage", 1, 3072);
        int keyBytes = 1024; //1Kb
        int valueBytes = 1024; //1Kb
        int elementsCount = 10;
        String[] arrayOfKeys = new String[elementsCount];
        String[] arrayOfValues = new String[elementsCount];

        for (int i = 0; i < elementsCount; i++) {
            arrayOfKeys[i] = GenerateStringAllCharacters(keyBytes);
            arrayOfValues[i] = GenerateStringAllCharacters(valueBytes);
        }

        for (int i = 0; i < elementsCount; i++) {
            _storage.AddOrUpdate(arrayOfKeys[i], arrayOfValues[i]);
        }

        for (int i = 0; i < elementsCount; i++) {
            WrappedKeyValue value = _storage.Get(arrayOfKeys[i]);
            assertEquals(arrayOfValues[i], value.Value);
        }
    }

    @Test
    public void StressTest_16MbKeysAnd1KbValues() throws IOException, ClassNotFoundException {
        //extend memory size for this test 4x16Mb
        _storage = new CombinedDataStorage<String, String>(System.getProperty("user.dir"), "fileStorage", 1, 67108864);
        int keyBytes = 16777216; //16Mb
        int valueBytes = 1024; //1Kb
        int elementsCount = 10;
        String[] arrayOfKeys = new String[elementsCount];
        String[] arrayOfValues = new String[elementsCount];

        for (int i = 0; i < elementsCount; i++) {
            arrayOfKeys[i] = GenerateStringAllCharacters(keyBytes);
            arrayOfValues[i] = GenerateStringAllCharacters(valueBytes);
        }

        for (int i = 0; i < elementsCount; i++) {
            _storage.AddOrUpdate(arrayOfKeys[i], arrayOfValues[i]);
        }

        for (int i = 0; i < elementsCount; i++) {
            WrappedKeyValue value = _storage.Get(arrayOfKeys[i]);
            if(value.Value == null)
            {
                FileOutputStream outputStream = new FileOutputStream("error");
                FileWriter writer = new FileWriter("error");
                writer.write("key:");
                writer.write(arrayOfKeys[i]);
                writer.write("\nvalue:");
                writer.write(arrayOfValues[i]);
                writer.flush();
                writer.close();
            }
            assertEquals(arrayOfValues[i], value.Value);
        }
    }

    /*private String GenerateString(int countOfBytes)    {
        byte[] bytes = new byte[countOfBytes];
        random.nextBytes(bytes);
        String str = new String(bytes);
        str = str.replace("\0", "");

        return str;
    }*/

    private String GenerateStringAllCharacters(int countOfBytes)    {
        byte[] bytes = new byte[countOfBytes];
        for(int i = 0; i<countOfBytes;i++)
        {
            bytes[i]= (byte) (i%256);
        }
        String str = new String(bytes);
        str = str.replace("\0", "");

        return str;
    }
}
