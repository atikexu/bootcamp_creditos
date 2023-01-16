package com.bootcamp.credits.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.bootcamp.credits.enums.ProductType;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection="creditCard")
public class CreditCard {
	@Id
	private String id;
	@NotEmpty
	private String numberCard;
	@NotEmpty
	private String customerId;

	private Double amountToPay;

	private Double amountPaid;

	private ProductType typeAccount;
	
	private Date creditDate;
	
}
