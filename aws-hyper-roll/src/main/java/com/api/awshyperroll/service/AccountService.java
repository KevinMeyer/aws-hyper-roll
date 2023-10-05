package com.api.awshyperroll.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.awshyperroll.dao.AccountDao;
import com.api.awshyperroll.model.EmailDetails;
import com.api.awshyperroll.model.LoginRequest;
import com.api.awshyperroll.model.LoginResponse;
import com.api.awshyperroll.model.RegisterAccountInfo;
import com.api.awshyperroll.utility.DeathRollUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class AccountService {

    @Autowired 
    private EmailService emailService;

    @Autowired
    private AccountDao accountDao;

    public void sendVerificationEmail(EmailDetails details){
        // Generate Verification Code 
        String verificationCode = DeathRollUtil.createRandomCode(6);
        // Create email_auth record 
        accountDao.createAuthRecord(details.getRecipient(), verificationCode);
        // Send verification email
        details.setSubject("Death Roll Verification Code");
        details.setMsgBody(verificationCode);
        emailService.sendSimpleMail(details);
    }

    public LoginResponse registerAccount(RegisterAccountInfo accountInfo) throws JsonProcessingException{
        return accountDao.createAccount(accountInfo);
    }
    public LoginResponse login(LoginRequest request) throws JsonProcessingException{
        return accountDao.login(request);
    }
}
