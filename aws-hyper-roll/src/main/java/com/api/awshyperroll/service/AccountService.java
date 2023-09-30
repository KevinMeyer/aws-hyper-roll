package com.api.awshyperroll.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.awshyperroll.dao.AccountDao;
import com.api.awshyperroll.model.EmailDetails;
import com.api.awshyperroll.utility.DeathRollUtil;

@Service
public class AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

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
}
