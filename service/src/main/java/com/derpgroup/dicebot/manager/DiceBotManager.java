package com.derpgroup.dicebot.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.derpwizard.voice.exception.DerpwizardException;
import com.derpgroup.derpwizard.voice.model.ServiceOutput;
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.derpwizard.voice.model.ServiceInput;
import com.derpgroup.derpwizard.voice.util.ConversationHistoryUtils;
import com.derpgroup.dicebot.DiceBotMetadata;
import com.derpgroup.dicebot.MixInModule;
import com.derpgroup.dicebot.skew.CoefficientDiceSkewStrategy;
import com.derpgroup.dicebot.skew.DiceSkewStrategy;
import com.derpgroup.dicebot.util.DiceUtil;

public class DiceBotManager{
  private final Logger LOG = LoggerFactory.getLogger(DiceBotManager.class);

  static {
    ConversationHistoryUtils.getMapper().registerModule(new MixInModule());
  }

  public DiceBotManager() {
    super();
  }

  public void handleRequest(ServiceInput serviceInput,
      ServiceOutput serviceOutput) throws DerpwizardException {
    String messageSubject = serviceInput.getSubject();
    
    switch(messageSubject){
    case "START_OF_CONVERSATION":
      doHelloRequest(serviceInput, serviceOutput);
      break;
    case "ROLL_DIE":
      doRollDieRequest(serviceInput, serviceOutput);
      break;
    case "ROLL_ME_A_DIE":
      doUpwardWeightedRollDieRequest(serviceInput, serviceOutput);
      break;
    case "ROLL_A_DIE_FOR_ME":
      doDownwardWeightedRollDieRequest(serviceInput, serviceOutput);
      break;
    case "HELP":
      doHelpRequest(serviceInput, serviceOutput);
      break;
    case "REPEAT":
      doRepeatRequest(serviceInput, serviceOutput);
      break;
    case "STOP":
      doStopRequest(serviceInput, serviceOutput);
      break;
    case "CANCEL":
      doStopRequest(serviceInput, serviceOutput);
      break;
      default:
        String message = "Unknown request type '" + messageSubject + "'.";
        LOG.warn(message);
        throw new DerpwizardException(new SsmlDocumentBuilder().text(message).build().getSsml(), message, "Unknown request.");
    }
  }

  private void doRollDieRequest(ServiceInput serviceInput, ServiceOutput serviceOutput) {
    Map<String, String> messageMap = serviceInput.getMessageAsMap();
    Map<Integer, Integer> rollParameters = getRollParameters(messageMap);
    
    Map<Integer, List<Integer>> rollsByNumSides = DiceUtil.rollDice(rollParameters, null);
    
    buildResponseFromRolls(rollsByNumSides, serviceOutput);
  }

  private void doDownwardWeightedRollDieRequest(ServiceInput serviceInput, ServiceOutput serviceOutput) {
    Map<String, String> messageMap = serviceInput.getMessageAsMap();
    Map<Integer, Integer> rollParameters = getRollParameters(messageMap);
    
    DiceSkewStrategy strategy = new CoefficientDiceSkewStrategy(-1);
    
    Map<Integer, List<Integer>> rollsByNumSides = DiceUtil.rollDice(rollParameters, strategy);
    
    buildResponseFromRolls(rollsByNumSides, serviceOutput);
  }

  private void doUpwardWeightedRollDieRequest(ServiceInput serviceInput, ServiceOutput serviceOutput) {
    Map<String, String> messageMap = serviceInput.getMessageAsMap();
    Map<Integer, Integer> rollParameters = getRollParameters(messageMap);
    
    DiceSkewStrategy strategy = new CoefficientDiceSkewStrategy(1);
    
    Map<Integer, List<Integer>> rollsByNumSides = DiceUtil.rollDice(rollParameters, strategy);
    
    buildResponseFromRolls(rollsByNumSides, serviceOutput);
  }

