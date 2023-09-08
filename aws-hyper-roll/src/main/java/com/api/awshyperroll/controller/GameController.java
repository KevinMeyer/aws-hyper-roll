package com.api.awshyperroll.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.Roll;
import com.api.awshyperroll.service.GameService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@RestController
public class GameController { 

    @Autowired
    private GameService gameService; 

    @CrossOrigin(origins = "*")
    @PostMapping("/game")
    public Game createNewGame(@RequestBody InitializeGameData gameData) {
        Game game = new Game();

        game.setGameStatus("PLAYING");
        game.setIntialBet(gameData.getBet());
        game.setCurrentRoll(gameData.getBet());
        game.setRolls(new ArrayList<>());
        List<String> log = new ArrayList<>();
        log.add("The game has begun with a bet of " + gameData.getBet() + " points!");
        game.setGameLog(log);
        game.setPlayers(new LinkedList<>(gameData.getPlayers()));
        System.out.println(gameData.getBet());
        return game;
    }

    @PostMapping("/game/roll")
    public Game roll(@RequestBody Game game){
        Roll roll = game.roll();
        gameService.insertRoll(roll);
        System.out.println(game.getGameLogString());
        return game;
    } 

}
