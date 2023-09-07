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
    private String rollsString;

    public String getRollsString (){
        String rollsString = "";
        for (Roll roll : rolls) {
            rollsString += roll.getPlayer() + " rolled a " + roll.getRoll() + ". \n";
        }
        return rollsString;
    }

    public void setRollsString(String rollsString){
        this.rollsString = rollsString;
    }
   
    public void roll(){
        // First person in queue is rolling
        Player roller = players.remove();
        int roll =  (int) ((Math.random() * (currentRoll - 1)) + 1);
        currentRoll = roll;
        rolls.add(new Roll(roller.name, roll));
        if (roll == 1) {
            gameStatus = "Player " +  roller.name + " loses!";
        }
        // Add them to the end of the players queue
        players.add(roller);
    }
    
}
