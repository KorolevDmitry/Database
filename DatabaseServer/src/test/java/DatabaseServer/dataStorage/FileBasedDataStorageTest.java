package DatabaseServer.dataStorage;

import DatabaseBase.entities.WrappedKeyValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import DatabaseServer.utils.AppendingObjectOutputStream;
import DatabaseServer.utils.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Random;

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
    private Random random = new Random();

    @Before
    public void setUp() throws Exception {
        _storage = new FileBasedDataStorage<String, String>(System.getProperty("user.dir"), "_fileStorage", 1);
    }

    @After
    public void tearDown() throws Exception {
        _storage.Close();
        File file = new File(_storage.GetIndexFilePath());
        if(file.exists())
        {
            FileUtils.DeleteFile(file);
//            file.delete();
        }
        String[] paths = _storage.GetStorageFilePaths();
        for(int i=0;i<paths.length;i++)
        {
            file = new File(paths[i]);
            if(file.exists())
            {
                FileUtils.DeleteFile(file);
//                file.delete();
            }
        }
    }

    @Test
    public void ChangeStreamPosition_ObjectOutputStream_StreamIsReadable() throws IOException, ClassNotFoundException {
        String[] values = {"1","2","3"};
        long[] indecies = {0,0,0};
        File file = new File("test.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
        indecies[0]=fileOutputStream.getChannel().position();
        out.writeObject(values[0]);
        indecies[1]=fileOutputStream.getChannel().position();
        out.writeObject(values[1]);
        indecies[2]=fileOutputStream.getChannel().position();
        out.writeObject(values[2]);
        out.close();

        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fileInputStream);

        fileInputStream.getChannel().position(indecies[2]);
        String readed = (String)in.readObject();
        assertEquals(values[2],readed);

        fileInputStream.getChannel().position(indecies[1]);
        readed = (String)in.readObject();
        assertEquals(values[1],readed);

        fileInputStream.getChannel().position(indecies[0]);
        readed = (String)in.readObject();
        assertEquals(values[0],readed);
        in.close();
    }

    @Test
    public void ChangeStreamPosition_AllCharacters_StreamIsReadable() throws IOException, ClassNotFoundException {
        String[] values = {"1","2","3"};
        for(int i = 0; i < 25600; i++)
        {
            values[0] += (char)(i%256);
            values[1] += (char)(i%256);
            values[2] += (char)(i%256);
        }
        long[] indecies = {0,0,0};
        File file = new File("test.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
        indecies[0]=fileOutputStream.getChannel().position();
        out.writeObject(values[0]);
        out.close();

        fileOutputStream = new FileOutputStream(file, true);
        out = new AppendingObjectOutputStream(fileOutputStream);
        indecies[1]=fileOutputStream.getChannel().position();
        out.writeObject(values[1]);
        indecies[2]=fileOutputStream.getChannel().position();
        out.writeObject(values[2]);
        out.close();

        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fileInputStream);

        fileInputStream.getChannel().position(indecies[2]);
        String readed = (String)in.readObject();
        assertEquals(values[2],readed);

        fileInputStream.getChannel().position(indecies[1]);
        readed = (String)in.readObject();
        assertEquals(values[1],readed);

        fileInputStream.getChannel().position(indecies[0]);
        readed = (String)in.readObject();
        assertEquals(values[0],readed);
        in.close();
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
    public void Get_ReOpenStorage_Returned() throws Exception {
        //arrange
        _storage.AddOrUpdate(defaultKey1, defaultValue1);
        _storage.Close();
        _storage = new FileBasedDataStorage<String, String>(System.getProperty("user.dir"), "_fileStorage", 1);

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
        byte[] keyBytes = new byte[1024]; //1Kb
        byte[] valueBytes = new byte[1024]; //1Kb
        int elementsCount = 10;
        String[] arrayOfKeys = new String[elementsCount];
        String[] arrayOfValues = new String[elementsCount];
        Random random = new Random();

        //Add
        for (int i = 0; i < elementsCount; i++) {
            random.nextBytes(keyBytes);
            arrayOfKeys[i] = new String(keyBytes);
            random.nextBytes(valueBytes);
            arrayOfValues[i] = new String(valueBytes);
        }
        for (int i = 0; i < elementsCount; i++) {
            _storage.AddOrUpdate(arrayOfKeys[i], arrayOfValues[i]);
        }

        //Update
        for (int i = 0; i < elementsCount; i++) {
            random.nextBytes(valueBytes);
            arrayOfValues[i] = new String(valueBytes);
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
    public void StressTest_16MbKeysAnd1KbValues() throws IOException, ClassNotFoundException {
        //extend memory size for this test 4x16Mb
        _storage = new FileBasedDataStorage<String, String>(System.getProperty("user.dir"), "fileStorage", 1);
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

    private String GenerateStringAllCharacters(int countOfBytes)    {
        byte[] bytes = new byte[countOfBytes];
        //random.nextBytes(bytes);
        for(int i = 0; i<countOfBytes;i++)
        {
            bytes[i]= (byte) (i%256);
        }
//        String str = new String(bytes);
//        str = str.replace("\0", "");

        return new String(bytes, Charset.forName("UTF-8"));
    }
}
