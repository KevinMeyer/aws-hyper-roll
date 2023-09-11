package com.api.awshyperroll.model;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

import com.Constants.GenericConstants;

import lombok.Data;

@Data
public class Game {
    private String gameId;
    private String lobbyId;
    private String gameStatus;
    private int initialRoll;
    private int currentRoll;
    private Queue<Player> players;
    private List<Roll> rolls;
    private List<String> gameLog;
    @SuppressWarnings("unused")
    // Field is used in method overriding lombok getter
    private String gameLogString;


    public String getGameLogString () {
        return String.join("\n", gameLog);
    }
  
    public Roll roll(){
        if (GenericConstants.INITIALIZING.equals(gameStatus)) {
            gameStatus = GenericConstants.PLAYING;
        }
        // First person in queue is rolling
        Player roller = players.remove();
        int roll =  ThreadLocalRandom.current().nextInt(1, currentRoll + 1);
        currentRoll = roll;
        Roll newRoll = new Roll();
        newRoll.setPlayer(roller.getName());
        rolls.add(newRoll);
        // Roller loses if they roll a 1
        if (roll == 1) {
            gameStatus = GenericConstants.FINISHED;
            gameLog.add(roller.getName() + " rolled a " + roll + ".");
            gameLog.add("Player " +  roller.getName() + " loses!");
        // If a 1 is not rolled, add them to the back of queue and add the log.
        } else {
            gameLog.add(roller.getName() + " rolled a " + roll + ".");
            players.add(roller);
        }
        return newRoll;
    }

    //Can only roll if current roll is greater than 1
    public boolean canRoll(){
        return currentRoll > 1;
    }
}
