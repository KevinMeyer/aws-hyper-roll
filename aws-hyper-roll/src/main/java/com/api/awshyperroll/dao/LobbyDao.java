package com.api.awshyperroll.dao;

import java.util.Random;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.Lobby;
import com.api.awshyperroll.model.Player;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class LobbyDao implements DaoConstants {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();


    public LobbyDao(NamedParameterJdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    
    private static final String CREATE_LOBBY = "INSERT INTO hyperrolldb.lobby (lobby_id,code,lobby_json,game_id,upd_ts,actv_flag) " + 
                                                "VALUES (:lobby_id,:code,:lobby_json,:game_id,CURRENT_TIMESTAMP(),true);";
    private static final String GET_LOBBY = "SELECT lobby_json FROM hyperrolldb.lobby WHERE code = :code AND actv_flag = true;";
    private static final String CODE_COUNT = "SELECT COUNT(code) FROM hyperrolldb.lobby WHERE code = :code AND actv_flag != 0;";
    private static final String CREATE_PLAYER = "INSERT INTO hyperrolldb.player (player_id,lobby_id,player_json) VALUES (:player_id,:lobby_id,:player_json);";
    private static final String POLL_PLAYER_REFRESH = "SELECT has_latest_game FROM hyperrolldb.player WHERE player_id = :player_id";
    private static final String UPDATE_HAS_LATEST_GAME = "UPDATE hyperrolldb.player SET has_latest_game = :has_latest_game WHERE player_id = :player_id;";
    private static final String CHANGE_LOBBY_ACTV_FLAG = "UPDATE hyperrolldb.lobby SET actv_flag = :actv_flag WHERE game_id = :game_id;";
    private static final String UPDATE_LOBBY_GAME_ID = "UPDATE hyperrolldb.lobby SET game_id = :game_id WHERE lobby_id = :lobby_id;";




    public Lobby createLobby(Game game, String lobbyId, String code) throws JsonProcessingException, DataAccessException {
        Lobby lobby = new Lobby();
        lobby.setLobbyId(lobbyId);
        lobby.setCode(code);
        lobby.setGameId(game.getGameId());
        String lobbyJSON = mapper.writeValueAsString(lobby);

        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(LOBBY_ID, lobbyId)
            .addValue(CODE, code)
            .addValue(LOBBY_JSON, lobbyJSON)
            .addValue(GAME_ID, lobby.getGameId());
        
        jdbcTemplate.update(CREATE_LOBBY, source);
        return lobby;



    }

    public Lobby getLobby(String code) throws JsonProcessingException, DataAccessException {
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.CODE, code);

        String lobbyJSON = jdbcTemplate.queryForObject(GET_LOBBY, source, String.class);
        return mapper.readValue(lobbyJSON,Lobby.class);
    }

    public void createPlayer (Player player, String lobbyId) throws JsonProcessingException, DataAccessException {
        String playerJSON = mapper.writeValueAsString(player);
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(PLAYER_ID, player.getPlayerId())
            .addValue(LOBBY_ID, lobbyId)
            .addValue(PLAYER_JSON, playerJSON);
        jdbcTemplate.update(CREATE_PLAYER, source);    
    }

    public boolean pollPlayerRefresh(String playerId){
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(PLAYER_ID, playerId);
        return jdbcTemplate.queryForObject(POLL_PLAYER_REFRESH, source, boolean.class);
    }

    public void setLatestGameFlag(String playerId, boolean flag){
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(PLAYER_ID, playerId)
            .addValue(HAS_LATEST_GAME, flag);
        jdbcTemplate.update(UPDATE_HAS_LATEST_GAME, source);
    }

    public void changeLobbyActvFlag(String gameId, boolean flag){
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(GAME_ID, gameId)
            .addValue(ACTV_FLAG, flag);

        jdbcTemplate.update(CHANGE_LOBBY_ACTV_FLAG, source);

    }

    public void updateLobbyGame(String lobbyId, String gameId){
        SqlParameterSource source = new MapSqlParameterSource()
                .addValue(LOBBY_ID, lobbyId)
                .addValue(GAME_ID, gameId);
        
        jdbcTemplate.update(UPDATE_LOBBY_GAME_ID, source);

    }

    public String generateCode() {
        String code = createRandomCode();
        boolean validCode = false;
        SqlParameterSource source;
        int count;
        while (!validCode) {
            source = new MapSqlParameterSource()
                .addValue(DaoConstants.CODE, code);
           count = jdbcTemplate.queryForObject(CODE_COUNT, source, int.class);
           if (count == 0){
                validCode = true;
           } else {
                code = createRandomCode();
           }
        }
        return code.toUpperCase();
    }

    private String createRandomCode() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 4;
        Random random = new Random();
    
        String code = random.ints(leftLimit, rightLimit + 1)
          .limit(targetStringLength)
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();

        return code;
    }
}
