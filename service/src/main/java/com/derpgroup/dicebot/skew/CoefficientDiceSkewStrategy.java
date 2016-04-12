package com.derpgroup.dicebot.skew;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CoefficientDiceSkewStrategy implements DiceSkewStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(CoefficientDiceSkewStrategy.class);

  private static final int maxNumberOfRerolls = 100;
  
  private static Random random = new Random();
  
  private final float coefficientModifier;
  
  
  public CoefficientDiceSkewStrategy(float coefficientModifier){
    if(coefficientModifier < -1 || coefficientModifier > 1){
      throw new IllegalArgumentException();
    }
    this.coefficientModifier = coefficientModifier;
  }
  
  @Override
  public int skew(int value, int maxValue) {
    if(value <= 0 || value > maxValue){
      throw new IllegalArgumentException();
    }
    
    //Prematurely exit in cases where a skew won't help
    if(coefficientModifier == 0){
      return value;
    }else if(coefficientModifier > 0 && value == maxValue){
      return value;
    }else if(coefficientModifier < 0 && value == 1){
      return value;
    }
    
    int skewedValue = -1;
    for(int i = 0; (skewedValue <= 0 || skewedValue > maxValue); i++){
      int coefficient = calculateCoefficient(maxValue);
      skewedValue = value + coefficient;
      if(i == maxNumberOfRerolls){
        LOG.info("Failed to skew value " + value + " with max " + maxValue + " and coefficient modifier + " + coefficientModifier + ".");
        skewedValue = value;
      }
    }
    
    
    return skewedValue;
  }

  public int calculateCoefficient(int maxValue) {
    float coefficientRoll = random.nextFloat() * coefficientModifier;
    int coefficient = Math.round(coefficientRoll * maxValue);
    return coefficient;
  }

}
