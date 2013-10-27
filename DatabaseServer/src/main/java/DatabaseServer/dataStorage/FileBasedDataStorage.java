package DatabaseServer.dataStorage;

import DatabaseBase.entities.WrappedKeyValue;
import DatabaseBase.interfaces.IDataStorage;
import DatabaseBase.interfaces.ISizable;
import DatabaseServer.utils.AppendingObjectOutputStream;
import DatabaseServer.utils.FileUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 2:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileBasedDataStorage<TKey extends ISizable, TValue extends ISizable> implements IDataStorage<TKey, TValue> {
    private String _indexFileName = "indexFileStorage.dat";
    private String _indexFilePath;
    private String _baseDirectory;
    private int _fileSplitSize;
    private String _prefix;
    private String[] _storageFilePaths;
    ConcurrentHashMap<TKey, Long> _index;

    public FileBasedDataStorage(String baseDirectory, String filePrefix, int fileSplitSize) throws IOException, ClassNotFoundException {
        _baseDirectory = baseDirectory == "." ? System.getProperty("user.dir") : baseDirectory;
        if (_baseDirectory.charAt(_baseDirectory.length() - 1) != '\\')
            _baseDirectory = _baseDirectory + "\\";
        _prefix = filePrefix;
        _fileSplitSize = fileSplitSize;
        _index = new ConcurrentHashMap<TKey, Long>();
        _indexFilePath = _baseDirectory + _indexFileName + "\\";
        _storageFilePaths = GetStorageFilePaths();
        LoadIndexFile();
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
    public List<WrappedKeyValue<TKey, TValue>> GetElements() throws IOException {
        //TODO: Implement in different way
        List<WrappedKeyValue<TKey, TValue>> elements = new ArrayList<WrappedKeyValue<TKey, TValue>>(_index.size());
        Iterator<Map.Entry<TKey,Long>> iterator = _index.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<TKey, Long> current = iterator.next();
            elements.add(Find(current.getKey()));
        }

        return elements;
    }

    @Override
    public void AddOrUpdate(TKey tKey, TValue tValue) throws IOException {
        Add(new WrappedKeyValue<TKey, TValue>(tKey, tValue));
    }

    @Override
    public void AddOrUpdate(List<WrappedKeyValue<TKey, TValue>> values) throws IOException {
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
    }

    @Override
    public void Delete(TKey tKey) throws IOException {
        Add(new WrappedKeyValue<TKey, TValue>(tKey, null, true));
    }

    @Override
    public void Clear() {
        //TODO: remove all files
    }

    @Override
    public void Close() throws IOException {
        FlushIndexFile();
    }

    public String GetIndexFilePath() {
        return _indexFilePath;
    }

    public String[] GetStorageFilePaths() {
        String[] paths = new String[_fileSplitSize];
        for (int i = 0; i < _fileSplitSize; i++) {
            paths[i] = _baseDirectory + _prefix + i + "\\";
        }

        return paths;
    }

    private void LoadIndexFile() throws IOException, ClassNotFoundException {
        String filePath = GetIndexFilePath();
        synchronized (filePath) {
            File file = new File(filePath);
            if (file.exists()) {
                FileInputStream fileInputStream = null;
                ObjectInputStream in = null;
                try {
                    fileInputStream = new FileInputStream(file);
                    in = new ObjectInputStream(fileInputStream);
                    _index = (ConcurrentHashMap<TKey, Long>) in.readObject();
                } catch (EOFException eOFException)
                {
                }
                finally {
                    if (in != null) {
                        in.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                }
            }
            if (_index == null) {
                _index = new ConcurrentHashMap<TKey, Long>();
            }
        }
    }

    private int GetIndex(TKey key) {
        return Math.abs(key.hashCode()) % _fileSplitSize;
    }

    private String GetFilePath(TKey key) {
        return _storageFilePaths[GetIndex(key)];
    }

    private void FlushIndexFile() throws IOException {
        String filePath = GetIndexFilePath();
        synchronized (filePath) {
            File file = new File(filePath);
            if (file.exists()) {
                //file.delete();
            }
            //file.createNewFile();
            FileUtils.CreateFile(file);
            FileOutputStream fileOutputStream = null;
            ObjectOutputStream out = null;
            try {
                fileOutputStream = new FileOutputStream(file);
                out = new ObjectOutputStream(fileOutputStream);
                out.writeObject(_index);
            } finally {
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if(fileOutputStream != null){
                    fileOutputStream.close();
                }
            }
        }
    }

    private void Add(String filePath, List<WrappedKeyValue<TKey, TValue>> value) throws IOException {
        synchronized (filePath) {
            File file = new File(filePath);
            ObjectOutputStream out = null;
            FileOutputStream fileOutputStream = null;
            long position;
            try {
                if (file.exists()) {
                    fileOutputStream = new FileOutputStream(file, true);
                    out = new AppendingObjectOutputStream(fileOutputStream);
                } else {
                    FileUtils.CreateFile(file);
                    fileOutputStream = new FileOutputStream(file);
                    out = new ObjectOutputStream(fileOutputStream);
                }
                for (int i = 0; i < value.size(); i++) {
                    position = fileOutputStream.getChannel().position();
                    out.writeObject(value.get(i));
                    _index.put(value.get(i).Key, position);
                }
            } finally {
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if(fileOutputStream != null){
                    fileOutputStream.close();
                }
            }
        }
    }

    private void Add(WrappedKeyValue<TKey, TValue> value) throws IOException {
        String filePath = GetFilePath(value.Key);
        synchronized (filePath) {
            File file = new File(filePath);
            ObjectOutputStream out = null;
            FileOutputStream fileOutputStream = null;
            long position;
            try {
                if (file.exists()) {
                    fileOutputStream = new FileOutputStream(file, true);
                    out = new AppendingObjectOutputStream(fileOutputStream);
                } else {
                    FileUtils.CreateFile(file);
                    fileOutputStream = new FileOutputStream(file);
                    out = new ObjectOutputStream(fileOutputStream);
                }
                position = fileOutputStream.getChannel().position();
                out.writeObject(value);
                _index.put(value.Key, position);
            } finally {
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if(fileOutputStream != null){
                    fileOutputStream.close();
                }
            }
        }
    }

    private WrappedKeyValue<TKey, TValue> Find(TKey key) throws IOException {
        File file = new File(GetFilePath(key));
        WrappedKeyValue<TKey, TValue> result = null;
        if (!_index.containsKey(key)) {
            return result;
        }
        if (!file.exists()) {
            return result;
        }
        long localFileIndex = _index.get(key);
        FileInputStream fileInputStream = null;
        ObjectInputStream in = null;
        try {
            fileInputStream = new FileInputStream(file);
            in = new ObjectInputStream(fileInputStream);
            fileInputStream.getChannel().position(localFileIndex);
            WrappedKeyValue<TKey, TValue> value;
            while (true) {
                value = (WrappedKeyValue<TKey, TValue>) in.readObject();
                if (value.Key.equals(key)) {
                    result = value;
                    break;
                }
            }
        } catch (EOFException e) {
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            //TODO: logging
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (in != null) {
                in.close();
            }
            if(fileInputStream != null) {
                fileInputStream.close();
            }
        }
        return result;
    }
}

