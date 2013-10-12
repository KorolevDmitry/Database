package dataStorage;

import interfaces.IDataStorage;
import entities.WrappedKeyValue;
import utils.AppendingObjectOutputStream;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 2:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileBasedDataStorage<TKey, TValue> implements IDataStorage<TKey, TValue> {

    private String _baseDirectory;
    private int _fileSplitSize;
    private final String _prefix = "fileStorage_";

    public FileBasedDataStorage(String baseDirectory, int fileSplitSize) {
        _baseDirectory = baseDirectory;
        _fileSplitSize = fileSplitSize;
    }

    @Override
    public WrappedKeyValue<TKey, TValue> Get(TKey tKey) {
        WrappedKeyValue<TKey, TValue> value = null;
        try {
            value = Find(tKey);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: logging
        }
        return value;
    }

    @Override
    public List<WrappedKeyValue<TKey, TValue>> GetElements() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void AddOrUpdate(TKey tKey, TValue tValue) {
        try {
            Add(new WrappedKeyValue<TKey, TValue>(tKey, tValue));
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: logging
        }
    }

    @Override
    public void AddOrUpdate(List<WrappedKeyValue<TKey, TValue>> values) {
        try {
            HashMap<String, List<WrappedKeyValue<TKey, TValue>>> splittedValues = new HashMap<String, List<WrappedKeyValue<TKey, TValue>>>();
            for (int i = 0; i < values.size(); i++) {
                String filePath = GetFilePath(values.get(i).Key);
                if (!splittedValues.containsKey(filePath)) {
                    splittedValues.put(filePath, new ArrayList<WrappedKeyValue<TKey, TValue>>());
                }
                splittedValues.get(filePath).add(values.get(i));
            }
            for (String entry : splittedValues.keySet()) {
                Add(entry, splittedValues.get(entry));
            }
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: logging
        }
    }

    @Override
    public void Delete(TKey tKey) {
        try {
            Add(new WrappedKeyValue<TKey, TValue>(tKey, null, true));
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: logging
        }
    }

    @Override
    public void Clear() {
        //TODO: remove all files
    }

    @Override
    public void Close() {
    }

    private int GetIndex(TKey key) {
        return Math.abs(key.hashCode()) % 5;
    }

    private String GetFilePath(TKey key) {
        return _baseDirectory + "" + GetIndex(key);
    }

    private void Add(String filePath, List<WrappedKeyValue<TKey, TValue>> value) throws IOException {
        File file = new File(filePath);
        ObjectOutputStream out;
        if (file.exists()) {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            out = new AppendingObjectOutputStream(fileOutputStream);
        } else {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            out = new ObjectOutputStream(fileOutputStream);
        }
        for (int i = 0; i < value.size(); i++) {
            out.writeObject(value.get(i));
        }
        out.flush();
        out.close();
    }

    private void Add(WrappedKeyValue<TKey, TValue> value) throws IOException {
        File file = new File(GetFilePath(value.Key));
        ObjectOutputStream out;
        if (file.exists()) {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            out = new AppendingObjectOutputStream(fileOutputStream);
        } else {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            out = new ObjectOutputStream(fileOutputStream);
        }

        out.writeObject(value);
        out.flush();
        out.close();
    }

    private WrappedKeyValue<TKey, TValue> Find(TKey key) throws IOException {
        File file = new File(GetFilePath(key));
        WrappedKeyValue<TKey, TValue> result = null;
        if (!file.exists()) {
            return result;
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fileInputStream);
        try {
            WrappedKeyValue<TKey, TValue> value;
            while (true) {

                value = (WrappedKeyValue<TKey, TValue>) in.readObject();
                if (value.Key.equals(key))
                    result = value;
            }
        } catch (EOFException e) {
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            //TODO: logging
        }
        in.close();
        return result;
    }
}

