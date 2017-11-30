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
package com.blackducksoftware.integration.hub.datasource.relation.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HubUsersEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserEmailRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserFrequenciesRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserHipChatRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelationPK;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserSlackRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.manager.HubUserManager;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserEmailRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserFrequenciesRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserHipChatRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserProjectVersionsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserSlackRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.HubUsersConfigWrapper;
import com.blackducksoftware.integration.hub.alert.web.model.ProjectVersionConfigWrapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
public class HubUserManagerTestIT {
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Autowired
    private HubUsersRepository hubUsersRepository;
    @Autowired
    private HubUserFrequenciesRepository hubUserFrequenciesRepository;
    @Autowired
    private HubUserEmailRepository hubUserEmailRepository;
    @Autowired
    private HubUserHipChatRepository hubUserHipChatRepository;
    @Autowired
    private HubUserSlackRepository hubUserSlackRepository;
    @Autowired
    private HubUserProjectVersionsRepository hubUserProjectVersionsRepository;

    @After
    public void cleanup() {
        hubUserProjectVersionsRepository.deleteAll();
        hubUserSlackRepository.deleteAll();
        hubUserHipChatRepository.deleteAll();
        hubUserEmailRepository.deleteAll();
        hubUserFrequenciesRepository.deleteAll();
        hubUsersRepository.deleteAll();
    }

    @Test
    public void saveConfigTestIT() throws AlertException {
        final HubUserManager manager = new HubUserManager(hubUsersRepository, hubUserFrequenciesRepository, hubUserEmailRepository, hubUserHipChatRepository, hubUserSlackRepository, hubUserProjectVersionsRepository, objectTransformer);

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
        final Long savedId = manager.saveConfig(configWrapper);

        final HubUsersEntity hubUsersEntity = hubUsersRepository.findOne(savedId);
        assertEquals(savedId, hubUsersEntity.getId());
        assertEquals(username, hubUsersEntity.getUsername());

        final HubUserFrequenciesRelation hubUserFrequenciesRelation = hubUserFrequenciesRepository.findOne(savedId);
        assertEquals(frequency, hubUserFrequenciesRelation.getFrequency());

        final HubUserEmailRelation hubUserEmailRelation = hubUserEmailRepository.findOne(savedId);
        assertEquals(objectTransformer.stringToLong(emailConfigId), hubUserEmailRelation.getChannelConfigId());

        final HubUserHipChatRelation hubUserHipChatRelation = hubUserHipChatRepository.findOne(savedId);
        assertEquals(objectTransformer.stringToLong(hipChatConfigId), hubUserHipChatRelation.getChannelConfigId());

        final HubUserSlackRelation hubUserSlackRelation = hubUserSlackRepository.findOne(savedId);
        assertEquals(objectTransformer.stringToLong(slackConfigId), hubUserSlackRelation.getChannelConfigId());

        final List<HubUserProjectVersionsRelation> hubUserProjectVersionsRelation = hubUserProjectVersionsRepository.findByUserConfigId(savedId);
        assertEquals(3, hubUserProjectVersionsRelation.size());
        for (int i = 0; i < projectVersions.size(); i++) {
            assertEquals(projectVersions.get(i).getProjectName(), hubUserProjectVersionsRelation.get(i).getProjectName());
            assertEquals(projectVersions.get(i).getProjectVersionName(), hubUserProjectVersionsRelation.get(i).getProjectVersionName());
            assertEquals(projectVersions.get(i).getEnabled(), hubUserProjectVersionsRelation.get(i).getEnabled().toString());
        }
    }

    @Test
    public void simpleDeleteTestIT() throws AlertException {
        final HubUserManager manager = new HubUserManager(hubUsersRepository, hubUserFrequenciesRepository, hubUserEmailRepository, hubUserHipChatRepository, hubUserSlackRepository, hubUserProjectVersionsRepository, objectTransformer);
        final HubUsersConfigWrapper newWrapper = new HubUsersConfigWrapper(null, "", "", null, null, null, null, null);
        final Long savedId = manager.saveConfig(newWrapper);
        assertTrue(hubUsersRepository.exists(savedId));
        manager.deleteConfig(savedId);
        assertTrue(!hubUsersRepository.exists(savedId));
    }

    @Test
    public void deleteTestIT() throws AlertException {
        final HubUserManager manager = new HubUserManager(hubUsersRepository, hubUserFrequenciesRepository, hubUserEmailRepository, hubUserHipChatRepository, hubUserSlackRepository, hubUserProjectVersionsRepository, objectTransformer);

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
        final Long savedId = manager.saveConfig(configWrapper);

        final HubUserProjectVersionsRelationPK key1 = new HubUserProjectVersionsRelationPK();
        key1.userConfigId = savedId;
        key1.projectName = "Project 1";
        key1.projectVersionName = "Version 1";
        final HubUserProjectVersionsRelationPK key2 = new HubUserProjectVersionsRelationPK();
        key2.userConfigId = savedId;
        key2.projectName = "Project 1";
        key2.projectVersionName = "Version 2";
        final HubUserProjectVersionsRelationPK key3 = new HubUserProjectVersionsRelationPK();
        key3.userConfigId = savedId;
        key3.projectName = "Project 2";
        key3.projectVersionName = "Version 1";

        assertTrue(hubUsersRepository.exists(savedId));
        assertTrue(hubUserFrequenciesRepository.exists(savedId));
        assertTrue(hubUserEmailRepository.exists(savedId));
        assertTrue(hubUserHipChatRepository.exists(savedId));

        assertTrue(hubUserProjectVersionsRepository.exists(key1));
        assertTrue(hubUserProjectVersionsRepository.exists(key2));
        assertTrue(hubUserProjectVersionsRepository.exists(key3));

        manager.deleteConfig(savedId);
        assertTrue(!hubUsersRepository.exists(savedId));
        assertTrue(!hubUserFrequenciesRepository.exists(savedId));
        assertTrue(!hubUserEmailRepository.exists(savedId));
        assertTrue(!hubUserHipChatRepository.exists(savedId));

        assertTrue(!hubUserProjectVersionsRepository.exists(key1));
        assertTrue(!hubUserProjectVersionsRepository.exists(key2));
        assertTrue(!hubUserProjectVersionsRepository.exists(key3));
    }

    @Test
    public void getProjectVersionsTestIT() throws AlertException {
        final HubUserManager manager = new HubUserManager(hubUsersRepository, hubUserFrequenciesRepository, hubUserEmailRepository, hubUserHipChatRepository, hubUserSlackRepository, hubUserProjectVersionsRepository, objectTransformer);

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
        final Long savedId = manager.saveConfig(configWrapper);

        final List<ProjectVersionConfigWrapper> projectVersionWrapperList = manager.getProjectVersions(savedId);
        for (int i = 0; i < projectVersions.size(); i++) {
            assertEquals(projectVersions.get(i).getProjectName(), projectVersionWrapperList.get(i).getProjectName());
            assertEquals(projectVersions.get(i).getProjectVersionName(), projectVersionWrapperList.get(i).getProjectVersionName());
            assertEquals(projectVersions.get(i).getEnabled(), projectVersionWrapperList.get(i).getEnabled());
        }
    }

}
