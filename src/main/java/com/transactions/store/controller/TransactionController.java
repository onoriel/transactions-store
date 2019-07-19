package com.transactions.store.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transactions.store.controller.exception.FutureTransactionException;
import com.transactions.store.controller.exception.OlderTransactionException;
import com.transactions.store.controller.exception.ParseableTransactionException;
import com.transactions.store.controller.util.ConverterUtils;
import com.transactions.store.dto.StatisticDTO;
import com.transactions.store.dto.TransactionDTO;
import com.transactions.store.service.TransactionService;

import lombok.extern.log4j.Log4j2;

/**
 * Transaction Controller 
 * 
 * @author onoriel
 *
 */
@RestController
@Log4j2
public class TransactionController {
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private ConverterUtils converterUtils;

	/**
	 * Save transaction end point
	 * @param transaction
	 * @return HTTP Code Status
	 * @throws OlderTransactionException, ParseableTransactionException, FutureTransactionException
	 */
	@PostMapping(value = "/transactions")
	public ResponseEntity<Void> saveTransaction(@RequestBody @Valid @NotNull TransactionDTO transaction){
		log.debug("saveTransaction: new transaction to save [{}]", transaction );
		transactionService.save(converterUtils.asTrasanctionEntity(transaction));
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	/**
	 * Statistics generation end point
	 * @return statistics information
	 */
	@GetMapping(value = "/statistics")
	public ResponseEntity<StatisticDTO> generateStatistics() {
		return new ResponseEntity<StatisticDTO>(converterUtils.asStatisticDTO(transactionService.getStatistics()), HttpStatus.OK);
	}
	/**
	 * Statistics removal end point
	 * @return
	 */
	@DeleteMapping(value = "/transactions")
	public ResponseEntity<Void> deleteTransactions() {
		transactionService.deleteStatistics(); 
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
	
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler({ParseableTransactionException.class, FutureTransactionException.class})
	public void parseableAndFutureExceptionHandler(Exception exception) {
	  
	}
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ExceptionHandler({OlderTransactionException.class})
	public void olderExceptionHanlder(Exception exception) {
	  
	}
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({MethodArgumentNotValidException.class})
	public void argumentExceptionHanlder(Exception exception) {
	  
	}
}
