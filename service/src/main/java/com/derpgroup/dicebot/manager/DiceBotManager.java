package com.derpgroup.dicebot.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.derpwizard.voice.exception.DerpwizardException;
import com.derpgroup.derpwizard.voice.model.ServiceOutput;
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.derpwizard.voice.model.ServiceInput;
import com.derpgroup.derpwizard.voice.util.ConversationHistoryUtils;
import com.derpgroup.dicebot.MixInModule;
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
      default:
        String message = "Unknown request type '" + messageSubject + "'.";
        LOG.warn(message);
        throw new DerpwizardException(new SsmlDocumentBuilder().text(message).build().getSsml(), message, "Unknown request.");
    }
  }

  private void doRollDieRequest(ServiceInput serviceInput, ServiceOutput serviceOutput) {
    Map<Integer, Integer> dice = new LinkedHashMap<Integer, Integer>();
    Map<String, String> messageMap = serviceInput.getMessageAsMap();
    if(messageMap == null || messageMap.isEmpty() || !messageMap.containsKey("sides")){
      LOG.debug("No information about dice was provided, falling back to default.");
      dice.put(6, 1);
    }else{
      String sides = messageMap.get("sides");
      LOG.debug("Adding a single '" + sides + "' sided die.");
      dice.put(Integer.parseInt(sides), 1);
    }
    
    Map<Integer, List<Integer>> rollsByNumSides = DiceUtil.rollDice(dice);
    
    buildResponseFromRolls(rollsByNumSides, serviceOutput);
  }

  private void buildResponseFromRolls(Map<Integer, List<Integer>> rollsByNumSides, ServiceOutput serviceOutput) {
    
    if(rollsByNumSides == null){
      throw new IllegalArgumentException();
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Here are your results:");
    for(Entry<Integer, List<Integer>> entry : rollsByNumSides.entrySet()){
      int numSides = entry.getKey();
      List<Integer> rolls = entry.getValue();
      sb.append("I rolled " + rolls.size() + " " + numSides + " sided dice, with values: ");
      for(Integer roll : rolls){
        sb.append(roll + "<break />");
      }
    }
    
    String message = sb.toString();
    serviceOutput.getVisualOutput().setTitle("Results: ");
    serviceOutput.getVisualOutput().setText(message);
    serviceOutput.getVoiceOutput().setSsmltext(message);
  }

  protected void doHelpRequest(ServiceInput serviceInput,
      ServiceOutput serviceOutput) throws DerpwizardException {
    serviceOutput.setConversationEnded(false);

    StringBuilder sb = new StringBuilder();
    sb.append("Example requests:");
    sb.append("\n\n");
    sb.append("\"Do I have any friends on Steam?\"");
    String cardMessage = sb.toString();
    serviceOutput.getVisualOutput().setTitle("How it works:");
    serviceOutput.getVisualOutput().setText(cardMessage);

    String audioMessage = "You can ask questions such as <break />Do I have any friends on Steam right now?<break /> or <break />Are any of my favorite Twitch streams live at the moment?";
    serviceOutput.getVoiceOutput().setSsmltext(audioMessage);
  }

  protected void doHelloRequest(ServiceInput serviceInput,
      ServiceOutput serviceOutput) throws DerpwizardException {
    serviceOutput.setConversationEnded(false);

    StringBuilder outputMessageBuilder = new StringBuilder();
    outputMessageBuilder.append("Lets roll some dice!");
    String outputMessage = outputMessageBuilder.toString();
    serviceOutput.getVisualOutput().setTitle("Hello...");
    serviceOutput.getVisualOutput().setText(outputMessage);
    serviceOutput.getVoiceOutput().setSsmltext(outputMessage);
  }

  protected void doGoodbyeRequest(ServiceInput serviceInput,
      ServiceOutput serviceOutput) throws DerpwizardException {
  }

  protected void doCancelRequest(ServiceInput serviceInput,
      ServiceOutput serviceOutput) throws DerpwizardException {
  }

  protected void doStopRequest(ServiceInput serviceInput,
      ServiceOutput serviceOutput) throws DerpwizardException {
  }
}
