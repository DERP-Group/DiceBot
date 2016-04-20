/**
 * Copyright (C) 2015 David Phillips
 * Copyright (C) 2015 Eric Olson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.derpgroup.dicebot.resource;

import java.util.Map;
import java.util.UUID;

import io.dropwizard.setup.Environment;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.SpeechletRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.derpgroup.derpwizard.dao.AccountLinkingDAO;
import com.derpgroup.derpwizard.model.accountlinking.ExternalAccountLink;
import com.derpgroup.derpwizard.model.accountlinking.UserAccount;
import com.derpgroup.derpwizard.voice.exception.DerpwizardException;
import com.derpgroup.derpwizard.voice.exception.DerpwizardExceptionAlexaWrapper;
import com.derpgroup.derpwizard.voice.exception.DerpwizardException.DerpwizardExceptionReasons;
import com.derpgroup.derpwizard.voice.alexa.AlexaUtils;
import com.derpgroup.derpwizard.voice.model.CommonMetadata;
import com.derpgroup.derpwizard.voice.model.ServiceOutput;
import com.derpgroup.derpwizard.voice.model.ServiceInput;
import com.derpgroup.derpwizard.voice.util.ConversationHistoryUtils;
import com.derpgroup.dicebot.DiceBotMetadata;
import com.derpgroup.dicebot.MixInModule;
import com.derpgroup.dicebot.configuration.DiceBotMainConfig;
import com.derpgroup.dicebot.manager.DiceBotManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST APIs for requests generating from Amazon Alexa
 *
 * @author Eric
 * @since 0.0.1
 */
