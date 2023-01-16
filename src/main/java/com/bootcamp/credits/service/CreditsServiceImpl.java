package com.bootcamp.credits.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.bootcamp.credits.dto.TransactionCreateRequest;
import com.bootcamp.credits.dto.TransactionEntity;
import com.bootcamp.credits.model.Credits;
import com.bootcamp.credits.repository.CreditsDao;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CreditsServiceImpl implements CreditsService{
	
	@Autowired
	private CreditsDao creditsDao;

	@Override
	public Flux<Credits> findAll() {
		return creditsDao.findAll();
	}

	@Override
	public Mono<Credits> findById(String id) {
		return creditsDao.findById(id);
	}

	@Override
	public Mono<Credits> save(Credits credits) {
		return creditsDao.save(credits);
	}

	@Override
	public Mono<Void> delete(Credits credits) {
		return creditsDao.delete(credits);
	}

}
