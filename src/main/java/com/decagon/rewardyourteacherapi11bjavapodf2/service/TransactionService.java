package com.decagon.rewardyourteacherapi11bjavapodf2.service;

import com.decagon.rewardyourteacherapi11bjavapodf2.response.PaymentResponse;
import com.decagon.rewardyourteacherapi11bjavapodf2.response.VerifyTransactionResponse;
import com.decagon.rewardyourteacherapi11bjavapodf2.model.Transaction;
import com.decagon.rewardyourteacherapi11bjavapodf2.response.ApiResponse;

import java.security.Principal;
import java.util.List;

public interface TransactionService {
    PaymentResponse initDeposit(Principal principal, Long amount) throws Exception;

    VerifyTransactionResponse verifyTransaction(String reference) throws Exception;

    ApiResponse<List<Transaction>> findAllTransactionByUser();


}
