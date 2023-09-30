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
import com.api.awshyperroll.service.AccountService;

@RestController
public class AccountController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @PostMapping("/account/verifyEmail")
    public void sendVerificationEmail(@RequestBody EmailDetails details){
        try {
            LOGGER.info("Begin sending verification code");
            accountService.sendVerificationEmail(details);
        } catch (DataAccessException dae) {
            String message = "Database error occurred in roll";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }

    }
}
