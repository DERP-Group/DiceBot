package com.derpgroup.dicebot.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.derpgroup.dicebot.skew.DiceSkewStrategy;

public abstract class DiceUtil {

  private static Random random = new Random();
  
  public static synchronized Map<Integer, List<Integer>> rollDice(Map<Integer, Integer> inputs, DiceSkewStrategy skewStrategy){
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
      outputs.put(sides, rollNSidedDice(sides, numRolls, skewStrategy));
    }
    
    return outputs;
  }

  public static List<Integer> rollNSidedDice(int sides, int numRolls, DiceSkewStrategy skewStrategy) {

    List<Integer> rolls = new LinkedList<Integer>();
    if(numRolls <= 0){
      throw new IllegalArgumentException();
    }
    for(int i = 0; i < numRolls; i ++){
      rolls.add(rollDie(sides, skewStrategy));
    }
    return rolls;
  }

  protected static int rollDie(int sides, DiceSkewStrategy skewStrategy) {
    if(sides <= 0){
      throw new IllegalArgumentException();
    }
    int roll = 1 + random.nextInt(sides);
    if(skewStrategy != null){
      return skewStrategy.skew(roll, sides);
    }
    return roll;
  }
}
