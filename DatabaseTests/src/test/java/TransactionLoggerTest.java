import DatabaseBase.components.TransactionLogger;
import DatabaseBase.entities.IntegerSizable;
import DatabaseBase.entities.Query;
import DatabaseBase.exceptions.TransactionException;
import DatabaseServer.dataStorage.MemoryBasedDataStorage;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 2:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionLoggerTest {
    TransactionLogger _transactionLogger;

    @Before
    public void setUp() throws IOException {
        _transactionLogger = new TransactionLogger(new MemoryBasedDataStorage<IntegerSizable, Query>());
    }

    @Test
    public void AddTransaction_Default_Added() throws IOException, TransactionException {
        //arrange
        Query query = new Query();

        //act
        _transactionLogger.AddTransaction(query);

        //assert
        List<Query> transactions = _transactionLogger.GetUnCommittedTransactions();
        assertEquals(query, transactions.get(0));
    }

    @Test
    public void CommitTransaction_QueryNotAdded_TransactionException() throws IOException {
        //arrange
        Query query = new Query();
        boolean hasException = false;

        //act
        try {
            _transactionLogger.CommitTransaction(query, true);
        } catch (TransactionException e) {
            hasException = true;
        }

        //assert
        assertEquals(true, hasException);
    }

    @Test
    public void GetCommittedTransactionsAfter_Uncommitted_ZeroSize() throws IOException, TransactionException {
        //arrange
        Query query = new Query();

        //act
        _transactionLogger.AddTransaction(query);

        //assert
        List<Query> transactions = _transactionLogger.GetCommittedTransactionsAfter(0);
        assertEquals(0, transactions.size());
    }

    @Test
    public void GetUnCommittedTransactions_QueryNotAdded_TransactionException() throws IOException {
        //arrange
        Query query = new Query();
        boolean hasException = false;

        //act
        try {
            _transactionLogger.CommitTransaction(query, true);
        } catch (TransactionException e) {
            hasException = true;
        }

        //assert
        List<Query> transactions = _transactionLogger.GetUnCommittedTransactions();
        assertEquals(0, transactions.size());
        assertEquals(true, hasException);
    }

    @Test
    public void GetCommittedTransactionsAfter_AddCommit_Committed() throws IOException, TransactionException {
        //arrange
        Query query = new Query();

        //act
        _transactionLogger.AddTransaction(query);
        _transactionLogger.CommitTransaction(query, true);

        //assert
        List<Query> transactions = _transactionLogger.GetCommittedTransactionsAfter(0);
        assertEquals(query, transactions.get(0));
    }

    @Test
    public void GetUnCommittedTransactions_AddCommit_ZeroSize() throws IOException, TransactionException {
        //arrange
        Query query = new Query();

        //act
        _transactionLogger.AddTransaction(query);
        _transactionLogger.CommitTransaction(query, true);

        //assert
        List<Query> transactions = _transactionLogger.GetUnCommittedTransactions();
        assertEquals(0, transactions.size());
    }
}
