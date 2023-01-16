package com.bootcamp.credits.service;

import com.bootcamp.credits.model.CreditCard;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditCardService {
	
	public Flux<CreditCard> findAll();
		
	public Mono<CreditCard> findById(String id);
	
	public Mono<CreditCard> save(CreditCard creditCard);
	
	public Mono<Void> delete(CreditCard creditCard); 
	
}
