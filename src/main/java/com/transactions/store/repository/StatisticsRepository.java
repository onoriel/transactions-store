package com.transactions.store.repository;

import java.util.Map;

import com.transactions.store.model.Statistic;
import com.transactions.store.model.Transaction;

/**
 * Statistic repository interface
 * @author onoriel
 *
 */
public interface StatisticsRepository {
	
	/**
	 * Statistic repository initialization
	 */
	void cleanAllStatistics();
	
	/**
	 * Get statistics
	 * @return statistics collection
	 */
	Map<Long, Statistic> getStatistics();
	
	/**
	 * Save transaction
	 * @param transaction
	 * @param newLastTransactionTime
	 */
	void save(Transaction transaction, Long newLastTransactionTime);
		
	/**
	 * clean statistics repository from old statistics
	 * 
	 * @param reference time 
	 */
	void cleanOldStatistics(Long newLastTransactionTime);
	
	/**
	 * Merge statistic with a new transaction
	 * @param transaction
	 * @param statistic
	 * 
	 */
	Statistic updateStatistic(Transaction transaction, Statistic statistic);

}
