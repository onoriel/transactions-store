package com.transactions.store.repository;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.transactions.store.model.Statistic;
import com.transactions.store.model.Transaction;
import com.transactions.store.repository.StatisticsRepository;
import com.transactions.store.repository.StatisticsRepositoryImpl;

/**
 * Statistics repository tests
 * 
 * @author onoriel
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class StatisticsRepositoryTest {

	StatisticsRepository statisticsRepository;

	/**
	 * Time to keep statistics alive
	 */
	Long timeToKeepAlive = 60L;
	
	@Before
    public void setUp() {
		statisticsRepository = new StatisticsRepositoryImpl(timeToKeepAlive);
    }


	/**
	 * When a new transaction is saved with a empty statistics one and only one
	 * aggregate is used
	 * 
	 * @throws Exception
	 */
	@Test
	public void saveTransactionUseOneAggregate() throws Exception {
		statisticsRepository.cleanAllStatistics();
		statisticsRepository.save(getTransaction(), Instant.now().getEpochSecond());
		Map<Long, Statistic> statistics = statisticsRepository.getStatistics();
		int countAggregate = statistics.values().size();
		assertTrue(statistics != null);
		assertTrue(Objects.equals(countAggregate, 1));
	}

	/**
	 * When a transaction is saved its sum property is equals to transaction amount
	 * 
	 * @throws Exception
	 */
	@Test
	public void whenSaveTransactionSumEqualTransactionAmount() throws Exception {
		statisticsRepository.cleanAllStatistics();
		Transaction trans = getTransaction();
		statisticsRepository.save(trans, Instant.now().getEpochSecond());
		BigDecimal statisticSum = statisticsRepository.getStatistics().values().stream().map(Statistic::getSum).reduce(BigDecimal.ZERO, BigDecimal::add);
		assertTrue(statisticSum != null);
		assertTrue(Objects.equals(statisticSum, trans.getAmount()));
	}

	/**
	 * only transactions inside the time to keep them alive remain in statistics
	 * 
	 * @throws Exception
	 */
	@Test
	public void whenSaveSecondTransactionAfterTimeToKeepAliveOnlyLastRemains() throws Exception {
		statisticsRepository.cleanAllStatistics();
		Transaction trans1 = getTransaction();
		statisticsRepository.save(trans1, Instant.now().getEpochSecond());
		Transaction trans2 = getTransaction();
		trans2.setTimestamp(Instant.now().plusSeconds(timeToKeepAlive).toEpochMilli());
		statisticsRepository.save(trans2, Instant.now().plusSeconds(timeToKeepAlive + 1).getEpochSecond());
		BigDecimal statisticSum = statisticsRepository.getStatistics().values().stream().map(Statistic::getSum).reduce(BigDecimal.ZERO, BigDecimal::add);
		assertTrue(statisticSum != null);
		assertTrue(Objects.equals(statisticsRepository.getStatistics().values().size(), 1));
		assertTrue(Objects.equals(statisticSum, trans2.getAmount()));
	}
	
	/**
	 * When 2 transaction with the same time stamp are saved its aggregate is merged with its information
	 * 
	 * @throws Exception
	 */
	@Test
	public void whenSaveSecondTransactionStatisticAggregateIsUpdated() throws Exception {
		statisticsRepository.cleanAllStatistics();
		Transaction trans1 = getTransaction();
		statisticsRepository.save(trans1, Instant.now().getEpochSecond());
		Transaction trans2 = getTransaction();
		statisticsRepository.save(trans2, Instant.now().getEpochSecond());
		Statistic statistic = statisticsRepository.getStatistics().values().stream().reduce(new Statistic(), (statisticA, statisticB) -> {
			statisticA.setSum(statisticA.getSum().add(statisticB.getSum()));
			statisticA.setCount(Long.sum(statisticA.getCount(), statisticB.getCount()));			
			return statisticA;
		});
		int size = statisticsRepository.getStatistics().values().size();
		assertTrue(ObjectUtils.allNotNull(statistic));
		assertTrue(Objects.equals(size, 1));
		assertTrue(Objects.equals(statistic.getSum(),trans1.getAmount().add(trans2.getAmount())));
		assertTrue(Objects.equals(statistic.getCount(),2L));
	}
	
	/**
	 * Testing saving parallel transaction and statistics keep all values
	 */
	@Test
	public void whenParallelTransactionStatsStoreAllValues(){
		statisticsRepository.cleanAllStatistics();
		long time = Instant.now().atZone(ZoneOffset.UTC).toEpochSecond();
		
		IntStream.range(-1, 99).parallel().forEach(i->{
			Transaction t = new Transaction(new BigDecimal(i),new Long(time) );
					statisticsRepository.save(t, Instant.now().atZone(ZoneOffset.UTC).toEpochSecond());
		});
		statisticsRepository.cleanOldStatistics(Instant.now().atZone(ZoneOffset.UTC).toEpochSecond());
		Statistic statistic = statisticsRepository.getStatistics().values().stream().reduce(new Statistic(), (statisticA, statisticB) -> {
			statisticA.setSum(statisticA.getSum().add(statisticB.getSum()));
			statisticA.setCount(Long.sum(statisticA.getCount(), statisticB.getCount()));
			statisticA.setMax(Objects.isNull(statisticA.getMax()) ? statisticB.getMax() : 
				  Objects.isNull(statisticB.getMax()) ? statisticA.getMax() :
					  		   statisticA.getMax().max(statisticB.getMax()));
			statisticA.setMin(Objects.isNull(statisticA.getMin()) ? statisticB.getMin() : 
							  Objects.isNull(statisticB.getMin()) ? statisticA.getMin() :
								  		   statisticA.getMin().min(statisticB.getMin()));
			statisticA.setAvg(statisticA.getCount() > 0 ? statisticA.getSum().divide(new BigDecimal( statisticA.getCount()),  2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
			return statisticA;
		});
		assertTrue(Objects.equals(statistic.getSum(), new BigDecimal(4850)));
		assertTrue(Objects.equals(statistic.getCount(), 100L));
		assertTrue(Objects.equals(statistic.getMin(), new BigDecimal(-1)));
		assertTrue(Objects.equals(statistic.getMax(), new BigDecimal(98)));
	}
	/**
	 * Testing statistic update when transactions have expired 
	 */
	@Test
	public void onlyReaminsTransaciontsInsideValidRangeExperitionTime(){
		statisticsRepository.cleanAllStatistics();
		IntStream.range(0, 10).forEach(i->{
			long time = Instant.now().plusSeconds(i).getEpochSecond();
			Transaction t = new Transaction(new BigDecimal(i),new Long(time) );
					statisticsRepository.save(t, Instant.now().plusSeconds(i).getEpochSecond());
		});
		
		statisticsRepository.cleanOldStatistics(Instant.now().plusSeconds(65L).getEpochSecond());
		Statistic statistic = statisticsRepository.getStatistics().values().stream().reduce(new Statistic(), (statisticA, statisticB) -> {
			statisticA.setSum(statisticA.getSum().add(statisticB.getSum()));
			statisticA.setCount(Long.sum(statisticA.getCount(), statisticB.getCount()));
			statisticA.setMax(Objects.isNull(statisticA.getMax()) ? statisticB.getMax() : 
				  Objects.isNull(statisticB.getMax()) ? statisticA.getMax() :
					  		   statisticA.getMax().max(statisticB.getMax()));
			statisticA.setMin(Objects.isNull(statisticA.getMin()) ? statisticB.getMin() : 
							  Objects.isNull(statisticB.getMin()) ? statisticA.getMin() :
								  		   statisticA.getMin().min(statisticB.getMin()));
			statisticA.setAvg(statisticA.getCount() > 0 ? statisticA.getSum().divide(new BigDecimal( statisticA.getCount()),  2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
			return statisticA;
		});
		assertTrue(Objects.equals(statistic.getSum(), new BigDecimal(30)));
		assertTrue(Objects.equals(statistic.getCount(), 4L));
		assertTrue(Objects.equals(statistic.getMin(), new BigDecimal(6)));
		assertTrue(Objects.equals(statistic.getMax(), new BigDecimal(9)));
	}
	
	private Transaction getTransaction() { 
		return new Transaction(new BigDecimal(100), ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()); 
	}
	
}
