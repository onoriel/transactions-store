package com.transactions.store.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.transactions.store.model.Statistic;
import com.transactions.store.model.Transaction;

/**
 * Statistics repository implementation
 * @author onoriel
 *
 */
@Repository
public class StatisticsRepositoryImpl implements StatisticsRepository{
	
	protected Map<Long, Statistic> statistics = new ConcurrentHashMap<>();
	
	
	/**
	 * Time to keep alive statistic
	 */
	private Long timeToKeepAlive;
	
	public StatisticsRepositoryImpl(@Value("${statistic.time}") Long timeToKeepAlive) {
		this.timeToKeepAlive = timeToKeepAlive;
	}
	
	@Override
	public void cleanAllStatistics() {
		statistics.clear();
	}
	
	@Override
	public Map<Long, Statistic>  getStatistics() {
		return statistics;
	}

	@Override
	public void save(Transaction transaction, Long currentTime) {
		cleanOldStatistics(currentTime);
		statistics.merge(transaction.getTimestamp(), updateStatistic(transaction, new Statistic()), (statiscticA,statiscticB) -> updateStatistic(transaction, statiscticA));
	}

	@Override
	public void cleanOldStatistics(Long currentTime) {
		statistics.keySet().removeIf(statisticKey ->  currentTime - statisticKey >=	 timeToKeepAlive);
	}

	@Override
	public Statistic updateStatistic(Transaction transaction, Statistic statistic) {
		statistic.setTimestamp(transaction.getTimestamp());
		statistic.setSum(statistic.getSum().add(transaction.getAmount()));
		statistic.setMax( !Objects.isNull(statistic.getMax()) ?  statistic.getMax().max(transaction.getAmount()) : transaction.getAmount());
		statistic.setMin( !Objects.isNull(statistic.getMin()) ?  statistic.getMin().min(transaction.getAmount()) : transaction.getAmount());
		statistic.setCount(statistic.getCount() + 1);
		statistic.setAvg(statistic.getSum().divide(new BigDecimal(statistic.getCount()),  2, RoundingMode.HALF_UP));
		
		return statistic;
	}
	
}
