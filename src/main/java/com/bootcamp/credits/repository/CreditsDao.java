package com.bootcamp.credits.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.bootcamp.credits.model.Credits;

public interface CreditsDao extends ReactiveMongoRepository<Credits, String>{

}
