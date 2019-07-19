package com.transactions.store.model;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang3.math.NumberUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * Statistic DTO
 * 
 * @author onoriel
 *
 */
@Data
@ToString(includeFieldNames=true)
@AllArgsConstructor	
public class Statistic implements Serializable {
	
	/**
	 * Serial version
	 */
	private final static long serialVersionUID = 8301295409563430072L;
	
	/**
	 * Long time (Seconds) 
	 */
	private Long timestamp;
	
	/**
	 * the total sum of transaction value in the last 60 seconds
	 */
	private BigDecimal sum;
	/**
	 * the average amount of transaction value in the last 60 seconds
	 */
	private BigDecimal avg;
	/**
	 * single highest transaction value in the last 60 seconds
	 */
	private BigDecimal max;
	/**
	 * single lowest transaction value in the last 60 seconds
	 */
	private BigDecimal min;
	/**
	 * total number of transactions happened in the last 60 seconds
	 */
	private Long count;
	
	public Statistic() {
		reset();
	}
	public void reset() {
		this.avg = BigDecimal.ZERO;
		this.count = NumberUtils.LONG_ZERO;
		this.sum = BigDecimal.ZERO;
		this.timestamp = NumberUtils.LONG_ZERO;
	}
	
	
}