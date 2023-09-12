package com.api.awshyperroll.model;

import java.util.List;

import lombok.Data;

@Data
public class InitializeGameData {
    private int initRoll;
    private List<Player> players;
    private boolean botGame;
}
