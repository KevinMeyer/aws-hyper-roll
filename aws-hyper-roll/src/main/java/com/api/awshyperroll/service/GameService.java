package com.api.awshyperroll.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.api.awshyperroll.dao.GameDao;
import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.Roll;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class GameService {
    @Autowired
    private GameDao gameDao;
    
    public Game createGame(InitializeGameData gameData) throws DataAccessException, JsonProcessingException{

        Game game = new Game();

        game.setIntialBet(gameData.getBet());
        game.setCurrentRoll(gameData.getBet());
        game.setRolls(new ArrayList<>());
        List<String> log = new ArrayList<>();
        game.setGameLog(log);
        game.setPlayers(new LinkedList<>(gameData.getPlayers()));
        // Creates game with unique UUID in database 
        gameDao.createGame(game);
        return game;
    }
    public void insertRoll(Roll roll) throws DataAccessException{
        gameDao.insertRoll(roll);
    }

    public Game getGame(String id) throws DataAccessException, JsonProcessingException{
        return gameDao.getGame(id);
    }

    public void updateGame(Game game)  throws DataAccessException, JsonProcessingException{
        gameDao.updateGame(game);
    }
}
