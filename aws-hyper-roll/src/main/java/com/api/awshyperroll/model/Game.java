package com.api.awshyperroll.model;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

import com.api.awshyperroll.Constants.GenericConstants;

import lombok.Data;

@Data
public class Game {


    private String gameId;
    private String lobbyId;
    private String gameStatus;
    private int initialRoll;
    private int currentRoll;
    private Queue<Player> players;
    private Map<String,Player> initPlayers;
    private List<Roll> rolls;
    private List<String> gameLog;
    private boolean eliminationGame;
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
        newRoll.setPlayer(roller.getPlayerId());
        newRoll.setRoll(roll);
        rolls.add(newRoll);

        // Roller loses if they roll a 1
        if (roll == 1) {
            // If not elimination Game finish once first player loses 
            if (!eliminationGame) {
                gameStatus = GenericConstants.FINISHED;
                addGameLog( roller.getName() + " rolled a " + roll + "." );
                addGameLog( "Player " +  roller.getName() + " loses!" );
            // If it is an elimination game, finish once all players but one are eliminated, but do not add eliminated players back into the queue
            } else {
                addGameLog( "Player " +  roller.getName() + " rolled a 1 and is eliminated!" ); 
                if(players.size() == 1){
                    addGameLog( "Player " +  players.peek().getName() + " wins!" );
                    gameStatus = GenericConstants.FINISHED;
                    return newRoll;
                } else {
                    // Log the losing player, reset the roll to initialRoll and return before adding the eliminated player back to the queue
                    addGameLog( "Roll has been reset to " + initialRoll + "!" ); 
                    currentRoll = initialRoll;
                    return newRoll;
                }

            }
        // If a 1 is not rolled, add the log.
        } else {
            addGameLog( roller.getName() + " rolled a " + roll + ".");
        }
        // Always add back to queue so game keeps track of all players for lobby reset 
        players.add(roller);
        return newRoll;
    }

    public void addGameLog(String log){
        gameLog.add(log);
    }

    //Can only roll if current roll is greater than 1
    public boolean canRoll(){
        return currentRoll > 1;
    }
}
