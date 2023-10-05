package com.api.awshyperroll.model;

import lombok.Data;

@Data
public class RegisterAccountInfo {
    private String displayName;
    private String password;
    private String email;
    private String code;

}
