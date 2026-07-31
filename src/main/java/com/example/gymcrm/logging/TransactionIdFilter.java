package com.example.gymcrm.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class TransactionIdFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_ID = "transactionId";
    public static final String HEADER_NAME = "X-transaction-id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String transactionId = request.getHeader(HEADER_NAME);

        if(transactionId == null || transactionId.isBlank()){
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put(TRANSACTION_ID, transactionId);
        request.setAttribute(TRANSACTION_ID, transactionId);
        response.setHeader(HEADER_NAME, transactionId);

        try{
            filterChain.doFilter(request, response);
        }finally {
            MDC.remove(TRANSACTION_ID);
        }
    }
}
