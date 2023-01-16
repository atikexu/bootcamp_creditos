package com.bootcamp.credits.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootcamp.credits.model.CreditCard;
import com.bootcamp.credits.repository.CreditCardDao;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CreditCardServiceImpl implements CreditCardService{
	
	@Autowired
	private CreditCardDao creditCardDao;

	@Override
	public Flux<CreditCard> findAll() {
		return creditCardDao.findAll();
	}

	@Override
	public Mono<CreditCard> findById(String id) {
		return creditCardDao.findById(id);
	}

	@Override
	public Mono<CreditCard> save(CreditCard credits) {
		return creditCardDao.save(credits);
	}

	@Override
	public Mono<Void> delete(CreditCard credits) {
		return creditCardDao.delete(credits);
	}

}
