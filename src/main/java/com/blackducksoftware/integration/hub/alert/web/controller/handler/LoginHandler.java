/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.web.controller.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.LoginActions;
import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.ResponseBodyBuilder;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

public class LoginHandler extends ControllerHandler {
    private final LoginActions loginActions;

    @Autowired
    public LoginHandler(final ObjectTransformer objectTransformer, final LoginActions loginActions) {
        super(objectTransformer);
        this.loginActions = loginActions;
    }

    public ResponseEntity<String> userLogout(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return new ResponseEntity<>("{\"message\":\"Success\"}", HttpStatus.ACCEPTED);
    }

    public ResponseEntity<String> userLogin(final HttpServletRequest request, final LoginRestModel loginRestModel) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            // TODO figure out timeout
            session.setMaxInactiveInterval(300);
        }

        return authenticateUser(loginRestModel);
    }

    public ResponseEntity<String> authenticateUser(final LoginRestModel loginRestModel) {
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);

        try {
            if (loginActions.authenticateUser(loginRestModel, logger)) {
                return createResponse(HttpStatus.ACCEPTED, "{\"message\":\"Success\"}");
            }
            return createResponse(HttpStatus.UNAUTHORIZED, "User not administrator");
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final AlertFieldException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(0L, e.getMessage());
            responseBodyBuilder.putErrors(e.getFieldErrors());
            final String responseBody = responseBodyBuilder.build();
            return createResponse(HttpStatus.BAD_REQUEST, responseBody);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
