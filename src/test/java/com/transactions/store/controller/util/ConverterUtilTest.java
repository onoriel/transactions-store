package com.transactions.store.controller.util;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.transactions.store.controller.exception.FutureTransactionException;
import com.transactions.store.controller.exception.OlderTransactionException;
import com.transactions.store.controller.exception.ParseableTransactionException;
import com.transactions.store.controller.util.ConverterUtils;
import com.transactions.store.dto.StatisticDTO;
import com.transactions.store.dto.TransactionDTO;
import com.transactions.store.model.Statistic;
import com.transactions.store.model.Transaction;

/**
 * Transactions service tests
 * 
 * @author onoriel
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConverterUtilTest {

	
	ConverterUtils converterUtils;
	
	Long timeToKeepAlive = 60L;
	
	@Before
	public void init() {
		converterUtils = new ConverterUtils();
		ReflectionTestUtils.setField(converterUtils, "timeToKeepAlive", timeToKeepAlive);
	}
	
	
	@Test
	public void testEmptyStatisticDTOGeneration() throws Exception {
		StatisticDTO statisticDTO = converterUtils.asStatisticDTO(new Statistic());
		assertTrue(!Objects.isNull(statisticDTO));
		assertTrue(Objects.equals(statisticDTO.getCount(), NumberUtils.LONG_ZERO));
		assertTrue(Objects.equals(statisticDTO.getSum(), "0.00"));
	}
	
	@Test
	public void testTransactionGeneration() throws Exception {
		Transaction transaction = converterUtils.asTrasanctionEntity(getTransactionDTO());
		assertTrue(!Objects.isNull(transaction));
		assertTrue(transaction.getAmount().compareTo(new BigDecimal(100.00)) == 0);
		assertTrue(Objects.equals(transaction.getTimestamp(), ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()));
	}
	
	@Test(expected=NullPointerException.class)
	public void testTransactionGenerationWhenNullObject() throws Exception {
		converterUtils.asTrasanctionEntity(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void testTransactionDTOGenerationWhenNullObject() throws Exception {
		converterUtils.asStatisticDTO(null);
	}
	
	@Test(expected=ParseableTransactionException.class)
	public void testParseableAmountValidationForTransactionModel() throws Exception {
		TransactionDTO transaction = getTransactionDTO();
		transaction.setAmount("100FFF00");
		converterUtils.asTrasanctionEntity(transaction);
		
	}
	@Test(expected=ParseableTransactionException.class)
	public void testParseableTimeStampValidationForTransactionModel() throws Exception {
		TransactionDTO transaction = getTransactionDTO();
		transaction.setTimestamp("HHHHHH");
		converterUtils.asTrasanctionEntity(transaction);
		
	}
	@Test(expected=FutureTransactionException.class)
	public void testFutureTimeStampValidationForTransactionModel() throws Exception {
		converterUtils.asTrasanctionEntity(getFutureTransactionDTO(getTransactionDTO()));
	}
	@Test(expected=OlderTransactionException.class)
	public void testOldTimeStampValidationForTransactionModel() throws Exception {
		converterUtils.asTrasanctionEntity(getOldTransactionDTO(getTransactionDTO()));
	}
	
	private TransactionDTO getTransactionDTO() {
		ZonedDateTime timstamp = ZonedDateTime.now(ZoneOffset.UTC);
		return new TransactionDTO("100.00", timstamp.toString()); 
	}
	private TransactionDTO getOldTransactionDTO(TransactionDTO transaction) {
		transaction.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(61).toString());
		return transaction;
	}
	private TransactionDTO getFutureTransactionDTO(TransactionDTO transaction) {
		transaction.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(61).toString());
		return transaction; 
	}	

}
