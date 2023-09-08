package com.api.awshyperroll.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.Roll;
import com.api.awshyperroll.service.GameService;
import com.fasterxml.jackson.core.JsonProcessingException;


@RestController
public class GameController { 

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameService gameService; 

    @PostMapping("/game")
    public Game createNewGame(@RequestBody InitializeGameData gameData) {
        try {
            LOGGER.info("Begin creating game...");
             return gameService.createGame(gameData);
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Game";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occured while creating new Game";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }

    @PostMapping("/game/roll")
    public Game roll(@RequestBody Game game){
        try {
            LOGGER.info("Begin rolling...");
            Game serverGame = gameService.getGame(game.getId()); 
            Roll roll = serverGame.roll();
            gameService.insertRoll(roll);
            gameService.updateGame(serverGame);
            return serverGame;
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Game";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occured while rolling";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    } 



}
