package com.api.awshyperroll.model;

import lombok.Data;

@Data
public class Account {
    private String accountId;
    private String email;
    private String displayName;
    private long credits;
    
}
