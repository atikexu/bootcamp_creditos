package com.bootcamp.credits.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClient;

import com.bootcamp.credits.dto.TransactionCreateRequest;
import com.bootcamp.credits.dto.TransactionEntity;
import com.bootcamp.credits.enums.TransactionType;
import com.bootcamp.credits.model.CreditCard;
import com.bootcamp.credits.service.CreditCardService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/credits/creditcard")
public class CreditCardController {
	@Autowired
	private CreditCardService creditCardService;
	
	private final Logger logger = LoggerFactory.getLogger(CreditCardController.class);
	
	@GetMapping
	public Mono<ResponseEntity<Flux<CreditCard>>> listCredits(){
		logger.info(String.format("Run process /listCredits",""));
		return Mono.just(
					ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(creditCardService.findAll())
				);
	}
	
	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> saveCredits(@Valid @RequestBody Mono<CreditCard> credits){
		logger.info("Run process /saveCredits");
		Map<String, Object> response = new HashMap<>();

		return credits.flatMap(p -> {
			
			return creditCardService.save(p).map(c -> {
				response.put("creditcard", c);
				response.put("mensaje", "Credit Card saved successfully");
				return ResponseEntity
						.created(URI.create("/credits/credit/register/".concat(c.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(response);
			});
		}).onErrorResume(t -> {
			return Mono.just(t).cast(WebExchangeBindException.class)
					.flatMap(e -> Mono.just(e.getFieldErrors()))
					.flatMapMany(Flux::fromIterable)
					.map(fieldError -> "the field "+fieldError.getField() + " "+ fieldError.getDefaultMessage())
					.collectList()
					.flatMap(list -> {
						response.put("errors", list);
						response.put("status", HttpStatus.BAD_REQUEST.value());
						return Mono.just(ResponseEntity.badRequest().body(response));
					});
		});
	}
	
	@PutMapping("/update/{id}")
	public Mono<ResponseEntity<CreditCard>> updateCreditCard(@RequestBody CreditCard credits, @PathVariable String id){
		logger.info("Run process /updateCreditCard");
		return creditCardService.findById(id).flatMap(p -> {
			p.setCustomerId(credits.getCustomerId());
			p.setTypeAccount(credits.getTypeAccount());
			p.setAmountPaid(credits.getAmountPaid());
			p.setAmountToPay(credits.getAmountToPay());
			p.setCreditDate(credits.getCreditDate());
			
			return creditCardService.save(p);
		}).map(p -> ResponseEntity.created(URI.create("/credit/update/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(p))
			.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/delete/{id}")
	public Mono<ResponseEntity<Void>> deleteCreditCard(@PathVariable String id){
		logger.info("Run process /deleteCreditCard");
		return creditCardService.findById(id).flatMap(p -> {
			return creditCardService.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
	
	@PutMapping("/updatecardpay/{id}")
	public Mono<ResponseEntity<Map<String, Object>>> saveCreditsCardPay(@RequestBody CreditCard credits , @PathVariable String id){
		logger.info("Run process /saveCreditsCardPay");
		Map<String, Object> response = new HashMap<>();

		return creditCardService.findById(id).flatMap(p -> {
			Double mount=credits.getAmountPaid()+p.getAmountPaid();
			response.put("message", "Maximum amount exceeds the loan");
			if(mount<=p.getAmountToPay()) {
				p.setAmountPaid(credits.getAmountPaid()+p.getAmountPaid());
				response.put("message", "Payment made successfully");
			}
			return creditCardService.save(p).flatMap(c -> {
				response.put("credit", c);
				return performDeposit(c.getId(), c.getAmountToPay(),c.getCustomerId()).map(agg -> ResponseEntity
			               .created(URI.create("/creditcard/updatecardpay/".concat(c.getId())))
			               .contentType(MediaType.APPLICATION_JSON)
			               .body(response));
			});
		}).onErrorResume(t -> {
			return Mono.just(t).cast(WebExchangeBindException.class)
					.flatMap(e -> Mono.just(e.getFieldErrors()))
					.flatMapMany(Flux::fromIterable)
					.map(fieldError -> "the field "+fieldError.getField() + " "+ fieldError.getDefaultMessage())
					.collectList()
					.flatMap(list -> {
						response.put("errors", list);
						response.put("status", HttpStatus.BAD_REQUEST.value());
						return Mono.just(ResponseEntity.badRequest().body(response));
					});
		});
	}
	
	@PutMapping("/updatecardcons/{id}")
	public Mono<ResponseEntity<Map<String, Object>>> saveCreditsCardPayCons(@RequestBody CreditCard credits , @PathVariable String id){
		logger.info("Run process /saveCreditsCardPayCons");
		Map<String, Object> response = new HashMap<>();

		return creditCardService.findById(id).flatMap(p -> {
			Double mount=p.getAmountPaid()-credits.getAmountPaid();
			response.put("message", "You don't have enough balance");
			if(mount>=0) {
				p.setAmountPaid(mount);
				response.put("message", "successful consumption");
			}
			return creditCardService.save(p).flatMap(c -> {
				response.put("credit", c);
				return performDeposit(c.getId(), c.getAmountToPay(),c.getCustomerId()).map(agg -> ResponseEntity
			               .created(URI.create("/creditcard/updatecardcons/".concat(c.getId())))
			               .contentType(MediaType.APPLICATION_JSON)
			               .body(response));
			});
		}).onErrorResume(t -> {
			return Mono.just(t).cast(WebExchangeBindException.class)
					.flatMap(e -> Mono.just(e.getFieldErrors()))
					.flatMapMany(Flux::fromIterable)
					.map(fieldError -> "the field "+fieldError.getField() + " "+ fieldError.getDefaultMessage())
					.collectList()
					.flatMap(list -> {
						response.put("errors", list);
						response.put("status", HttpStatus.BAD_REQUEST.value());
						return Mono.just(ResponseEntity.badRequest().body(response));
					});
		});
	}
	
	private Mono<TransactionEntity> sendPayloadToTransactions(TransactionCreateRequest transactionCreateRequest){
        WebClient webClient = WebClient.create("http://localhost:8084");
        return  webClient.post()
                .uri("/transaction")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(transactionCreateRequest), TransactionCreateRequest.class)
                .retrieve().bodyToMono(TransactionEntity.class);

    }

	 @Transactional
	public Mono<CreditCard> performDeposit(String accountId, Double amount, String customerId){
	    Mono<CreditCard> accountMono = creditCardService.findById(accountId);
	    return accountMono.flatMap(account -> {
	        if(!account.getCustomerId().equals(customerId)){
	            return Mono.error(new Exception(String.format("Account %s and customerId %s are not associated", accountId, customerId)));
	        }
	
	        CreditCard newAccount;
	
	        newAccount = account;
	
	        return creditCardService.save(newAccount).flatMap(updatedAccount -> {
	            TransactionCreateRequest transactionCreateRequest = TransactionCreateRequest.builder()
	                    .transactionType(TransactionType.CONSUMO)
	                    .productId(updatedAccount.getId())
	                    .customerId(updatedAccount.getCustomerId())
	                    .amount(amount)
	                    .productType(updatedAccount.getTypeAccount())
	                    .build();
	
	            return sendPayloadToTransactions(transactionCreateRequest).map(t -> {
	                return updatedAccount;
	            });
	
	        });
	    });
 }
	
}
