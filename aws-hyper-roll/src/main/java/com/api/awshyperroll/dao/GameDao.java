package com.api.awshyperroll.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.api.awshyperroll.model.Roll;

@Repository
public class GameDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public GameDao(NamedParameterJdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    private static final String INSERT_ROLL = "INSERT INTO db.roll_hist (roll,player_nm) VALUES (:roll, :player_nm); ";

    public void insertRoll (Roll roll) throws DataAccessException{
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("roll", roll.getRoll())
            .addValue("player_nm", roll.getPlayer());

            jdbcTemplate.update(INSERT_ROLL, parameterSource);
    }

}
