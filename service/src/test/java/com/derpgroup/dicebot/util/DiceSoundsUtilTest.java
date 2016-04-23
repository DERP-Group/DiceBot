package com.derpgroup.dicebot.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import javax.naming.ConfigurationException;

import org.junit.Before;
import org.junit.Test;

public class DiceSoundsUtilTest {

  private DiceSoundsUtil util;
  private String rootUrl = "http://rootUrl/";
  
  @Before
  public void setup(){
    util = new DiceSoundsUtil(rootUrl);
  }
  
  @Test
  public void testRootUrl() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "1d20/1d20";
    String url = util.buildDiceSoundUrl(1,20);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testTwoTwentySidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "3d20/3d20";
    String url = util.buildDiceSoundUrl(2, 20);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testFiveTwentySidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "3d20/3d20";
    String url = util.buildDiceSoundUrl(5, 20);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testThreeTwentySidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "3d20/3d20";
    String url = util.buildDiceSoundUrl(3, 20);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testManyTwentySidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "HandfulOfDice/HandfulOfDice";
    String url = util.buildDiceSoundUrl(6, 20);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testThreeThreeSidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "3d4/3d4";
    String url = util.buildDiceSoundUrl(3, 3);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testThreeFourSidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "3d4/3d4";
    String url = util.buildDiceSoundUrl(3, 4);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testThreeFiveSidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "3d4/3d4";
    String url = util.buildDiceSoundUrl(3, 5);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testThreeSixSidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "3d6/3d6";
    String url = util.buildDiceSoundUrl(3, 6);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testThreeEightSidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "3d6/3d6";
    String url = util.buildDiceSoundUrl(3, 8);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testThreeNineSidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "3d20/3d20";
    String url = util.buildDiceSoundUrl(3, 9);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testThreeMaxIntSidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "3d20/3d20";
    String url = util.buildDiceSoundUrl(3, Integer.MAX_VALUE);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test
  public void testMaxIntTwentySidedDice() throws IllegalArgumentException, ConfigurationException{
    String expectedString = rootUrl + "HandfulOfDice/HandfulOfDice";
    String url = util.buildDiceSoundUrl(Integer.MAX_VALUE, 20);
    assertEquals(expectedString,url.split("-")[0]);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testZeroDice() throws IllegalArgumentException, ConfigurationException{
    util.buildDiceSoundUrl(0, 20);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testNegativeDice() throws IllegalArgumentException, ConfigurationException{
    util.buildDiceSoundUrl(-1, 20);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testCoin() throws IllegalArgumentException, ConfigurationException{
    util.buildDiceSoundUrl(1, 2);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testOneSidedDie() throws IllegalArgumentException, ConfigurationException{
    util.buildDiceSoundUrl(1, 1);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testZeroSidedDie() throws IllegalArgumentException, ConfigurationException{
    util.buildDiceSoundUrl(1, 0);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testNegativeSidedDie() throws IllegalArgumentException, ConfigurationException{
    util.buildDiceSoundUrl(1, -1);
  }
  
  @Test
  public void testOutputRange() throws IllegalArgumentException, ConfigurationException{
    Set<String> iterationNumbers = new HashSet<String>();
    for(int i = 0; i < 100; i++){
      String dice = util.buildDiceSoundUrl(6, 6);
      String iteration = dice.split("-")[1];
      iterationNumbers.add(iteration);
    }
    
    assertEquals(10, iterationNumbers.size());
    assertTrue(iterationNumbers.contains("01"));
    assertTrue(iterationNumbers.contains("10"));
  }
  
  @Test(expected=ConfigurationException.class)
  public void testNoRootUrl() throws IllegalArgumentException, ConfigurationException{
    DiceSoundsUtil util = new DiceSoundsUtil(null);
    util.buildDiceSoundUrl(1, -1);
  }
}
