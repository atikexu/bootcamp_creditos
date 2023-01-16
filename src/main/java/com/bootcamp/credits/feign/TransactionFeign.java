//package com.bootcamp.credits.feign;
//
//import org.springframework.cloud.netflix.feign.FeignClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//
//import com.bootcamp.credits.dto.TransactionEntity;
//import com.bootcamp.credits.dto.TransactionCreateRequest;
//
//import reactor.core.publisher.Mono;
//
//@FeignClient(name="posts", url="http://localhost:8084")
//public interface TransactionFeign {
// 
//    @PostMapping(value = "/", consumes = "application/json")
//    Mono<ResponseEntity<TransactionEntity>> save(TransactionCreateRequest transactionCreateRequest);
//	
//}
//
//
