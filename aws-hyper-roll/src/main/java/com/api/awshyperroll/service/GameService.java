package com.api.awshyperroll.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.api.awshyperroll.dao.GameDao;
import com.api.awshyperroll.model.Roll;

@Service
public class GameService {
    @Autowired
    private GameDao gameDao;
    public void insertRoll(Roll roll) throws DataAccessException{
        gameDao.insertRoll(roll);
    }
}
