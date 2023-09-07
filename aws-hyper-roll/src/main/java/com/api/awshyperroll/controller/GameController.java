package com.api.awshyperroll.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


@RestController
public class GameController { 

    @PostMapping("/game")
    public Game createNewGame(@RequestBody InitializeGameData gameData) {
        Game game = new Game();
        game.setGameStatus("In Progess...");
        game.setIntialBet(gameData.getBet());
        game.setCurrentRoll(gameData.getBet());
        game.setRolls(new ArrayList<>());
        Queue<Player> playersQueue = new LinkedList<>(gameData.getPlayers());
        game.setPlayers(playersQueue);
        return game;
    }

    @PostMapping("/game/roll")
    public Game roll(@RequestBody Game game){
        game.roll();
        System.out.println(game.getRollsString());
        return game;
    } 

}
