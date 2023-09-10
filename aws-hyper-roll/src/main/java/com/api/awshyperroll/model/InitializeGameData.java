package com.api.awshyperroll.model;

import lombok.Data;

@Data
public class InitializeGameData {
    private int initRoll;
    private Player player;
    private boolean botGame;
}
