package com.transactions.store.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@JsonPropertyOrder({ "sum", "avg", "max", "min", "count" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticDTO implements Serializable {

	/**
	 * Serial version
	 */
	private final static long serialVersionUID = 8301295409563430072L;

	/**
	 * the total sum of transaction value in the last 60 seconds
	 */
	@JsonProperty("sum")
	private String sum;
	/**
	 * the average amount of transaction value in the last 60 seconds
	 */
	@JsonProperty("avg")
	private String avg;
	/**
	 * single highest transaction value in the last 60 seconds
	 */
	@JsonProperty("max")
	private String max;
	/**
	 * single lowest transaction value in the last 60 seconds
	 */
	@JsonProperty("min")
	private String min;
	/**
	 * total number of transactions happened in the last 60 seconds
	 */
	@JsonProperty("count")
	private Long count = 0L;
}