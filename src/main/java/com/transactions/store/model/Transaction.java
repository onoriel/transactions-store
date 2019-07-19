package com.transactions.store.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

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
@NoArgsConstructor
public class Transaction implements Serializable {
	 

	private final static long serialVersionUID = -6967216046036895149L;
	
	@NotNull
	private BigDecimal amount;
	@NotNull
	private Long timestamp;
}
