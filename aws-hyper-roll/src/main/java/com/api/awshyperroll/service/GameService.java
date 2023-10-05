package com.api.awshyperroll.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.awshyperroll.Constants.GenericConstants;
import com.api.awshyperroll.dao.GameDao;
import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.GameMessage;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.Player;
import com.api.awshyperroll.model.Roll;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class GameService {
    @Autowired
    private GameDao gameDao;
    
    public Game createGame(InitializeGameData gameData, String lobbyId, String initMessage) throws JsonProcessingException{

        Game game = new Game();

        game.setInitialRoll(gameData.getInitRoll());
        game.setCurrentRoll(gameData.getInitRoll());
        game.setRolls(new ArrayList<>());
       
        List<String> log = new ArrayList<>();
        log.add(initMessage);
        
        game.setGameLog(log);
        game.setGameStatus(GenericConstants.INITIALIZING);
        game.setLobbyId(lobbyId);
        
        game.setPlayers(new LinkedList<>(gameData.getPlayers()));
        if(gameData.isBotGame()){
            Player awsBot = new Player();
            awsBot.setName("AWS Bot");
            awsBot.setRole("Player");
            awsBot.setPlayerId("BOT");
        }
        // Creates game with unique UUID in database 
        gameDao.createGame(game);
        return game;
    }
    
    public void insertRoll(Roll roll) {
        gameDao.insertRoll(roll);
    }
    
    public Game getGame(String gameId) throws JsonProcessingException{
        return gameDao.getGame(gameId);
    }

    public Game getGameByLobbyId(String lobbyId) throws JsonProcessingException{
        return gameDao.getGameByLobbyId(lobbyId);
    }

    public void updateGame(Game game)  throws JsonProcessingException{
        gameDao.updateGame(game);
        pushUpdateToPlayer(game.getLobbyId());
    }
    
    public void pushUpdateToPlayer(String lobbyId) {
        gameDao.pushUpdateToPlayer(lobbyId);
    }

    public void postGameMessage ( GameMessage gameMessage ) throws JsonProcessingException{
        Game game = getGame(gameMessage.getGameId());
        game.addGameLog(gameMessage.getMessage());
        updateGame(game);
        

    }
}