  protected void doHelpRequest(ServiceInput serviceInput,
      ServiceOutput serviceOutput) throws DerpwizardException {
    serviceOutput.setConversationEnded(false);

    StringBuilder sb = new StringBuilder();
    sb.append("Example requests:");
    sb.append("\n\n");
    sb.append("\"Roll me three dice\"");
    sb.append("\n");
    sb.append("\"Roll a twelve sided die\"");
    sb.append("\n");
    sb.append("\"Roll 4d20\"");
    sb.append("\n");
    sb.append("\"Repeat that\"");
    String cardMessage = sb.toString();
    serviceOutput.getVisualOutput().setTitle("How it works:");
    serviceOutput.getVisualOutput().setText(cardMessage);

    String audioMessage = "You can use commands like <break />Roll me three dice<break />Roll a twelve sided die<break /> or <break />Roll four d twenty. You can also say repeat to hear your rolls again, or say stop to exit.";
    serviceOutput.getVoiceOutput().setSsmltext(audioMessage);
  }

  private void doRepeatRequest(ServiceInput serviceInput, ServiceOutput serviceOutput) {
    
  }

  protected void doHelloRequest(ServiceInput serviceInput,
      ServiceOutput serviceOutput) throws DerpwizardException {
    serviceOutput.setConversationEnded(false);

    StringBuilder outputMessageBuilder = new StringBuilder();
    outputMessageBuilder.append("Here to test the fates? Lets roll some dice!");
    StringBuilder delayedOutputMessageBuilder = new StringBuilder();
    delayedOutputMessageBuilder.append("You can start by saying<break /> roll a die</break> or say help if you want to hear more options.");
    String outputMessage = outputMessageBuilder.toString();
    serviceOutput.getVisualOutput().setTitle("Hello...");
    serviceOutput.getVisualOutput().setText(outputMessage);
    serviceOutput.getVoiceOutput().setSsmltext(outputMessage);
    serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedOutputMessageBuilder.toString());
  }

  protected void doStopRequest(ServiceInput serviceInput, ServiceOutput serviceOutput) throws DerpwizardException {
    serviceOutput.getVoiceOutput().setSsmltext("See ya!");
  }

  private Map<Integer, Integer> getRollParameters(Map<String, String> messageMap) {
    Map<Integer, Integer> rollParameters = new LinkedHashMap<Integer, Integer>();
    int numSides;
    int numDice;
    if(messageMap == null || messageMap.isEmpty()){
      LOG.debug("No information about dice was provided, falling back to a single default sided die.");
      numSides = 6;
      numDice = 1;
    }else{ 
      String dice = messageMap.get("dice");
      String sides = messageMap.get("sides");
      if(StringUtils.isEmpty(sides)){
        numSides = 6;
      }else{
        numSides = Integer.parseInt(sides);
      }
      
      if(StringUtils.isEmpty(dice)){
        numDice = 1;
      }else{
        numDice = Integer.parseInt(dice);
      }
    }

    LOG.debug("Adding " + numDice + " dice with " + numSides + " sides each.");
    
    rollParameters.put(numSides, numDice);
    
    return rollParameters;
  }

  private void buildResponseFromRolls(Map<Integer, List<Integer>> rollsByNumSides, ServiceOutput serviceOutput) {
    
    if(rollsByNumSides == null){
      throw new IllegalArgumentException();
    }

    StringBuilder sb = new StringBuilder();
    for(Entry<Integer, List<Integer>> entry : rollsByNumSides.entrySet()){
      int numSides = entry.getKey();
      List<Integer> rolls = entry.getValue();
      sb.append("I rolled " + rolls.size() + " " + numSides + " sided dice, with values: ");
      for(Integer roll : rolls){
        sb.append(roll + ",");
      }
    }
    
    String message = sb.toString();
    serviceOutput.getVisualOutput().setTitle("Results: ");
    serviceOutput.getVisualOutput().setText(message);
    serviceOutput.getVoiceOutput().setSsmltext(message);
    
    DiceBotMetadata metadata = (DiceBotMetadata)serviceOutput.getMetadata();
    metadata.setRolls(rollsByNumSides);
  }
}
