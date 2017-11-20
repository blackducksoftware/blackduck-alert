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
package com.blackducksoftware.integration.hub.alert.web.actions;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.manager.HubUserManager;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.model.HubUsersConfigWrapper;
import com.blackducksoftware.integration.hub.alert.web.model.ProjectVersionConfigWrapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
public class HubUsersConfigWrapperActionsTestIT {
    @Autowired
    private HubUsersRepository hubUsersRepository;
    @Autowired
    private HubUserManager hubUserManager;

    @After
    public void cleanup() {
        // Delete any possible configurations created during tests
        for (long i = 0; i < 5; i++) {
            hubUserManager.deleteConfig(new Long(i));
        }
    }

    @Test
    public void getConfigTestIT() throws AlertException {
        final String id = "1";
        final String username = "user";
        final String frequency = "DAILY";
        final String emailConfigId = "1";
        final String hipChatConfigId = "1";
        final String slackConfigId = "1";
        final String existsOnHub = "true";

        final List<ProjectVersionConfigWrapper> projectVersions = new ArrayList<>();
        projectVersions.add(new ProjectVersionConfigWrapper("Project 1", "Version 1", "false"));
        projectVersions.add(new ProjectVersionConfigWrapper("Project 1", "Version 2", "false"));
        projectVersions.add(new ProjectVersionConfigWrapper("Project 2", "Version 1", "false"));

        final HubUsersConfigWrapper configWrapper = new HubUsersConfigWrapper(id, username, frequency, emailConfigId, hipChatConfigId, slackConfigId, existsOnHub, projectVersions);
        final Long longId = hubUserManager.saveConfig(configWrapper);

        final HubUsersConfigWrapperActions actions = new HubUsersConfigWrapperActions(hubUsersRepository, hubUserManager);
        final List<HubUsersConfigWrapper> userConfigList = actions.getConfig(longId);
        assertEquals(1, userConfigList.size());

        // We cannot predict the id in tests
        final String jsonVersion = "{\"username\":\"" + username + "\",\"frequency\":\"" + frequency + "\",\"emailConfigId\":\"" + emailConfigId + "\",\"hipChatConfigId\":\"" + hipChatConfigId + "\",\"slackConfigId\":\"" + slackConfigId
                + "\",\"existsOnHub\":\"" + existsOnHub + "\",\"projectVersions\":" + projectVersions + ",\"id\":\"" + userConfigList.get(0).getId() + "\"}";
        assertEquals(jsonVersion, userConfigList.get(0).toString());
    }

    @Test
    public void getConfigNullIdTestIT() throws AlertException {
        final String username = "user";
        final String frequency = "DAILY";
        final String emailConfigId = "1";
        final String hipChatConfigId = "1";
        final String slackConfigId = "1";
        final String existsOnHub = "true";

        final List<ProjectVersionConfigWrapper> projectVersions = new ArrayList<>();
        projectVersions.add(new ProjectVersionConfigWrapper("Project 1", "Version 1", "false"));
        projectVersions.add(new ProjectVersionConfigWrapper("Project 1", "Version 2", "false"));
        projectVersions.add(new ProjectVersionConfigWrapper("Project 2", "Version 1", "false"));

        final HubUsersConfigWrapper configWrapper = new HubUsersConfigWrapper(null, username, frequency, emailConfigId, hipChatConfigId, slackConfigId, existsOnHub, projectVersions);
        hubUserManager.saveConfig(configWrapper);

        final HubUsersConfigWrapperActions actions = new HubUsersConfigWrapperActions(hubUsersRepository, hubUserManager);
        final List<HubUsersConfigWrapper> userConfigList = actions.getConfig(null);
        assertEquals(1, userConfigList.size());

        // We cannot predict the id in tests
        final String jsonVersion = "{\"username\":\"" + username + "\",\"frequency\":\"" + frequency + "\",\"emailConfigId\":\"" + emailConfigId + "\",\"hipChatConfigId\":\"" + hipChatConfigId + "\",\"slackConfigId\":\"" + slackConfigId
                + "\",\"existsOnHub\":\"" + existsOnHub + "\",\"projectVersions\":" + projectVersions + ",\"id\":\"" + userConfigList.get(0).getId() + "\"}";
        assertEquals(jsonVersion, userConfigList.get(0).toString());
    }
}
