package com.transactions.store.controller;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.store.controller.TransactionController;
import com.transactions.store.controller.exception.FutureTransactionException;
import com.transactions.store.controller.exception.OlderTransactionException;
import com.transactions.store.controller.exception.ParseableTransactionException;
import com.transactions.store.controller.util.ConverterUtils;
import com.transactions.store.dto.StatisticDTO;
import com.transactions.store.dto.TransactionDTO;
import com.transactions.store.model.Statistic;
import com.transactions.store.model.Transaction;
import com.transactions.store.service.TransactionService;

@RunWith(MockitoJUnitRunner.class)
public class TransactionControllerTest {

	@Mock
	private TransactionService transactionService;

	@InjectMocks
	private TransactionController transactionController;

	/**
	 * Mock Mvc
	 */
	private MockMvc endpoint;

	/**
	 * Object Mapper utility
	 */
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Mock
	private ConverterUtils converterUtils;
	
	private static final String SAVE_TRANSACTION_ENDPOINT = "/transactions";
	private static final String STATISTICS_ENDPOINT = "/statistics";

	@Before
	public void init() {
		// Process mock annotations
		MockitoAnnotations.initMocks(this);
		endpoint = MockMvcBuilders.standaloneSetup(transactionController).build();
	}

	/**
	 * Testing controller always returns a 201 HTTP Code when a transaction is
	 * created
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaveTransactionCreated() throws Exception {
		Mockito.doNothing().when(transactionService).save(Mockito.any(Transaction.class));
		Mockito.doReturn(new Transaction()).when(converterUtils).asTrasanctionEntity(Mockito.any());
		String transactionContent = objectMapper.writeValueAsString(getTransactionDTO());
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isCreated());
	}

	/**
	 * Testing controller always returns a 204 HTTP Code when a transaction time
	 * stamp is expired
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaveTransactionOldTimeStamp() throws Exception {
		Mockito.doThrow(new OlderTransactionException()).when(converterUtils).asTrasanctionEntity(Mockito.any());
		String transactionContent = objectMapper.writeValueAsString(getTransactionDTOOlder());
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isNoContent());
	}
	/**
	 * Testing controller always returns a 422 HTTP Code when a transaction time
	 * stamp is in the future
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaveTransactionFutureTimeStamp() throws Exception {
		Mockito.doThrow(new FutureTransactionException()).when(converterUtils).asTrasanctionEntity(Mockito.any());
		String transactionContent = objectMapper.writeValueAsString(getTransactionDTOOlder());
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
	}
	/** 422 HTTP Code error when unparsable amount
	 * @throws Exception
	 */
	@Test
	public void testUnparseableAmountTransaction() throws Exception {
		Mockito.doThrow(new ParseableTransactionException()).when(converterUtils).asTrasanctionEntity(Mockito.any());
		TransactionDTO transaction = getTransactionDTO();
		transaction.setAmount("100FFF00");
		String transactionContent = objectMapper.writeValueAsString(transaction);
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
	}
	
	/** 422 HTTP Code error when unparsable timestamp
	 * @throws Exception
	 */
	@Test
	public void testUnparseableTimeStampTransaction() throws Exception {
		Mockito.doThrow(new ParseableTransactionException()).when(converterUtils).asTrasanctionEntity(Mockito.any());
		TransactionDTO transaction = getTransactionDTO();
		transaction.setTimestamp("HHHHHH");
		String transactionContent = objectMapper.writeValueAsString(transaction);
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
	}
	
	/**
	 * A 400 HTTP Code is returned when a not content is sent
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBadRequestNoContentSent() throws Exception {
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	/**
	 * A 404 HTTP Code is returned when a not valid transaction is sent
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBadRequestWhenInvalidIsTransactionSent() throws Exception {
		String transactionContent = objectMapper.writeValueAsString(getTransactionDTO());
		transactionContent = transactionContent.replaceFirst("100.00", "");
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * Testing an new empty statistic is returned when no transaction saved before
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStatisticsCeroValuesWhenNoTransactionSavedBefore() throws Exception {
		Mockito.doReturn(new Statistic()).when(transactionService).getStatistics();
		Mockito.doReturn(new StatisticDTO()).when(converterUtils).asStatisticDTO(Mockito.any());
		MvcResult result = endpoint
				.perform(MockMvcRequestBuilders.get(STATISTICS_ENDPOINT).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
		String response = result.getResponse().getContentAsString();
		assertTrue(response.equals(objectMapper.writeValueAsString(new StatisticDTO())));
	}
	
	/**
	 * Testing a statistic is returned when one transaction is in memory
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStatisticsOneTransactionSaved() throws Exception {
		Statistic statistic = getStatistic(getTransaction());
		Mockito.doReturn(statistic).when(transactionService).getStatistics();
		Mockito.doReturn(asStatisticDTO(statistic)).when(converterUtils).asStatisticDTO(Mockito.any());
		MvcResult result = endpoint
				.perform(MockMvcRequestBuilders.get(STATISTICS_ENDPOINT).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
		String response = result.getResponse().getContentAsString();
		assertTrue(response.contains("\"count\":1"));
	}

	/**
	 * Build a transaction
	 * @return
	 */
	private Transaction getTransaction() {
		return new Transaction(new BigDecimal(100), ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()); 
	}
	/**
	 * Build a transaction
	 * @return
	 */
	private TransactionDTO getTransactionDTO() {
		ZonedDateTime timstamp = ZonedDateTime.now(ZoneOffset.UTC);
		return new TransactionDTO("100.00", timstamp.toString()); 
	}
	private TransactionDTO getTransactionDTOOlder() {
		ZonedDateTime timstamp = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(61);
		return new TransactionDTO("100.00", timstamp.toString()); 
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
	public StatisticDTO asStatisticDTO(Statistic statistic) {
		StatisticDTO statisticDTO = new StatisticDTO();
		statisticDTO.setAvg(statistic.getAvg().setScale(2, RoundingMode.HALF_UP).toString());
		statisticDTO.setMax(statistic.getMax().setScale(2, RoundingMode.HALF_UP).toString());
		statisticDTO.setMin(statistic.getMin().setScale(2, RoundingMode.HALF_UP).toString());
		statisticDTO.setSum(statistic.getSum().setScale(2, RoundingMode.HALF_UP).toString());
		statisticDTO.setCount(statistic.getCount());
		return statisticDTO;
	}
}
