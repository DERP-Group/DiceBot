package com.derpgroup.dicebot.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public abstract class DiceUtil {

  private static Random random = new Random();
  
  public static synchronized Map<Integer, List<Integer>> rollDice(Map<Integer, Integer> inputs){
    if(inputs == null || inputs.size() < 1){
      return null;
    }
    
    Map<Integer, List<Integer>> outputs = new LinkedHashMap<Integer, List<Integer>>();
    for(Entry<Integer,Integer> entry : inputs.entrySet()){
      if(entry.getValue() == null || entry.getValue() < 1){
        continue;
      }
      
      int sides = entry.getKey();
      int numRolls = entry.getValue();
      outputs.put(sides, rollNSidedDice(sides, numRolls));
    }
    
    return outputs;
  }

  public static List<Integer> rollNSidedDice(int sides, int numRolls) {

    List<Integer> rolls = new LinkedList<Integer>();
    if(numRolls <= 0){
      throw new IllegalArgumentException();
    }
    for(int i = 0; i < numRolls; i ++){
      rolls.add(rollDie(sides));
    }
    return rolls;
  }

  protected static int rollDie(int sides) {
    if(sides <= 0){
      throw new IllegalArgumentException();
    }
    int randomInt = random.nextInt(sides + 1);
    return randomInt;
  }
}
