package com.blackducksoftware.integration.hub.alert.web.controller.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.HubDataActions;
import com.blackducksoftware.integration.hub.model.HubGroup;
import com.blackducksoftware.integration.hub.model.HubProject;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.google.gson.Gson;

@Component
public class HubDataHandler extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(HubDataHandler.class);
    private final Gson gson;
    private final HubDataActions hubDataActions;

    @Autowired
    public HubDataHandler(final ObjectTransformer objectTransformer, final Gson gson, final HubDataActions hubDataActions) {
        super(objectTransformer);
        this.gson = gson;
        this.hubDataActions = hubDataActions;
    }

    public ResponseEntity<String> getHubGroups() {
        try {
            final List<HubGroup> groups = hubDataActions.getHubGroups();
            final String usersJson = gson.toJson(groups);
            return createResponse(HttpStatus.OK, usersJson);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final IntegrationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<String> getHubProjects() {
        try {
            final List<HubProject> projects = hubDataActions.getHubProjects();
            final String usersJson = gson.toJson(projects);
            return createResponse(HttpStatus.OK, usersJson);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final IntegrationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
