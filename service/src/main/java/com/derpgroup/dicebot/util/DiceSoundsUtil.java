package com.derpgroup.dicebot.util;


import java.util.Random;

import javax.naming.ConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiceSoundsUtil {
  private final Logger LOG = LoggerFactory.getLogger(DiceSoundsUtil.class);

  private static final int numSoundIterations = 10;
  private static final int numDiceForHandful = 6;
  private String diceBotSoundsRootPath;
  private Random random;
  
  public DiceSoundsUtil(String diceBotSoundsRootPath) {
    if(StringUtils.isEmpty(diceBotSoundsRootPath)){
      LOG.warn("No valid root path provided for dice sounds.");
    }
    this.diceBotSoundsRootPath = diceBotSoundsRootPath;
    random = new Random();
  }
  
  //TODO build this via singleton
  public String buildDiceSoundUrl(int numDice, int numSides) throws IllegalArgumentException, ConfigurationException{
    if(StringUtils.isEmpty(diceBotSoundsRootPath)){
      throw new ConfigurationException();
    }
    if(numDice < 1){
      LOG.error("Illegal number of dice requested: " + numDice);
      throw new IllegalArgumentException();
    }
    if(numSides < 3){
      LOG.error("Illegal number of sides requested: " + numSides);
      throw new IllegalArgumentException();
    }
    
    int soundIteration = random.nextInt(numSoundIterations) + 1;
    StringBuilder url = new StringBuilder(diceBotSoundsRootPath);
    
    int numDiceTranslated;
    int numSidesTranslated;
    if(numDice >= numDiceForHandful){
      url.append("HandfulOfDice/HandfulOfDice-");
      url.append(String.format("%02d",soundIteration));
      url.append(".mp3");
      return url.toString();
    }else if(numDice > 1){
      numDiceTranslated = 3;
    }else{
      numDiceTranslated = 1;
    }
    
    if(numSides > 8){
      numSidesTranslated = 20;
    }else if(numSides > 5){
      numSidesTranslated = 6;
    }else{
      numSidesTranslated = 4;
    }
    
    String dicePattern = String.format("%dd%d",numDiceTranslated,numSidesTranslated);
    url.append(dicePattern);
    url.append("/");
    url.append(dicePattern);
    url.append("-");
    url.append(String.format("%02d",soundIteration));
    url.append(".mp3");
    return url.toString();
  }
}
