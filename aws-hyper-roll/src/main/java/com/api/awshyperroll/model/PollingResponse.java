package com.api.awshyperroll.model;

import lombok.Data;

@Data
public class PollingResponse {
    private LobbyIds lobbyIds;
    private Game game; 
}
