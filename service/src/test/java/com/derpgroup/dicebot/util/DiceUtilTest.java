package com.derpgroup.dicebot.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.derpgroup.dicebot.skew.CoefficientDiceSkewStrategy;
import com.derpgroup.dicebot.skew.DiceSkewStrategy;

public class DiceUtilTest {

  DiceSkewStrategy strategy;
  
  @Before
  public void setup(){
    strategy = new CoefficientDiceSkewStrategy(0);
  }
  
  /////////////////////////////////////
  ////////// Fair Dice Tests //////////
  /////////////////////////////////////
  @Test
  public void testRollDie(){
    int output = DiceUtil.rollDie(5, null);
    
    assertTrue(output >= 0);
    assertTrue(output <= 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRollDie_negativeSides(){
    DiceUtil.rollDie(-1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRollDie_zeroSides(){
    DiceUtil.rollDie(0, null);
  }

  @Test
  public void testRollNSidedDice_outputRange(){
    List<Integer> output = DiceUtil.rollNSidedDice(5, 100, null);
    
    assertNotNull(output);
    assertEquals(100,output.size());
    
    boolean lowerBound = false;
    boolean upperBound = false;
    for(int roll : output){
      assertTrue(roll >= 1);
      assertTrue(roll <= 5);
      if(roll == 1){
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
    DiceUtil.rollNSidedDice(2, -1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRollNSidedDice_zeroRolls(){
    DiceUtil.rollNSidedDice(2, 0, null);
  }

  /////////////////////////////////////
  ///////// Skewed Dice Tests /////////
  /////////////////////////////////////
  
  @Test
  public void testRollDie_unskewed(){
    int output = DiceUtil.rollDie(5, strategy);
    
    assertTrue(output >= 0);
    assertTrue(output <= 5);
  }
  
  @Test
  public void testRollDie_lightlySkewed(){
    strategy = new CoefficientDiceSkewStrategy((float) .1);
    int output = DiceUtil.rollDie(5, strategy);
    
    assertTrue(output >= 0);
    assertTrue(output <= 5);
  }
  
  @Test
  public void testRollDie_heavilySkewed(){
    strategy = new CoefficientDiceSkewStrategy(1);
    int output = DiceUtil.rollDie(5, strategy);
    
    assertTrue(output >= 0);
    assertTrue(output <= 5);
  }

  @Test
  public void testRollNSidedDice__unskewed_outputRange(){
    List<Integer> output = DiceUtil.rollNSidedDice(5, 100, strategy);
    
    assertNotNull(output);
    assertEquals(100,output.size());
    
    boolean lowerBound = false;
    boolean upperBound = false;
    for(int roll : output){
      assertTrue(roll >= 1);
      assertTrue(roll <= 5);
      if(roll == 1){
        lowerBound = true;
      }
      if(roll == 5){
        upperBound = true;
      }
    }
    assertTrue(lowerBound);
    assertTrue(upperBound);
  }

  @Test
  public void testRollNSidedDice__lightlySkewed_outputRange(){
    strategy = new CoefficientDiceSkewStrategy((float) .1);
    List<Integer> output = DiceUtil.rollNSidedDice(5, 100, strategy);
    
    assertNotNull(output);
    assertEquals(100,output.size());
    
    boolean lowerBound = false;
    boolean upperBound = false;
    for(int roll : output){
      assertTrue(roll >= 1);
      assertTrue(roll <= 5);
      if(roll == 1){
        lowerBound = true;
      }
      if(roll == 5){
        upperBound = true;
      }
    }
    assertTrue(lowerBound);
    assertTrue(upperBound);
  }

  @Test
  public void testRollNSidedDice__heavilySkewed_outputRange(){
    strategy = new CoefficientDiceSkewStrategy(1);
    List<Integer> output = DiceUtil.rollNSidedDice(5, 100, strategy);
    
    assertNotNull(output);
    assertEquals(100,output.size());
    
    boolean lowerBound = false;
    boolean upperBound = false;
    for(int roll : output){
      assertTrue(roll >= 1);
      assertTrue(roll <= 5);
      if(roll == 1){
        lowerBound = true;
      }
      if(roll == 5){
        upperBound = true;
      }
    }
    assertTrue(lowerBound);
    assertTrue(upperBound);
  }
  
  @Ignore //This is just here for doing comparisons of different values - not an actual test
  @Test
  public void testCoefficientValue(){
    strategy = new CoefficientDiceSkewStrategy((float) .2);
    
    int unskewedSum = 0;
    int skewedSum = 0;
    for(int i = 0; i < 1000; i++){
      unskewedSum += DiceUtil.rollNSidedDice(20, 1, null).get(0);
      skewedSum += DiceUtil.rollNSidedDice(20, 1, strategy).get(0);
    }
    
    System.out.println("Unskewed: " + unskewedSum);
    System.out.println("Skewed: " + skewedSum);
  }
  
}
