package com.transactions.store.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.transactions.store.model.Statistic;
import com.transactions.store.model.Transaction;
import com.transactions.store.repository.StatisticsRepository;

/**
 * Transactions service
 * 
 * @author onoriel
 *
 */
@Service
public class TransactionService {
	
	@Autowired
	private StatisticsRepository statisticsRepository;
	
	/**
	 * Time to keep statistics alive
	 */
	@Value("${statistic.time:60}") 
	private Long timeToKeepAlive;
	
	
	public void save(Transaction transaction) { 
		statisticsRepository.save(transaction, Instant.now().getEpochSecond());
	}
	
	public Statistic getStatistics() { 
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
		return statistic;
	}
	
	public void deleteStatistics() {
		statisticsRepository.cleanAllStatistics();
	}

}
