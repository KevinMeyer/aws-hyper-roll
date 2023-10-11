package com.api.awshyperroll.model;

import lombok.Data;

@Data
public class Player {
    
    private String playerId;
    private String accountId;
    private String role;
    private String name;
    private boolean eliminated; 
    private boolean guest;

}
