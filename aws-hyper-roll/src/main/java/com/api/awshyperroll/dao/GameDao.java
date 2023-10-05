package com.api.awshyperroll.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.GameMessage;
import com.api.awshyperroll.model.Roll;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class GameDao implements DaoConstants{
    private final NamedParameterJdbcTemplate jdbcTemplate;
    
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private BaseDao baseDao;

    public GameDao(NamedParameterJdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }


    private static final String INSERT_ROLL = "INSERT INTO hyperrolldb.roll_hist (roll, player_nm) VALUES (:roll, :player_nm);";
    private static final String INSERT_GAME = "INSERT INTO hyperrolldb.game (game_id, game_json) VALUES (:game_id, :game_json);";
    private static final String GET_GAME = "SELECT g.game_json FROM hyperrolldb.game g WHERE g.game_id = :game_id;";
    private static final String GET_GAME_BY_LOBBY_ID = "SELECT g.game_json FROM hyperrolldb.game g INNER JOIN lobby l ON g.game_id = l.game_id WHERE l.lobby_id = :lobby_id;";
    private static final String UPDATE_GAME = "UPDATE hyperrolldb.game SET game_json = :game_json WHERE game_id = :game_id;";
    private static final String PUSH_UPDATE_TO_PLAYERS = "UPDATE hyperrolldb.player SET has_latest_game = false WHERE lobby_id = :lobby_id;";
                                                          

    public void createGame(Game game) throws JsonProcessingException {
        String id = baseDao.getUUID();
        game.setGameId(id);
        String gameJSON = mapper.writeValueAsString(game);
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(GAME_ID, id)
            .addValue(GAME_JSON, gameJSON);
        jdbcTemplate.update(INSERT_GAME, source);
    }

    public void updateGame(Game game) throws JsonProcessingException {
        String gameJSON = mapper.writeValueAsString(game);
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(GAME_ID, game.getGameId())
            .addValue(GAME_JSON, gameJSON);
        jdbcTemplate.update(UPDATE_GAME, source);
    }

    public void pushUpdateToPlayer(String lobbyId){
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(LOBBY_ID, lobbyId);
        jdbcTemplate.update(PUSH_UPDATE_TO_PLAYERS, source);
    }
    
    public Game getGame(String gameId) throws JsonProcessingException {
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(GAME_ID, gameId); 
        String gameJSON = jdbcTemplate.queryForObject(GET_GAME, source, String.class);
        return mapper.readValue(gameJSON, Game.class);
    }

    public Game getGameByLobbyId(String lobbyId) throws JsonProcessingException {
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(LOBBY_ID, lobbyId); 
        String gameJSON = jdbcTemplate.queryForObject(GET_GAME_BY_LOBBY_ID, source, String.class);
        return mapper.readValue(gameJSON, Game.class);
    }

    public void insertRoll (Roll roll) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue(ROLL, roll.getRoll())
            .addValue(DaoConstants.PLAYER_NM, roll.getPlayer());
        jdbcTemplate.update(INSERT_ROLL, parameterSource);
    }

    public void postGameMessage ( GameMessage gameMessage) {
        
    }
}
