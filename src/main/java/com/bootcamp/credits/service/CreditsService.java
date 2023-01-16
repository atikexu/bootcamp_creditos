package com.bootcamp.credits.service;

import com.bootcamp.credits.model.Credits;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditsService {
	
	public Flux<Credits> findAll();
		
	public Mono<Credits> findById(String id);
	
	public Mono<Credits> save(Credits credits);
	
	public Mono<Void> delete(Credits credits); 
	
}
