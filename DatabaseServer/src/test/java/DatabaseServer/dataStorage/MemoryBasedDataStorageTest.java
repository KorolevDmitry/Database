package DatabaseServer.dataStorage;

import DatabaseBase.entities.StringSizable;
import DatabaseBase.entities.WrappedKeyValue;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;

import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class MemoryBasedDataStorageTest {
    private MemoryBasedDataStorage<StringSizable, StringSizable> _storage;
    private StringSizable defaultKey1 = new StringSizable("defaultKey1");
    private StringSizable defaultKey2 = new StringSizable("defaultKey2");
    private StringSizable defaultValue1 = new StringSizable("defaultValue1");
    private StringSizable defaultValue2 = new StringSizable("defaultValue2");
    private Random random = new Random();


    @Before
    public void setUp() throws Exception {
        _storage = new MemoryBasedDataStorage<StringSizable, StringSizable>();
    }

    @Test
    public void RemoveCompletely_ItemExist_GetReturnNull() throws Exception {
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
    public void StressTest_16MbKeysAnd1KbValues() throws IOException, ClassNotFoundException {
        //extend memory size for this test 4x16Mb
        int keyBytes = 16777216; //16Mb
        int valueBytes = 1024; //1Kb
        int elementsCount = 10;
        StringSizable[] arrayOfKeys = new StringSizable[elementsCount];
        StringSizable[] arrayOfValues = new StringSizable[elementsCount];

        for (int i = 0; i < elementsCount; i++) {
            arrayOfKeys[i] = new StringSizable(GenerateStringAllCharacters(keyBytes));
            arrayOfValues[i] = new StringSizable(GenerateStringAllCharacters(valueBytes));
        }

        for (int i = 0; i < elementsCount; i++) {
            _storage.AddOrUpdate(arrayOfKeys[i], arrayOfValues[i]);
        }

        for (int i = 0; i < elementsCount; i++) {
            WrappedKeyValue<StringSizable, StringSizable> value = _storage.Get(arrayOfKeys[i]);
            if(value.Value == null)
            {
                FileWriter writer = new FileWriter("error");
                writer.write("key:");
                writer.write(arrayOfKeys[i].Value);
                writer.write("\nvalue:");
                writer.write(arrayOfValues[i].Value);
                writer.flush();
                writer.close();
            }
            assertEquals(arrayOfValues[i], value.Value);
        }
    }

    private String GenerateStringAllCharacters(int countOfBytes)    {
        byte[] bytes = new byte[countOfBytes];
        for(int i = 0; i<countOfBytes;i++)
        {
            bytes[i]= (byte) (i%256);
        }
        String str = new String(bytes, Charset.forName("UTF-8"));
        //str = str.replace("\0", "");

        return str;
    }
}
