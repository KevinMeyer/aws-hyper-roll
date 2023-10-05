package com.api.awshyperroll.model;

import lombok.Data;

@Data
public class LoginResponse {
    private String accountId;
    private String loginToken;
    private Account account;
    private boolean success;
    private String errMsg;

}
