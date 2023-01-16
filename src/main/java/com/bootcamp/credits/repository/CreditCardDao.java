package com.bootcamp.credits.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.bootcamp.credits.model.CreditCard;

public interface CreditCardDao extends ReactiveMongoRepository<CreditCard, String>{

}