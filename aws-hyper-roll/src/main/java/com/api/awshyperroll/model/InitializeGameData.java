package com.api.awshyperroll.model;

import java.util.List;

import lombok.Data;

@Data
public class InitializeGameData {
    int bet;
    List<Player> players;
}
