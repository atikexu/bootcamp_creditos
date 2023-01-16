package com.bootcamp.credits.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.bootcamp.credit.util.Constants;
import com.bootcamp.credits.model.Credits;
import com.bootcamp.credits.service.CreditsService;
import com.bootcamp.credits.dto.TransactionEntity;
import com.bootcamp.credits.dto.TransactionCreateRequest;
import com.bootcamp.credits.enums.TransactionType;
//import com.bootcamp.credits.feign.TransactionFeign;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/credits/credit")
@RequiredArgsConstructor
public class CreditsController {
	@Autowired
	private CreditsService creditsService;
	
//	@Autowired
//	private TransactionFeign transactionFeign;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Credits>>> listCredits(){
		logger.info("Run process /listCredits");
		return Mono.just(
					ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(creditsService.findAll())
				);
	}
	
	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> saveCredits(@Valid @RequestBody Mono<Credits> credits){
		logger.info("Run process /saveCredits");
		Map<String, Object> response = new HashMap<>();

		return credits.flatMap(p -> {
			return creditsService.save(p).map(c -> {
				response.put("credit", c);
				response.put("mensaje", Constants.MESSAGE_CREDIT_SAVE);
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
	public Mono<ResponseEntity<Credits>> updateCredits(@RequestBody Credits credits, @PathVariable String id){
		logger.info("Run process /updateCredits");
		return creditsService.findById(id).flatMap(p -> {
			p.setCustomerId(credits.getCustomerId());
			p.setTypeAccount(credits.getTypeAccount());
			p.setAmountPaid(credits.getAmountPaid());
			p.setAmountToPay(credits.getAmountToPay());
			p.setCreditDate(credits.getCreditDate());
			return creditsService.save(p);
		}).map(p -> ResponseEntity.created(URI.create("/credit/update/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(p))
			.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/delete/{id}")
	public Mono<ResponseEntity<Void>> deleteCredits(@PathVariable String id){
		logger.info("Run process /deleteCredits");
		return creditsService.findById(id).flatMap(p -> {
			return creditsService.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
	
	@PutMapping("/updatepay/{id}")
	public Mono<ResponseEntity<Map<String, Object>>> saveCreditsPay(@RequestBody Credits credits , @PathVariable String id){
		logger.info("Run process /saveCreditsPay");
		Map<String, Object> response = new HashMap<>();
		
		return creditsService.findById(id).flatMap(p -> {
			Double mount=credits.getAmountPaid()+p.getAmountPaid();
			response.put("message", Constants.MESSAGE_PAY_NOOK);
			if(mount<=p.getAmountToPay()) {
				p.setAmountPaid(credits.getAmountPaid()+p.getAmountPaid());
				response.put("message", Constants.MESSAGE_PAY_OK);
			}
			return creditsService.save(p).map(c -> {
				response.put("credit", c);
				performDeposit(c.getId(), c.getAmountToPay(),c.getCustomerId());
//				TransactionCreateRequest transactionCreateRequest = TransactionCreateRequest.builder()
//                        .transactionType(TransactionType.PAGO)
//                        .productId(c.getId())
//                        .customerId(c.getCustomerId())
//                        .amount(c.getAmountToPay())
//                        .productType(c.getTypeAccount())
//                        .build();
//				sendPayloadToTransactions(transactionCreateRequest).map(t -> {
//                    return c;
//                });
			
				return ResponseEntity
						.created(URI.create("/credit/updatepay/".concat(c.getId())))
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
	
	 private Mono<TransactionEntity> sendPayloadToTransactions(TransactionCreateRequest transactionCreateRequest){
	        WebClient webClient = WebClient.create("http://localhost:8084");
	        return  webClient.post()
	                .uri("/transaction")
	                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
	                .body(Mono.just(transactionCreateRequest), TransactionCreateRequest.class)
	                .retrieve().bodyToMono(TransactionEntity.class);

	    }
	
	 @Transactional
    public Mono<Credits> performDeposit(String accountId, Double amount, String customerId){
        Mono<Credits> accountMono = creditsService.findById(accountId);
        return accountMono.flatMap(account -> {
            if(!account.getCustomerId().equals(customerId)){
                return Mono.error(new Exception(String.format("Account %s and customerId %s are not associated", accountId, customerId)));
            }

            Credits newAccount;

            newAccount = account;

            return creditsService.save(newAccount).flatMap(updatedAccount -> {
                TransactionCreateRequest transactionCreateRequest = TransactionCreateRequest.builder()
                        .transactionType(TransactionType.DEPOSITO)
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
