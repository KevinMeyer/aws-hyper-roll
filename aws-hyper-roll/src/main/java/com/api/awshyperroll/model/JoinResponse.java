package com.api.awshyperroll.model;

import lombok.Data;

@Data
public class JoinResponse {
    private boolean success;
    private String errMsg;
    private LobbyIds lobbyIds;
}
