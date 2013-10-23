package dataStorage;

import entities.WrappedKeyValue;
import interfaces.IDataStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/6/13
 * Time: 1:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class CombinedDataStorage<TKey, TValue> implements IDataStorage<TKey, TValue> {
    private MemoryBasedDataStorage<TKey, TValue> _memoryStorage;
    private FileBasedDataStorage<TKey, TValue> _fileStorage;
    private ConcurrentLinkedQueue<TKey> _freqElements;
    private int _maxMemorySize;
    private int _currentMemorySize;

    public CombinedDataStorage(String baseDirectory, String filePrefix, int fileSplitSize, int memorySize) throws IOException, ClassNotFoundException {
        _memoryStorage = new MemoryBasedDataStorage<TKey, TValue>();
        _fileStorage = new FileBasedDataStorage<TKey, TValue>(baseDirectory, filePrefix, fileSplitSize);
        _freqElements = new ConcurrentLinkedQueue<TKey>();
        _maxMemorySize = memorySize;
    }

    private void AddItemToMemory(WrappedKeyValue<TKey, TValue> item) throws IOException {
        if (item.IsDeleted) {
            WrappedKeyValue oldItem = _memoryStorage.Get(item.Key);
            _currentMemorySize -= oldItem == null ? 0 : oldItem.Size;
            _memoryStorage.Delete(item.Key);
            _currentMemorySize += oldItem == null ? 0 : item.Size;
        } else {
            _memoryStorage.AddOrUpdate(item.Key, item.Value);
        }
        if (!_freqElements.contains(item.Key)) {
            _freqElements.add(item.Key);
            _currentMemorySize += item.Size;
        }

        if (_currentMemorySize >= _maxMemorySize) {
            List<WrappedKeyValue<TKey, TValue>> items = new ArrayList<WrappedKeyValue<TKey, TValue>>();
            while (_currentMemorySize > _maxMemorySize / 2) {
                item = _memoryStorage.Get(_freqElements.poll());
                items.add(item);
                _currentMemorySize -= item.Size;
            }
            for (int i = 0; i < items.size(); i++) {
                _memoryStorage.RemoveCompletly(items.get(i).Key);
            }
            _fileStorage.AddOrUpdate(items);
        }
    }

    private void AddItemToMemory(List<WrappedKeyValue<TKey, TValue>> wrappedKeyValue) throws IOException {
        for (WrappedKeyValue<TKey, TValue> item : wrappedKeyValue) {
            AddItemToMemory(item);
        }
    }

    @Override
    public WrappedKeyValue<TKey, TValue> Get(TKey tKey) throws IOException {
        WrappedKeyValue<TKey, TValue> value = _memoryStorage.Get(tKey);
        if (value == null) {
            value = _fileStorage.Get(tKey);
            if (value == null) {
                value = new WrappedKeyValue<TKey, TValue>(tKey, null, true);
            }
            AddItemToMemory(value);
        }
        return value;
    }

    @Override
    public List<WrappedKeyValue<TKey, TValue>> GetElements() {
        return _fileStorage.GetElements();
    }

    @Override
    public void AddOrUpdate(TKey tKey, TValue tValue) throws IOException {
        AddItemToMemory(new WrappedKeyValue<TKey, TValue>(tKey, tValue));
    }

    @Override
    public void AddOrUpdate(List<WrappedKeyValue<TKey, TValue>> wrappedKeyValues) throws IOException {
        AddItemToMemory(wrappedKeyValues);
    }

    @Override
    public void Delete(TKey tKey) throws IOException {
        AddItemToMemory(new WrappedKeyValue<TKey, TValue>(tKey, null, true));
    }

    @Override
    public void Clear() {
        _fileStorage.Clear();
        _memoryStorage.Clear();
    }

    @Override
    public void Close() throws IOException {
        _fileStorage.AddOrUpdate(_memoryStorage.GetElements());
        _memoryStorage.Close();
        _fileStorage.Close();
    }

    public String GetIndexFilePath() {
        return _fileStorage.GetIndexFilePath();
    }

    public String[] GetStorageFilePaths() {
        return _fileStorage.GetStorageFilePaths();
    }
}
