package com.api.awshyperroll.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String accountId;
    private String email;
    private String loginToken;
    private String password;
}
