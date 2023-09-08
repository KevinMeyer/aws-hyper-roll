package com.api.awshyperroll.model;
import java.util.List;
import java.util.Queue;

import lombok.Data;

@Data
public class Game {
    private String gameStatus;
    private int intialBet;
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
    public String getRollsString (){
        String rollsString = "";
        for (Roll roll : rolls) {
            rollsString += roll.getPlayer() + " rolled a " + roll.getRoll() + ". \n";
        }
        return rollsString;
    }

    
   
    public Roll roll(){
        // First person in queue is rolling
        Player roller = players.remove();
        int roll =  (int) ((Math.random() * (currentRoll - 1)) + 1);
        currentRoll = roll;
        Roll newRoll = new Roll(roller.name, roll);
        rolls.add(newRoll);
        // Roller loses if they roll a 1
        if (roll == 1) {
            gameStatus = "FINISHED";
            gameLog.add(roller.getName() + " rolled a " + roll + ".");
            gameLog.add("Player " +  roller.name + " loses!");
        // If a 1 is not rolled, add them to the back of queue and add the log.
        } else {
            gameLog.add(roller.getName() + " rolled a " + roll + ".");
            players.add(roller);
        }
        // Add them to the end of the players queue
      
        return newRoll;
    }
    
}
