package com.api.awshyperroll.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.Constants.GenericConstants;
import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.Roll;
import com.api.awshyperroll.service.GameService;
import com.api.awshyperroll.service.LobbyService;
import com.fasterxml.jackson.core.JsonProcessingException;


@RestController
public class GameController { 

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameService gameService; 

    @Autowired 
    private LobbyService lobbyService;

    @PatchMapping("/game/roll/{gameId}")
    public Game roll(@PathVariable String gameId){
        try {
            LOGGER.info("Begin rolling...");
            Game serverGame = gameService.getGame(gameId); 
            // Safeguard to only roll if the game is playable
            if( serverGame.canRoll()) {
                Roll roll = serverGame.roll();
                gameService.insertRoll(roll);
                if (GenericConstants.FINISHED.equals( serverGame.getGameStatus())){
                    lobbyService.changeLobbyActvFlag(gameId, false);
                }
                gameService.updateGame(serverGame);
            }
            LOGGER.info("Finished rolling...");
            return serverGame;
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Game";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occurred while rolling";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    } 

    @GetMapping("/game/{gameId}")
    public Game getGame(@PathVariable String gameId) {
        try {
            LOGGER.info("Begin getting Game...");
            return gameService.getGame(gameId);
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Game";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occurred while creating new Game";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }
}