@Path("/dicebot/alexa")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DiceBotAlexaResource {

  private static final Logger LOG = LoggerFactory.getLogger(DiceBotAlexaResource.class);

  private DiceBotManager manager;
  
  private ObjectMapper mapper;
  
  private AccountLinkingDAO accountLinkingDAO;
  
  public DiceBotAlexaResource(DiceBotMainConfig config, Environment env, AccountLinkingDAO accountLinkingDAO) {
    manager = new DiceBotManager();
    mapper = new ObjectMapper().registerModule(new MixInModule());
    
    this.accountLinkingDAO = accountLinkingDAO;
  }

  /**
   * @return The message, never null
   */
  @POST
  public SpeechletResponseEnvelope doAlexaRequest(SpeechletRequestEnvelope request, @HeaderParam("SignatureCertChainUrl") String signatureCertChainUrl, 
      @HeaderParam("Signature") String signature, @QueryParam("testFlag") Boolean testFlag){
    DiceBotMetadata outputMetadata = null;
    try {
      if (request.getRequest() == null) {
        throw new DerpwizardException(DerpwizardExceptionReasons.MISSING_INFO.getSsml(),"Missing request body.");
      }
  
      Map<String, Object> sessionAttributes = request.getSession().getAttributes();
      
      if(request.getSession() == null || request.getSession().getUser() == null){
        String message = "Alexa request did not contain a valid userId.";
        LOG.error(message);
        throw new DerpwizardException(message);
      } 
      
      String alexaUserId = request.getSession().getUser().getUserId();
      
      if(StringUtils.isEmpty(alexaUserId)){
        String message = "Missing Alexa userId.";
        LOG.error(message);
        throw new DerpwizardException(message);
      }
      
      ExternalAccountLink alexaAccountLink = accountLinkingDAO.getAccountLinkByExternalUserIdAndExternalSystemName(alexaUserId, "ALEXA");
      String userId = null;
      
      if(alexaAccountLink == null){     
        LOG.info("No Alexa account link found for this user, creating a new user account and Alexa link.");
        userId = UUID.randomUUID().toString();
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        accountLinkingDAO.updateUser(userAccount);
        
        ExternalAccountLink accountLink = new ExternalAccountLink();
        accountLink.setUserId(userId);
        accountLink.setExternalUserId(alexaUserId);
        accountLink.setExternalSystemName("ALEXA");
        accountLinkingDAO.createAccountLink(accountLink);
      }else{
        userId = alexaAccountLink.getUserId();
      }      

      sessionAttributes.put("userId", userId);
      
      CommonMetadata inputMetadata = mapper.convertValue(sessionAttributes, new TypeReference<DiceBotMetadata>(){});
      outputMetadata = mapper.convertValue(sessionAttributes, new TypeReference<DiceBotMetadata>(){});

      ///////////////////////////////////
      // Build the ServiceInput object //
      ///////////////////////////////////
      ServiceInput serviceInput = new ServiceInput();
      serviceInput.setMetadata(inputMetadata);
      Map<String, String> messageAsMap = AlexaUtils.getMessageAsMap(request.getRequest());
      serviceInput.setMessageAsMap(messageAsMap);
      
      SpeechletRequest speechletRequest = (SpeechletRequest)request.getRequest();
      String intent = AlexaUtils.getMessageSubject(speechletRequest);
      serviceInput.setSubject(intent);
      
      ////////////////////////////////////
      // Build the ServiceOutput object //
      ////////////////////////////////////
      ServiceOutput serviceOutput = new ServiceOutput();
      serviceOutput.setMetadata(outputMetadata);
      serviceOutput.setConversationEnded(false);
      ConversationHistoryUtils.registerRequestInConversationHistory(intent, messageAsMap, outputMetadata, outputMetadata.getConversationHistory());

      manager.handleRequest(serviceInput, serviceOutput);
      
      SimpleCard card;
      SsmlOutputSpeech outputSpeech;
      Reprompt reprompt = null;
      boolean shouldEndSession = false;
      
      switch(serviceInput.getSubject()){
      case "END_OF_CONVERSATION":
      case "STOP":
      case "CANCEL":
        if(serviceOutput.getVoiceOutput() == null || serviceOutput.getVoiceOutput().getSsmltext() == null){
          outputSpeech = null;
        }else{
          outputSpeech = new SsmlOutputSpeech();
          outputSpeech.setSsml("<speak>"+serviceOutput.getVoiceOutput().getSsmltext()+"</speak>");
        }
        card = null;
        shouldEndSession = true;
        break;
      default:
        if(StringUtils.isNotEmpty(serviceOutput.getVisualOutput().getTitle())&&
            StringUtils.isNotEmpty(serviceOutput.getVisualOutput().getText())){
          card = new SimpleCard();
          card.setTitle(serviceOutput.getVisualOutput().getTitle());
          card.setContent(serviceOutput.getVisualOutput().getText());
        }
        else{
          card = null;
        }
        if(serviceOutput.getDelayedVoiceOutput() !=null && StringUtils.isNotEmpty(serviceOutput.getDelayedVoiceOutput().getSsmltext())){
          reprompt = new Reprompt();
          SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
          repromptSpeech.setSsml("<speak>"+serviceOutput.getDelayedVoiceOutput().getSsmltext()+"</speak>");
          reprompt.setOutputSpeech(repromptSpeech);
        }

        outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml("<speak>"+serviceOutput.getVoiceOutput().getSsmltext()+"</speak>");
        shouldEndSession = serviceOutput.isConversationEnded();
        break;
      }
      
      return buildOutput(outputSpeech, card, reprompt, shouldEndSession, outputMetadata);
    }catch(DerpwizardException e){
      LOG.debug(e.getMessage());
      return new DerpwizardExceptionAlexaWrapper(e, "1.0",mapper.convertValue(outputMetadata, new TypeReference<Map<String,Object>>(){}));
    }catch(Throwable t){
      LOG.error(t.getMessage());
      t.printStackTrace();
      return new DerpwizardExceptionAlexaWrapper(new DerpwizardException(t.getMessage()),"1.0", mapper.convertValue(outputMetadata, new TypeReference<Map<String,Object>>(){}));
    }
  }
  
  private SpeechletResponseEnvelope buildOutput(OutputSpeech outputSpeech, Card card, Reprompt reprompt, boolean shouldEndSession, DiceBotMetadata outputMetadata){

    Map<String,Object> sessionAttributes = mapper.convertValue(outputMetadata, new TypeReference<Map<String,Object>>(){});
    SpeechletResponseEnvelope responseEnvelope = new SpeechletResponseEnvelope();
    
    SpeechletResponse speechletResponse = new SpeechletResponse();

    speechletResponse.setOutputSpeech(outputSpeech);
    speechletResponse.setCard(card);
    speechletResponse.setReprompt(reprompt);
    speechletResponse.setShouldEndSession(shouldEndSession);
    
    responseEnvelope.setResponse(speechletResponse);
    
    responseEnvelope.setSessionAttributes(sessionAttributes);

    return responseEnvelope;
  }
}
