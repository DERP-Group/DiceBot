package com.derpgroup.dicebot.manager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.ConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.derpwizard.voice.exception.DerpwizardException;
import com.derpgroup.derpwizard.voice.model.ConversationHistoryEntry;
import com.derpgroup.derpwizard.voice.model.ServiceOutput;
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.derpwizard.voice.model.ServiceInput;
import com.derpgroup.derpwizard.voice.util.ConversationHistoryUtils;
import com.derpgroup.dicebot.DiceBotMetadata;
import com.derpgroup.dicebot.MixInModule;
import com.derpgroup.dicebot.configuration.DiceBotConfig;
import com.derpgroup.dicebot.skew.CoefficientDiceSkewStrategy;
import com.derpgroup.dicebot.skew.DiceSkewStrategy;
import com.derpgroup.dicebot.util.DiceSoundsUtil;
import com.derpgroup.dicebot.util.DiceUtil;

public class DiceBotManager{
  private final Logger LOG = LoggerFactory.getLogger(DiceBotManager.class);
  private final float defaultCoefficientModifierScalar = (float) .1;
  private final boolean includeDiceSounds = true; //replace this with user specific config later
  private static final String AUDIO_TAG_PATTERN = "<audio src=\"%s\" />";
  
  public static final String[] META_SUBJECT_VALUES = new String[] { "REPEAT" };
  public static final Set<String> META_SUBJECTS = new HashSet<String>(Arrays.asList(META_SUBJECT_VALUES));
  
  private DiceSoundsUtil diceSoundsUtil;

  static {
    ConversationHistoryUtils.getMapper().registerModule(new MixInModule());
  }

  public DiceBotManager(DiceBotConfig diceBotConfig) {
    super();
    String diceBotSoundsRootPath = diceBotConfig.getDiceBotSoundsRootPath();
    diceSoundsUtil = new DiceSoundsUtil(diceBotSoundsRootPath);
  }

