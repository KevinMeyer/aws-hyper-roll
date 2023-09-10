package com.api.awshyperroll.model;

import lombok.Data;

@Data
public class Lobby {
    private String lobbyId;
    private String gameId;
    private String code;
    private Player player;
}
