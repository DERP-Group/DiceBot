document.addEventListener('DOMContentLoaded', function() {
    init();
}, false);

var mappingTokenResponse; //For debugging
var mappingTokenError; //For debugging
var linkSteamIdResponse; //For debugging
var linkSteamIdError; //For debugging
var token;
var state;
var accessToken;
var transparentRegistrationRequest;
var mappingTokenRequest;
var twitchRedirectUrl = "https://api.twitch.tv/kraken/oauth2/authorize?response_type=code&client_id=31yn7mmsohzw3pyrvere5biycw4wbv9&redirect_uri=http%3A%2F%2Ftwitch.oauth.derpgroup.com%3A9080%2Flivefinder%2Fauth%2Ftwitch&scope=user_read&state=";
var userId;

function init(){
	//Grab token from query params
	token = window.uQuery('sessionToken');
	state = window.uQuery('state');
	
	if(token){
		var url = '/livefinder/auth/mappingToken?token=' + token;
		mappingTokenRequest = reqwest({
		    url: url
		    , method: 'get'
		    , type: 'json'
		    , contentType: 'application/json'
		}).then(redeemMappingTokenSuccess, redeemMappingTokenFailure);
	}else if(state){
		transparentRegistration();
	}

	qwery('#accountLinkingForm')[0].onchange = hideNotificationDiv;
}

function redeemMappingTokenSuccess(response){
	mappingTokenResponse = response;
	setAccessToken(mappingTokenRequest.request.getResponseHeader('Access-Token'));
	populateForm(response);
	userId = response.userId;
} 

function redeemMappingTokenFailure(error, message){
	mappingTokenError = error;
	qwery('#steamSubmitButton')[0].disabled = true;
	alert(error.responseText);
}

function populateForm(userAccount){
	var externalAccountLinks = userAccount.externalAccountLinks;
	if(externalAccountLinks && externalAccountLinks.STEAM && externalAccountLinks.STEAM.externalUserId){
		qwery('#steamExternalId')[0].value = userAccount.externalAccountLinks.STEAM.externalUserId;
	}
	var firstName = userAccount.firstName;
	if(firstName){
		qwery('#firstName')[0].value = firstName;
	}
}

function linkSteamId(){
	var steamId = qwery('#steamExternalId')[0].value;
	if(steamId){
		var url = '/livefinder/auth/steam/linkIds?accessToken=' + accessToken + '&externalId=' + steamId;
		reqwest({
		    url: url
		    , method: 'get'
		    , type: 'json'
		    , contentType: 'application/json'
		}).then(linkSteamIdSuccess, linkSteamIdFailure)
		.always(displayNotificationDiv);
	}else{
		alert('A valid steamId must be provided.');
	}
}

function updateUser(){
	var firstName = qwery('#firstName')[0].value;
	var steamId = qwery('#steamExternalId')[0].value;
	var requestBody = {'firstName':firstName};
	var externalAccountLinks = {}
	if(steamId){
		externalAccountLinks.STEAM = {'userId':userId,'externalUserId':steamId,'externalSystemName':'STEAM'}
		requestBody.externalAccountLinks = externalAccountLinks;
	} 
	
	var requestHeaders = {};
	requestHeaders.Authorization = accessToken;
	
	var url = '/livefinder/auth/user';
	reqwest({
	    url: url
	    , method: 'put'
	    , type: 'json'
	    , data: JSON.stringify(requestBody)
	    , headers: requestHeaders
	    , contentType: 'application/json'
	}).then(linkSteamIdSuccess, linkSteamIdFailure)
	.always(displayNotificationDiv);
}

function linkSteamIdSuccess(response){
	linkSteamIdResponse = response;
	qwery('#notificationDiv')[0].textContent = "Successfully updated user info.";
} 

function linkSteamIdFailure(error, message){
	linkSteamIdError = error;
	qwery('#notificationDiv')[0].textContent = "Failed to update user info: " + error;
}

function displayNotificationDiv(){
	qwery('#notificationDiv')[0].style.display = 'block';
}

function hideNotificationDiv(){
	qwery('#notificationDiv')[0].style.display = 'none';
}

function transparentRegistration(){

	var url = '/livefinder/auth/alexa';
	transparentRegistrationRequest = reqwest({
	    url: url
	    , method: 'get'
	    , type: 'json'
	    , contentType: 'application/json'
	}).then(transparentRegistrationSuccess, transparentRegistrationFailure);
}

function transparentRegistrationSuccess(response){
	populateForm(response);
	userId = response.userId;
	qwery('#registrationSubmitButton')[0].style.display = 'block';
	setAccessToken(transparentRegistrationRequest.request.getResponseHeader('Access-Token'));
} 

function transparentRegistrationFailure(error, message){
	qwery('#notificationDiv')[0].textContent = "Failed to create user account for Alexa session: " + error;
	displayNotificationDiv();
	qwery('#steamSubmitButton')[0].disabled = true;
	qwery('#registrationSubmitButton')[0].disabled = true;
}

function submitRegistration(){
	var uri = "https://pitangui.amazon.com/spa/skill/account-linking-status.html";
	var fragment = '#'; 
	fragment += 'state=' + state;
	fragment += '&access_token=' + accessToken;
	fragment += '&token_type=Bearer';
	
	var url = uri + fragment;
	console.log('url: ' + url);
	qwery('#accountLinkingForm')[0].action = url;
	qwery('#accountLinkingForm')[0].submit();
}

function setAccessToken(inputToken){
	accessToken = inputToken;
	qwery('#twitchAuthLink')[0].href = twitchRedirectUrl + inputToken;
}