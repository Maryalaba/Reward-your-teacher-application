package com.decagon.rewardyourteacherapi11bjavapodf2.controllers;

import com.decagon.rewardyourteacherapi11bjavapodf2.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import com.decagon.rewardyourteacherapi11bjavapodf2.model.Transaction;
import com.decagon.rewardyourteacherapi11bjavapodf2.response.ApiResponse;
import com.decagon.rewardyourteacherapi11bjavapodf2.response.PaymentResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private PaymentResponse paymentResponse = new PaymentResponse();


    @PostMapping(value = "/deposit")
    public ResponseEntity<?> deposit(Principal principal, @RequestParam Long amount) throws Exception {

        return new ResponseEntity<>( transactionService.initDeposit(principal, amount), HttpStatus.OK);
    }

    @GetMapping(value = "/callback")
    public ResponseEntity<?> payStackResponse(String reference) throws Exception {
        return new ResponseEntity<>(transactionService.verifyTransaction(reference), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Transaction>>> retrieveAllUserTransactions() {
        return new ResponseEntity<>(transactionService.findAllTransactionByUser(), HttpStatus.OK);
    }


}
