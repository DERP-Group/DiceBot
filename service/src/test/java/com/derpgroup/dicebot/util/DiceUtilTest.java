package com.derpgroup.dicebot.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class DiceUtilTest {

  @Test
  public void testRollDie(){
    int output = DiceUtil.rollDie(5);
    
    assertTrue(output >= 0);
    assertTrue(output <= 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRollDie_negativeSides(){
    DiceUtil.rollDie(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRollDie_zeroSides(){
    DiceUtil.rollDie(0);
  }

  @Test
  public void testRollNSidedDice_outputRange(){
    List<Integer> output = DiceUtil.rollNSidedDice(5, 100);
    
    assertNotNull(output);
    assertEquals(100,output.size());
    
    boolean lowerBound = false;
    boolean upperBound = false;
    for(int roll : output){
      assertTrue(roll >= 0);
      assertTrue(roll <= 5);
      if(roll == 0){
        lowerBound = true;
      }
      if(roll == 5){
        upperBound = true;
      }
    }
    assertTrue(lowerBound);
    assertTrue(upperBound);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRollNSidedDice_negativeRolls(){
    DiceUtil.rollNSidedDice(2, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRollNSidedDice_zeroRolls(){
    DiceUtil.rollNSidedDice(2, 0);
  }
}
