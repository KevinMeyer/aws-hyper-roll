package com.api.awshyperroll.model;

import lombok.Data;

@Data
public class GameMessage {
    private String fromPlayerId;
    private String gameId;
    private String message;  
}
