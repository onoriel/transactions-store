package com.transactions.store;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.store.dto.TransactionDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TransactionIntegrationTest {
	
	/**
	 * Mock Mvc
	 */
	private MockMvc endpoint;

	/**
	 * Object Mapper
	 */
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
    private GenericWebApplicationContext webApplicationContext;
	
	/**
	 * Save transaction endpoint
	 */
	private static final String SAVE_TRANSACTION_ENDPOINT = "/transactions";

	/**
	 * Save transaction endpoint
	 */
	private static final String STATISTICS_ENDPOINT = "/statistics";
	
	/**
	 * Delete transactions endpoint
	 */
	private static final String DELETE_STATISTICS_ENDPOINT = "/transactions";

	@Before
	public void init() {
		endpoint = webAppContextSetup(webApplicationContext).build();
        assertNotNull(endpoint);
	}
	
	/**
	 * Testing controller always returns a 201 HTTP Code when a transaction is
	 * created
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaveTransactionCreated() throws Exception {
		
		String transactionContent = objectMapper.writeValueAsString(getTransaction());
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isCreated());
	}
	/**
	 * Testing controller always returns a 204 HTTP Code when a transaction time stamp is expired
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaveTransactionOldTimeStamp() throws Exception {
		String transactionContent = objectMapper.writeValueAsString(getOldTransactionDTO(getTransaction()));
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isNoContent());
	}
	
	/**
	 * Testing controller always returns a 422 HTTP Code when the transaction date is in the future
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaveTransactionFutureTimeStamp() throws Exception {
		String transactionContent = objectMapper.writeValueAsString(getFutureTransactionDTO(getTransaction()));
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
	}
	
	/**
	 * A 404 HTTP Code is returned when a not content is sent
	 * stamp is expired
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
	 * stamp is expired
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBadRequestInvalidTransactionSent() throws Exception {
		String transactionContent = objectMapper.writeValueAsString(getTransaction());
		transactionContent = transactionContent.replaceFirst("100.00", "");
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	/**
	 * A 422 HTTP Code is returned if the amount value is not parsable
	 * 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBadParseableAmountTransactionInformation() throws Exception {
		TransactionDTO transaction = getTransaction();
		transaction.setAmount("100FFF00");
		String transactionContent = objectMapper.writeValueAsString(transaction);
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
	}
	/**
	 * A 422 HTTP Code is returned if the timestamp value is not parsable
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBadParseableTimeStampTransactionInformation() throws Exception {
		TransactionDTO transaction = getTransaction();
		transaction.setTimestamp("HHHHHH");
		String transactionContent = objectMapper.writeValueAsString(transaction);
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(transactionContent))
				.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
	}

	/**
	 * Testing an new empty statistic is returned when no transaction saved before
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStatisticsCeroValuesWhenNoTransactionSavedBefore() throws Exception {
		endpoint.perform(MockMvcRequestBuilders.delete(STATISTICS_ENDPOINT).contentType(MediaType.APPLICATION_JSON));
		MvcResult result = endpoint
				.perform(MockMvcRequestBuilders.get(STATISTICS_ENDPOINT).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
		String response = result.getResponse().getContentAsString();
		assertTrue(response.contains("\"count\":0"));
	}
	
	/**
	 * Testing a statistic is returned when one transaction is in memory
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStatisticsOneTransactionSaved() throws Exception {
		endpoint.perform(MockMvcRequestBuilders.delete(DELETE_STATISTICS_ENDPOINT).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isNoContent());
		endpoint.perform(MockMvcRequestBuilders.post(SAVE_TRANSACTION_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(getTransaction())))
				.andExpect(MockMvcResultMatchers.status().isCreated());
		MvcResult result = endpoint
				.perform(MockMvcRequestBuilders.get(STATISTICS_ENDPOINT).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
		String response = result.getResponse().getContentAsString();
		assertTrue(response.contains("\"count\":1"));
	}

	private TransactionDTO getTransaction() {
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
