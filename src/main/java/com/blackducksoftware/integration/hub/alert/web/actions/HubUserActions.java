package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

@Component
public class HubUserActions {
    private final Logger logger = LoggerFactory.getLogger(GlobalConfigActions.class);
    private final GlobalProperties globalProperties;

    @Autowired
    public HubUserActions(final GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    public String getHubUsers() throws IntegrationException {
        final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(logger);
        if (hubServicesFactory != null) {
            final UserRequestService userRequestService = hubServicesFactory.createUserRequestService();
            final List<UserView> users = userRequestService.getAllUsers();
        }
        return null;
    }

}
