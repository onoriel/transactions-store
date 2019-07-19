package com.transactions.store.service;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.transactions.store.model.Statistic;
import com.transactions.store.model.Transaction;
import com.transactions.store.repository.StatisticsRepository;
import com.transactions.store.service.TransactionService;

/**
 * Transactions service tests
 * 
 * @author onoriel
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {

	@InjectMocks
	TransactionService transactionService;

	@Mock
	StatisticsRepository statisticsRepository;
	
	/**
	 * Time to keep statistics alive
	 */
	Long timeToKeepAlive = 60L;

	/**
	 * Mocks initialization
	 */
	@Before
	public void init() {
		// Process mock annotations
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(transactionService, "timeToKeepAlive", timeToKeepAlive);
	}
	
	/**
	 * When no transaction is saved previously statistic response is zero values
	 * @throws Exception
	 */
	@Test
	public void testNotTransactionsStatistic() throws Exception {
		Mockito.doReturn(new ConcurrentHashMap<>()).when(statisticsRepository).getStatistics();
		Statistic statistic = transactionService.getStatistics();
		assertTrue(statistic != null);
		assertTrue(Objects.equals(statistic.getCount(), NumberUtils.LONG_ZERO));
		assertTrue(Objects.equals(statistic.getSum(), BigDecimal.ZERO));
	}
	@Test
	public void testDeleteAllStatistic() throws Exception {
		transactionService.deleteStatistics();
		Statistic statistic = transactionService.getStatistics();
		assertTrue(statistic != null);
		assertTrue(Objects.equals(statistic.getCount(), NumberUtils.LONG_ZERO));
		assertTrue(Objects.equals(statistic.getSum(), BigDecimal.ZERO));
	}
	@Test
	public void testSaveTransaction() throws Exception {
		transactionService.save(getTransaction());
		Mockito.doReturn(getStatistics(getTransaction(), new ConcurrentHashMap<Long, Statistic>())).when(statisticsRepository).getStatistics();
		Statistic statistic = transactionService.getStatistics();
		assertTrue(statistic != null);
		assertTrue(Objects.equals(statistic.getCount(), 1L));
		assertTrue(Objects.equals(statistic.getSum(), new BigDecimal(100)));
	}
	/**
	 * When no transaction is saved previously statistic response is zero values
	 * @throws Exception
	 */
	@Test
	public void testStatisticWhenTransactionIsSaved() throws Exception {
		Mockito.doReturn(getStatistics(getTransaction(), new ConcurrentHashMap<Long, Statistic>())).when(statisticsRepository).getStatistics();
		Statistic statistic = transactionService.getStatistics();
		assertTrue(statistic != null);
		assertTrue(Objects.equals(statistic.getCount(), 1L));
		assertTrue(Objects.equals(statistic.getSum(), new BigDecimal(100)));
	}
	
	/**
	 * Statistics calculation are made when two different statistics were saved before 
	 * @throws Exception
	 */
	@Test
	public void testStatisticWhenTwoTransactionAreSaved() throws Exception {
		Transaction transaction = getTransaction();
		Map<Long, Statistic> statistics = new ConcurrentHashMap<Long, Statistic>();
		Statistic statistic = getStatistics(transaction, statistics).get(transaction.getTimestamp());
		statistic = mergeStatistic(statistic , getTransaction());
		statistics.put(statistic.getTimestamp(), statistic);
		Mockito.doReturn(statistics).when(statisticsRepository).getStatistics();
		Mockito.doNothing().when(statisticsRepository).cleanOldStatistics(Mockito.anyLong());
		Statistic statisticResponse = transactionService.getStatistics();
		
		assertTrue(statisticResponse != null);
		assertTrue(Objects.equals(statisticResponse.getCount(), 2L));
		assertTrue(Objects.equals(statisticResponse.getSum(), new BigDecimal(200)));
	}
	/**
	 * Statistics calculation are made when two transactions were saved at the same statistic
	 * @throws Exception
	 */
	@Test
	public void testStatisticWhenTwoTransactionAreSavedInTheSameStatistic() throws Exception {
		Transaction transaction = getTransaction();
		Statistic statistic = getStatistic(transaction);
		statistic = mergeStatistic(statistic, getTransaction() );
		Map<Long, Statistic> statistics = new ConcurrentHashMap<Long, Statistic>();
		statistics.put(statistic.getTimestamp(), statistic);
		Mockito.doReturn(statistics).when(statisticsRepository).getStatistics();
		Mockito.doNothing().when(statisticsRepository).cleanOldStatistics(Mockito.anyLong());
		Statistic statisticResponse = transactionService.getStatistics();
		
		assertTrue(statistic != null);
		assertTrue(Objects.equals(statisticResponse.getCount(), 2L));
		assertTrue(Objects.equals(statisticResponse.getSum(), new BigDecimal(200)));
	}
	/**
	 * Build a transaction
	 * @return
	 */
	private Transaction getTransaction() { 
		return new Transaction(new BigDecimal(100), ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()); 
	}

	/**
	 * Build a new statistic object from a transaction
	 * @param t
	 * @return
	 */
	private Statistic getStatistic(Transaction t) {
		Statistic statistic = new Statistic();
		statistic.setAvg(BigDecimal.ONE);
		statistic.setCount(1L);
		statistic.setSum(t.getAmount());
		statistic.setMax(t.getAmount());
		statistic.setMin(t.getAmount());
		statistic.setTimestamp(t.getTimestamp());
		return statistic;
	}
	/** 
	 * Generates statistics list
	 * @param t
	 * @return
	 */
	private Map<Long,Statistic> getStatistics(Transaction t, Map<Long,Statistic> response){
		Statistic statistic = getStatistic(t);
		if(Objects.isNull(response))
			response = new ConcurrentHashMap<Long, Statistic>();
		response.put(statistic.getTimestamp(),statistic);
		return response;
		
	}
	public Statistic mergeStatistic(Statistic statistic, Transaction transaction) {
		statistic.setSum(statistic.getSum().add(transaction.getAmount()));
		statistic.setCount(statistic.getCount() + 1);
		statistic.setMax(statistic.getMax().max(transaction.getAmount()));
		statistic.setMin(statistic.getMin().min(transaction.getAmount()));
		statistic.setAvg(statistic.getSum().divide(new BigDecimal(statistic.getCount())));
		return statistic;
	}

}
