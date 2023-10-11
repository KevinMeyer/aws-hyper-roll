package com.api.awshyperroll.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.awshyperroll.Constants.GenericConstants;
import com.api.awshyperroll.dao.BaseDao;
import com.api.awshyperroll.dao.LobbyDao;
import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.JoinResponse;
import com.api.awshyperroll.model.Lobby;
import com.api.awshyperroll.model.LobbyIds;
import com.api.awshyperroll.model.Player;
import com.fasterxml.jackson.core.JsonProcessingException;


@Service
public class LobbyService {
    @Autowired
    private LobbyDao lobbyDao; 

    @Autowired 
    private GameService gameService;

    @Autowired
    private BaseDao dao;

    public JoinResponse createLobby(InitializeGameData data) throws JsonProcessingException {
        //Fetch available lobby code and create host UUID
        String code =  lobbyDao.generateCode();
        String hostPlayerId = dao.getUUID();
        String lobbyId = dao.getUUID();

        
        Player initPlayer = data.getPlayers().get(0);

        initPlayer.setPlayerId(hostPlayerId);
        initPlayer.setRole(GenericConstants.HOST);
        if(initPlayer.isGuest()){
            initPlayer.setAccountId(hostPlayerId);
        }

        // Create new game for lobby
        String initMessage = "Created Lobby: " + code + ". Starting Roll:" + data.getInitRoll();
        Game game = gameService.createGame(data, lobbyId, initMessage);
        
        Map<String,Player> initPlayers = new HashMap<>();
        initPlayers.put(initPlayer.getPlayerId(), initPlayer);
        game.setInitPlayers(initPlayers);
        game.setPlayers(new LinkedList<>(initPlayers.values()));

        // Create lobby for game 
        Lobby lobby =  lobbyDao.createLobby(game, lobbyId, code);
        lobby.setPlayer(initPlayer);
        // Create host player/update game in db
        lobbyDao.createPlayer(initPlayer,lobbyId);
        gameService.updateGame(game);

        LobbyIds ids = new LobbyIds();
        ids.setGameId(lobby.getGameId());
        ids.setLobbyId(lobby.getLobbyId());
        ids.setPlayerId(initPlayer.getPlayerId());
        ids.setAccountId(initPlayer.getAccountId());

        JoinResponse response = new JoinResponse();
        response.setSuccess(true);
        response.setLobbyIds(ids);

        return response;
    }

    public JoinResponse joinLobby (String code, Player player ) throws JsonProcessingException {
        JoinResponse response = new JoinResponse();
        // Create new player
        String playerId = dao.getUUID();
        player.setPlayerId(playerId);
        player.setRole(GenericConstants.PLAYER);
        if(player.isGuest()){
            player.setAccountId(playerId);
        }

        // Update game with new player and log 
        Lobby lobby = lobbyDao.getLobby(code);
        Game game = gameService.getGame(lobby.getGameId());
        // TODO ADD CHECK FOR DETERMINING IF LOBBY CODE IS ACTIVE
        if (!game.getGameStatus().equals(GenericConstants.INITIALIZING)) {
            response.setSuccess(false);
            response.setErrMsg("Game is not in setup and cannot be joined.");
            return response;
        }
        game.getGameLog().add("Player " + player.getName() + " has joined the game!");
        game.getPlayers().add(player);
        game.getInitPlayers().put(player.getAccountId(), player);
        
        // Create player and update game in db
        lobbyDao.createPlayer(player, lobby.getLobbyId());
        gameService.updateGame(game);

        // Return relevant ids for lobby to function
        LobbyIds lobbyIds = new LobbyIds();
        lobbyIds.setGameId(lobby.getGameId());
        lobbyIds.setPlayerId(playerId);
        lobbyIds.setAccountId(player.getAccountId());
        lobbyIds.setLobbyId(lobby.getLobbyId());
       
        response.setLobbyIds(lobbyIds);
        response.setSuccess(true);
        return response;

    }

    public boolean pollPlayerRefresh(String playerId) {
        return lobbyDao.pollPlayerRefresh(playerId);
    }

    public void setLatestGameFlag(String playerId, boolean flag) {
        lobbyDao.setLatestGameFlag(playerId, flag);
    }

    public void changeLobbyActvFlag(String gameId, boolean flag) {
        lobbyDao.changeLobbyActvFlag(gameId, flag);
    }

    public void updateLobbyGame(String lobbyId, String gameId) {
        lobbyDao.updateLobbyGame(lobbyId, gameId);
    }
}
