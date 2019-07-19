package com.transactions.store.controller.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.transactions.store.controller.exception.FutureTransactionException;
import com.transactions.store.controller.exception.OlderTransactionException;
import com.transactions.store.controller.exception.ParseableTransactionException;
import com.transactions.store.dto.StatisticDTO;
import com.transactions.store.dto.TransactionDTO;
import com.transactions.store.model.Statistic;
import com.transactions.store.model.Transaction;

/**
 * Converter utility
 * 
 * @author onoriel
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ConverterUtils {
	
	/**
	 * Time to keep statistics alive
	 */
	@Value("${statistic.time:60}") 
	private Long timeToKeepAlive;

	
	public StatisticDTO asStatisticDTO(Statistic statistic) {
		Objects.requireNonNull(statistic);
		StatisticDTO statisticDTO = new StatisticDTO();
		statisticDTO.setAvg(roundBigDecimal(statistic.getAvg()).toString());
		statisticDTO.setMax(roundBigDecimal(statistic.getMax()).toString());
		statisticDTO.setMin(roundBigDecimal(statistic.getMin()).toString());
		statisticDTO.setSum(roundBigDecimal(statistic.getSum()).toString());
		statisticDTO.setCount(Objects.isNull(statistic.getCount()) ? NumberUtils.LONG_ZERO : statistic.getCount());
		return statisticDTO;
	}
	
	public Transaction asTrasanctionEntity(TransactionDTO transactionDTO) {
		Objects.requireNonNull(transactionDTO);
		Transaction transaction = new Transaction();
		transaction.setAmount(parseToBigDecimal(transactionDTO.getAmount()));
		transaction.setTimestamp(parseToDateTimeLong(transactionDTO.getTimestamp()));
		return transaction;
	}
	 
	private BigDecimal parseToBigDecimal(String amount) {
		BigDecimal parsedAmount = null;
		try {
			parsedAmount = new BigDecimal (amount);
		}catch(NumberFormatException numberFormatException) {
			throw new ParseableTransactionException();
		}
		return parsedAmount;
	}
	
	private Long parseToDateTimeLong(String datetime) throws ParseableTransactionException, FutureTransactionException, OlderTransactionException {
		Long parsedLong = null;
		try {
			 ZonedDateTime dateTime = ZonedDateTime.parse(datetime).toInstant().atZone(ZoneOffset.UTC);
			 checkFutureTransaction(dateTime);
			 checkPastTransaction(dateTime);
			 parsedLong = dateTime.toEpochSecond();
		}catch(DateTimeParseException dateTimeParseException) {
			throw new ParseableTransactionException();
		}
		return parsedLong;
	}
	private void checkFutureTransaction(ZonedDateTime dateTime) {
		ZonedDateTime limitTime = Instant.now().atZone(ZoneOffset.UTC);
		if( dateTime.isAfter(limitTime)) {
			 throw new FutureTransactionException();
		}
	}
	private void checkPastTransaction(ZonedDateTime dateTime) {
		ZonedDateTime limitTime = Instant.now().minusSeconds(timeToKeepAlive).atZone(ZoneOffset.UTC);
		if( dateTime.isBefore(limitTime)) {
			 throw new OlderTransactionException();
		}
	}
	private BigDecimal roundBigDecimal(BigDecimal initial) {
		if(Objects.isNull(initial))
			return BigDecimal.ZERO.setScale(2); 
		return initial.setScale(2, RoundingMode.HALF_UP);
	}

}
