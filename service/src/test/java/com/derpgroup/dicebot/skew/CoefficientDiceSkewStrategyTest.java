package com.derpgroup.dicebot.skew;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.derpgroup.dicebot.util.DiceUtil;

public class CoefficientDiceSkewStrategyTest {

  private CoefficientDiceSkewStrategy strategy;
  
  @Before
  public void setup(){
    strategy = new CoefficientDiceSkewStrategy((float).5);
  }
  
  @Test
  public void testSkew_upwardCoefficientModifierLevels(){

    CoefficientDiceSkewStrategy lightStrategy = new CoefficientDiceSkewStrategy((float).1);
    CoefficientDiceSkewStrategy mediumStrategy = new CoefficientDiceSkewStrategy((float).5);
    CoefficientDiceSkewStrategy heavyStrategy = new CoefficientDiceSkewStrategy((float)1);
    int numSides = 20;
    List<Integer> originalRolls = DiceUtil.rollNSidedDice(numSides, 100, null);
    int sum = 0;
    int lightSkewedSum = 0;
    int mediumSkewedSum = 0;
    int heavySkewedSum = 0;
    for(int roll : originalRolls){
      sum += roll;
      lightSkewedSum += lightStrategy.skew(roll, numSides);
      mediumSkewedSum += mediumStrategy.skew(roll, numSides);
      heavySkewedSum += heavyStrategy.skew(roll, numSides);
    }
    
    assertTrue(sum < lightSkewedSum);
    assertTrue(lightSkewedSum < mediumSkewedSum);
    assertTrue(mediumSkewedSum < heavySkewedSum);
  }
  
  @Test
  public void testSkew_downwardCoefficientModifierLevels(){

    CoefficientDiceSkewStrategy lightStrategy = new CoefficientDiceSkewStrategy((float)-.1);
    CoefficientDiceSkewStrategy mediumStrategy = new CoefficientDiceSkewStrategy((float)-.5);
    CoefficientDiceSkewStrategy heavyStrategy = new CoefficientDiceSkewStrategy((float)-1);
    int numSides = 20;
    List<Integer> originalRolls = DiceUtil.rollNSidedDice(numSides, 100, null);
    int sum = 0;
    int lightSkewedSum = 0;
    int mediumSkewedSum = 0;
    int heavySkewedSum = 0;
    for(int roll : originalRolls){
      sum += roll;
      lightSkewedSum += lightStrategy.skew(roll, numSides);
      mediumSkewedSum += mediumStrategy.skew(roll, numSides);
      heavySkewedSum += heavyStrategy.skew(roll, numSides);
    }
    
    assertTrue(sum > lightSkewedSum);
    assertTrue(lightSkewedSum > mediumSkewedSum);
    assertTrue(mediumSkewedSum > heavySkewedSum);
  }
  
  @Test
  public void testSkew_inBounds(){
    int numSides = 20;
    List<Integer> originalRolls = DiceUtil.rollNSidedDice(numSides, 100, null);
    for(int roll : originalRolls){ 
      int skewedRoll = strategy.skew(roll, numSides);
      assertTrue(skewedRoll > 0);
      assertTrue(skewedRoll <= numSides);
    }
  }
  
  @Test
  public void testSkew_minimalSkewSpace(){
     
    int skewedRoll = strategy.skew(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE - 1,skewedRoll);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSkew_valueOutOfBoundsLow(){
    strategy.skew(0, 1);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSkew_valueOutOfBoundsHigh(){
    strategy.skew(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
  }
  
  @Test
  public void testSkew_noSkewSpaceHigh(){
    int skewedRoll = strategy.skew(Integer.MAX_VALUE, Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE,skewedRoll);
  }
  
  @Test
  public void testSkew_noSkewSpaceLow(){
    strategy = new CoefficientDiceSkewStrategy((float)-.5);
    int skewedRoll = strategy.skew(1, 2);
    assertEquals(1,skewedRoll);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_coefficientModifierOutOfBoundsHigh(){
    new CoefficientDiceSkewStrategy(2);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_coefficientModifierOutOfBoundsLow(){
    new CoefficientDiceSkewStrategy(-2);
  }
  
  @Test
  public void testSkew_noCoefficientModifier(){
    strategy = new CoefficientDiceSkewStrategy(0);
    int numSides = 20;
    List<Integer> originalRolls = DiceUtil.rollNSidedDice(numSides, 100, null);
    for(int roll : originalRolls){ 
      int skewedRoll = strategy.skew(roll, numSides);
      assertTrue(skewedRoll == roll);
    }
  }
}
