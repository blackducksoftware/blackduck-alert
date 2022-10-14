package com.synopsys.integration.alert.channel.azure.boards.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.channel.azure.boards.action.AzureBoardsOAuthCallbackAction;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;

@RestController
@RequestMapping(AlertRestConstants.AZURE_BOARDS_OAUTH_CALLBACK_PATH)
public class AzureOAuthCallbackController {
    public static final String AZURE = "azure";
    private final AzureBoardsOAuthCallbackAction azureBoardsOAuthCallbackAction;

    @Autowired
    public AzureOAuthCallbackController(AzureBoardsOAuthCallbackAction azureBoardsOAuthCallbackAction) {
        this.azureBoardsOAuthCallbackAction = azureBoardsOAuthCallbackAction;
    }

    @GetMapping
    public ResponseEntity<String> oauthCallback(HttpServletRequest request) {
        return ResponseFactory.createFoundRedirectResponseV2(azureBoardsOAuthCallbackAction.handleCallback(request));
    }
}
