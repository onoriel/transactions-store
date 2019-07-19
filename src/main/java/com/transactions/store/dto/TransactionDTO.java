package com.transactions.store.dto;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


/**
 * Transaction DTO
 * 
 * @author onoriel
 *
 */
@Data
@ToString(includeFieldNames=true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "amount", "timestamp" })
@NoArgsConstructor
public class TransactionDTO implements Serializable {
	
	private final static long serialVersionUID = -6967216046036895149L;
	
	@NotEmpty
	private String amount;
	@NotEmpty
	private String timestamp;	
}
