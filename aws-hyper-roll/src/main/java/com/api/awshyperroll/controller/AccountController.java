package com.api.awshyperroll.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.api.awshyperroll.model.EmailDetails;
import com.api.awshyperroll.model.LoginRequest;
import com.api.awshyperroll.model.LoginResponse;
import com.api.awshyperroll.model.RegisterAccountInfo;
import com.api.awshyperroll.service.AccountService;
import com.fasterxml.jackson.core.JsonProcessingException;


@RestController
public class AccountController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @PostMapping("/account/verifyEmail")
    public void sendVerificationEmail(@RequestBody EmailDetails details){
        try {
            LOGGER.info("Begin sendVerificationEmail...");
            accountService.sendVerificationEmail(details);
            LOGGER.info("Verification code sent!");
        } catch (DataAccessException dae) {
            String message = "Database error occurred in sendVerificationEmail...";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }
    @PostMapping("/account")
    public LoginResponse registerAccount(@RequestBody RegisterAccountInfo accountInfo){
        try {
            LOGGER.info("Begin registerAccount...");
            return accountService.registerAccount(accountInfo);
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parsing error occurred in registerAccount...";
            LOGGER.error(message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }  catch (DataAccessException dae) {
            String message = "Database error occurred in registerAccount...";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }
    // TODO: HANDLE NEGATIVE SCENARIOS TOKEN NOT VALID< BAD PASSWORD
    @PostMapping("/account/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        try {
            LOGGER.info("Begin login...");
            return accountService.login(request);
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parsing error occurred in login...";
            LOGGER.error(message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }  catch (DataAccessException dae) {
            String message = "Database error occurred in login...";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }
}
