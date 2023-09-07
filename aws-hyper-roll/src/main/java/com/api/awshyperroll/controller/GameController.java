package com.api.awshyperroll.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.Player;
import com.api.awshyperroll.model.Roll;
import com.api.awshyperroll.service.GameService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


@RestController
public class GameController { 

    @Autowired
    private GameService gameService; 

    @PostMapping("/game")
    public Game createNewGame(@RequestBody InitializeGameData gameData) {
        Game game = new Game();
        game.setGameStatus("In Progess...");
        game.setIntialBet(gameData.getBet());
        game.setCurrentRoll(gameData.getBet());
        game.setRolls(new ArrayList<>());
        game.setGameLog(new ArrayList<>());
        Queue<Player> playersQueue = new LinkedList<>(gameData.getPlayers());
        game.setPlayers(playersQueue);

        return game;
    }

    @PostMapping("/game/roll")
    public Game roll(@RequestBody Game game){
        Roll roll = game.roll();
        gameService.insertRoll(roll);
        System.out.println(game.getRollsString());
        return game;
    } 

}