  public void handleRequest(ServiceInput serviceInput,
      ServiceOutput serviceOutput) throws DerpwizardException {
    String messageSubject = serviceInput.getSubject();
    
    switch(messageSubject){
    case "START_OF_CONVERSATION":
      doHelloRequest(serviceInput, serviceOutput);
      break;
    case "END_OF_CONVERSATION":
      return;
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

  //TODO: Refactor these three methods into one, keeping in mind their expansion in the future
  private void doRollDieRequest(ServiceInput serviceInput, ServiceOutput serviceOutput) {
    Map<String, String> messageMap = serviceInput.getMessageAsMap();
    Map<Integer, Integer> rollParameters = getRollParameters(messageMap);
    
    DiceSkewStrategy strategy = new CoefficientDiceSkewStrategy(0 * defaultCoefficientModifierScalar);
    
    Map<Integer, List<Integer>> rollsByNumSides = DiceUtil.rollDice(rollParameters, strategy);
    
    buildResponseFromRolls(rollsByNumSides, serviceOutput);
  }

  private void doDownwardWeightedRollDieRequest(ServiceInput serviceInput, ServiceOutput serviceOutput) {
    Map<String, String> messageMap = serviceInput.getMessageAsMap();
    Map<Integer, Integer> rollParameters = getRollParameters(messageMap);
    
    DiceSkewStrategy strategy = new CoefficientDiceSkewStrategy(-1 * defaultCoefficientModifierScalar );
    
    Map<Integer, List<Integer>> rollsByNumSides = DiceUtil.rollDice(rollParameters, strategy);
    
    buildResponseFromRolls(rollsByNumSides, serviceOutput);
  }

  private void doUpwardWeightedRollDieRequest(ServiceInput serviceInput, ServiceOutput serviceOutput) {
    Map<String, String> messageMap = serviceInput.getMessageAsMap();
    Map<Integer, Integer> rollParameters = getRollParameters(messageMap);
    
    DiceSkewStrategy strategy = new CoefficientDiceSkewStrategy(1 * defaultCoefficientModifierScalar);
    
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

  private void doRepeatRequest(ServiceInput serviceInput, ServiceOutput serviceOutput) throws DerpwizardException {
    DiceBotMetadata inputMetadata = (DiceBotMetadata)serviceInput.getMetadata();
    if(inputMetadata == null || inputMetadata.getConversationHistory() == null || inputMetadata.getConversationHistory().size() < 1){
      LOG.error("User ended up on repeat with no previous requests. Redirecting to launch.");
      doHelloRequest(serviceInput, serviceOutput);
      return;
    }
    
    ConversationHistoryEntry conversationHistoryEntry = ConversationHistoryUtils.getLastNonMetaRequestBySubject(inputMetadata.getConversationHistory(), META_SUBJECTS);
    if(conversationHistoryEntry == null || StringUtils.isEmpty(conversationHistoryEntry.getMessageSubject())){
      LOG.error("User ended up on repeat with invalid conversation history. Redirecting to launch.");
      doHelloRequest(serviceInput, serviceOutput);
      return;
    }
    
    String subject = conversationHistoryEntry.getMessageSubject().toUpperCase();
    switch(subject){
      case "ROLL_DIE":
      case "ROLL_ME_A_DIE":
      case "ROLL_A_DIE_FOR_ME":
        
        if(inputMetadata.getPreviousRolls() == null){
          LOG.error("User asked to repeat dice with no previous rolls. Redirecting to launch.");
          doHelloRequest(serviceInput, serviceOutput);
          
        }

        Map<Integer, List<Integer>> previousRolls = inputMetadata.getPreviousRolls();

        StringBuilder textOutput = new StringBuilder("Rolls were: ");
        StringBuilder voiceOutput = new StringBuilder("Rolls were <break /> ");
        for(Entry<Integer, List<Integer>> entry : previousRolls.entrySet()){
          List<Integer> rolls = entry.getValue();
          for(Integer roll : rolls){
            textOutput.append(roll + ",");
            voiceOutput.append(roll + "<break />");
          }
        }
        
        String textMessage = textOutput.substring(0, textOutput.length() - 1);
        String voiceMessage = voiceOutput.toString();
        serviceOutput.getVisualOutput().setTitle("Previous results: ");
        serviceOutput.getVisualOutput().setText(textMessage);
        serviceOutput.getVoiceOutput().setSsmltext(voiceMessage);
        
        break;
      default:
        serviceInput.setSubject(subject);
        handleRequest(serviceInput, serviceOutput);
        break;
    }
  }

  protected void doHelloRequest(ServiceInput serviceInput,
      ServiceOutput serviceOutput){
    serviceOutput.setConversationEnded(false);

    StringBuilder outputMessageBuilder = new StringBuilder();
    outputMessageBuilder.append("Here to test the fates? Lets roll some dice!");
    StringBuilder delayedOutputMessageBuilder = new StringBuilder();
    delayedOutputMessageBuilder.append("You can start by saying<break /> roll a die<break /> or say help if you want to hear more options.");
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
        try{
          numSides = Integer.parseInt(sides);
        }catch(NumberFormatException e){
          numSides = 6;
          LOG.error(e.getMessage());
        }
      }
      
      if(StringUtils.isEmpty(dice)){
        numDice = 1;
      }else{
        try{
          numDice = Integer.parseInt(dice);
        }catch(NumberFormatException e){
          numDice = 1;
          LOG.error(e.getMessage());
        }
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

    StringBuilder textOutput = new StringBuilder();
    StringBuilder voiceOutput = new StringBuilder();
    voiceOutput.append("Alright. "); //Randomize me
    for(Entry<Integer, List<Integer>> entry : rollsByNumSides.entrySet()){
      int numSides = entry.getKey();
      List<Integer> rolls = entry.getValue();
      
      String soundUrl = null;
      if(includeDiceSounds){
        try {
          soundUrl = diceSoundsUtil.buildDiceSoundUrl(rolls.size(), numSides);
        } catch (IllegalArgumentException | ConfigurationException e) {
          LOG.error(e.getMessage());
        }
      }
      
      if(soundUrl != null){
        String audioString = String.format(AUDIO_TAG_PATTERN, soundUrl);
        voiceOutput.append(audioString);
      }
      
      String diceWord;
      String valuesWord;
      if(rolls.size() == 1){
        diceWord = "die";
        valuesWord = "a";
      }else{
        diceWord = "dice";
        valuesWord = "the following ";
      }
      textOutput.append("I rolled " + rolls.size() + " " + numSides + " sided " + diceWord + ", and got " + valuesWord + ": ");
      voiceOutput.append("I rolled " + rolls.size() + " " + numSides + " sided  " + diceWord + "<break /> and got " + valuesWord + "<break /> ");
      for(Integer roll : rolls){
        textOutput.append(roll + ",");
        voiceOutput.append(roll + "<break />");
      }
    }
    
    String textMessage = textOutput.substring(0, textOutput.length() - 1);
    String voiceMessage = voiceOutput.toString();
    serviceOutput.getVisualOutput().setTitle("Results: ");
    serviceOutput.getVisualOutput().setText(textMessage);
    serviceOutput.getVoiceOutput().setSsmltext(voiceMessage);
    
    DiceBotMetadata metadata = (DiceBotMetadata)serviceOutput.getMetadata();
    metadata.setPreviousRolls(rollsByNumSides);
  }
}
