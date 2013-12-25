package DatabaseServer.dataStorage;

import DatabaseBase.entities.WrappedKeyValue;
import DatabaseBase.exceptions.DataStorageException;
import DatabaseBase.interfaces.ISizable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 12/25/13
 * Time: 9:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class CombinedDataStorageWithMemoryProfiling<TKey extends ISizable, TValue extends ISizable> extends CombinedDataStorage<TKey, TValue> {
    Runtime _runtime;
    private long _currentInMemoryItemsCount;
    private final long _systemMinMemoryRequirement = 1024 * 1024 * 20;

    public CombinedDataStorageWithMemoryProfiling(String baseDirectory, String filePrefix, int fileSplitSize, int memorySize) throws IOException, ClassNotFoundException, DataStorageException {
        super(baseDirectory, filePrefix, fileSplitSize, memorySize);
        _runtime = Runtime.getRuntime();
        long maxMemory = _runtime.maxMemory();
        if (memorySize + _systemMinMemoryRequirement > maxMemory) {
            throw new DataStorageException("Requested " + memorySize + " memory + " + _systemMinMemoryRequirement +
                    " needed for normal system execution, but available only: " + maxMemory +
                    ". Check your JVM settings");
        }
    }

    @Override
    protected void AddItemToMemory(WrappedKeyValue<TKey, TValue> item) throws IOException {
        if (item.IsDeleted) {
            _memoryStorage.Delete(item.Key);
            _currentInMemoryItemsCount--;
        } else {
            _memoryStorage.AddOrUpdate(item.Key, item.Value);
            _currentInMemoryItemsCount++;
        }
        if (!_freqElements.contains(item.Key)) {
            _freqElements.add(item.Key);
        }

        checkAndReduceIfNeeded(_currentInMemoryItemsCount / 2);
        checkAndReduceIfNeeded(0);
    }

    private void checkAndReduceIfNeeded(long inMemoryItemsCountToSave) throws IOException {
        final long usedMemory = _runtime.totalMemory() - _runtime.freeMemory();

        if (usedMemory >= _maxMemorySize) {
            List<WrappedKeyValue<TKey, TValue>> items = new ArrayList<WrappedKeyValue<TKey, TValue>>();
            while (_currentInMemoryItemsCount > inMemoryItemsCountToSave) {
                WrappedKeyValue<TKey, TValue> item = _memoryStorage.Get(_freqElements.poll());
                items.add(item);
                _currentInMemoryItemsCount--;
            }
            _fileStorage.AddOrUpdate(items);
            for (int i = 0; i < items.size(); i++) {
                _memoryStorage.RemoveCompletly(items.get(i).Key);
            }
            items = null;
        }

        System.gc();
    }
}
