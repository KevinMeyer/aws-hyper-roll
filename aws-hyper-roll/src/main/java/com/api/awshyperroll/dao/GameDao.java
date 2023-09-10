package com.api.awshyperroll.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.Roll;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class GameDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();
    public GameDao(NamedParameterJdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    private static final String GET_UUID = "SELECT UUID()";
    private static final String INSERT_ROLL = "INSERT INTO hyperrolldb.roll_hist (roll, player_nm) VALUES (:roll, :player_nm)";
    private static final String INSERT_GAME = "INSERT INTO hyperrolldb.game (game_id, game_json) VALUES (:game_id, :game_json)";
    private static final String GET_GAME = "SELECT game_json FROM hyperrolldb.game g WHERE g.game_id = :game_id ";
    private static final String UPDATE_GAME = "UPDATE hyperrolldb.game SET game_json = :game_json WHERE game_id = :game_id";

  


    public void createGame(Game game) throws JsonProcessingException, DataAccessException {
        String id = getUUID();
        game.setGameId(id);
        String gameJSON = mapper.writeValueAsString(game);
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.GAME_ID, id)
            .addValue(DaoConstants.GAME_JSON, gameJSON);
        jdbcTemplate.update(INSERT_GAME, source);
    }

    public void updateGame(Game game) throws JsonProcessingException, DataAccessException {
        String gameJSON = mapper.writeValueAsString(game);
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.GAME_ID, game.getGameId())
            .addValue(DaoConstants.GAME_JSON, gameJSON);
        jdbcTemplate.update(UPDATE_GAME, source);

    }
    public Game getGame(String id) throws JsonProcessingException, DataAccessException {
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.GAME_ID, id); 
        String gameJSON = jdbcTemplate.queryForObject(GET_GAME, source, String.class);
        return mapper.readValue(gameJSON, Game.class);
    }

    public String getUUID() throws DataAccessException {
        return jdbcTemplate.queryForObject(GET_UUID, new MapSqlParameterSource(),String.class);
    }
    public void insertRoll (Roll roll) throws DataAccessException{
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue(DaoConstants.ROLL, roll.getRoll())
            .addValue(DaoConstants.PLAYER_NM, roll.getPlayer());

            jdbcTemplate.update(INSERT_ROLL, parameterSource);
    }
}
