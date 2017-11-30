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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.MockUtils;
import com.blackducksoftware.integration.hub.alert.ResourceLoader;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HubUsersEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserFrequenciesRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.manager.HubUserManager;
import com.blackducksoftware.integration.hub.alert.datasource.relation.manager.HubUserSynchronizationManager;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserFrequenciesRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserProjectVersionsRepository;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.dataservice.user.UserDataService;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class HubUserSynchronizationManagerTestIT {
    @Autowired
    private GlobalProperties globalProperties;
    @Autowired
    private HubUsersRepository hubUsersRepository;
    @Autowired
    private HubUserFrequenciesRepository hubUserFrequenciesRepository;
    @Autowired
    private HubUserProjectVersionsRepository hubUserProjectVersionsRepository;
    @Autowired
    private HubUserManager hubUserManager;

    private MockUtils mockUtils;
    private HubUserSynchronizationManager hubUserSynchronizationManager;
    private Properties properties;

    @Before
    public void init() throws IOException {
        mockUtils = new MockUtils();
        final ResourceLoader resourceLoader = new ResourceLoader();
        properties = resourceLoader.loadProperties(ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION);

        hubUserSynchronizationManager = new HubUserSynchronizationManager(globalProperties, hubUsersRepository, hubUserProjectVersionsRepository, hubUserManager, null);
    }

    @After
    public void cleanup() {
        for (int i = 0; i < 25; i++) {
            try {
                hubUserManager.deleteConfig(new Long(i));
            } catch (final DataAccessException e) {
            }
        }
        hubUserProjectVersionsRepository.deleteAll();
        hubUsersRepository.deleteAll();
    }

    @Test
    public void syncWithActiveUserTestIT() throws IntegrationException {
        final UserView userView = new UserView();
        userView.userName = properties.getProperty("blackduck.hub.active.user");
        userView.active = Boolean.TRUE;

        final ProjectView projectView = mockUtils.createProjectView();

        final UserDataService userDataService = Mockito.mock(UserDataService.class);
        Mockito.when(userDataService.getProjectsForUser(userView.userName)).thenReturn(Arrays.asList(projectView));

        final ProjectVersionRequestService projectVersionRequestService = Mockito.mock(ProjectVersionRequestService.class);
        Mockito.when(projectVersionRequestService.getAllProjectVersions(projectView)).thenReturn(Arrays.asList(mockUtils.createProjectVersionView()));

        hubUserSynchronizationManager.synchronizeUsersWithHubServer(Arrays.asList(userView), createHubUsersMap(hubUsersRepository.findAll()), userDataService, projectVersionRequestService);

        assertEquals(1, hubUsersRepository.count());
        assertEquals(1, hubUserProjectVersionsRepository.count());
    }

    @Test
    public void syncWithStaleProjectVersionTestIT() throws IntegrationException {
        final UserView userView = new UserView();
        userView.userName = properties.getProperty("blackduck.hub.active.user");
        userView.active = Boolean.TRUE;

        final ProjectView activeProject = mockUtils.createProjectView("Active Project");

        final HubUsersEntity staleUser = hubUsersRepository.save(new HubUsersEntity("Stale User", Boolean.TRUE));
        hubUserFrequenciesRepository.save(new HubUserFrequenciesRelation(staleUser.getId(), "EXAMPLE"));
        hubUserProjectVersionsRepository.save(new HubUserProjectVersionsRelation(staleUser.getId(), "Stale Project", "Stale Version", Boolean.TRUE));
        assertEquals(1, hubUsersRepository.count());
        assertEquals(1, hubUsersRepository.count());

        final Map<String, HubUsersEntity> localUsernameMap = createHubUsersMap(hubUsersRepository.findAll());
        localUsernameMap.put(staleUser.getUsername(), staleUser);

        final UserDataService userDataService = Mockito.mock(UserDataService.class);
        Mockito.when(userDataService.getProjectsForUser(userView.userName)).thenReturn(Arrays.asList(activeProject));

        final ProjectVersionRequestService projectVersionRequestService = Mockito.mock(ProjectVersionRequestService.class);
        Mockito.when(projectVersionRequestService.getAllProjectVersions(activeProject)).thenReturn(Arrays.asList(mockUtils.createProjectVersionView()));

        hubUserSynchronizationManager.synchronizeUsersWithHubServer(Arrays.asList(), localUsernameMap, userDataService, projectVersionRequestService);

        assertEquals(0, hubUsersRepository.count());
        assertEquals(0, hubUserProjectVersionsRepository.count());
    }

    @Test
    public void syncWithInactiveUserTestIT() throws IntegrationException {
        final UserView userView = new UserView();
        userView.userName = properties.getProperty("blackduck.hub.active.user");
        userView.active = Boolean.FALSE;

        final ProjectView projectView = mockUtils.createProjectView();

        final UserDataService userDataService = Mockito.mock(UserDataService.class);
        Mockito.when(userDataService.getProjectsForUser(userView.userName)).thenReturn(Arrays.asList(projectView));

        final ProjectVersionRequestService projectVersionRequestService = Mockito.mock(ProjectVersionRequestService.class);
        Mockito.when(projectVersionRequestService.getAllProjectVersions(projectView)).thenReturn(Arrays.asList(mockUtils.createProjectVersionView()));

        hubUserSynchronizationManager.synchronizeUsersWithHubServer(Arrays.asList(userView), new HashMap<String, HubUsersEntity>(), userDataService, projectVersionRequestService);

        assertEquals(1, hubUsersRepository.count());
        assertEquals(Boolean.FALSE, hubUsersRepository.findAll().get(0).getActive());
        assertEquals(0, hubUserProjectVersionsRepository.count());
    }

    @Test
    public void syncWithNullInactiveUserTestIT() throws IntegrationException {
        final UserView userView = new UserView();
        userView.userName = properties.getProperty("blackduck.hub.active.user");
        userView.active = null;

        final ProjectView projectView = mockUtils.createProjectView();

        final UserDataService userDataService = Mockito.mock(UserDataService.class);
        Mockito.when(userDataService.getProjectsForUser(userView.userName)).thenReturn(Arrays.asList(projectView));

        final ProjectVersionRequestService projectVersionRequestService = Mockito.mock(ProjectVersionRequestService.class);
        Mockito.when(projectVersionRequestService.getAllProjectVersions(projectView)).thenReturn(Arrays.asList(mockUtils.createProjectVersionView()));

        hubUserSynchronizationManager.synchronizeUsersWithHubServer(Arrays.asList(userView), new HashMap<String, HubUsersEntity>(), userDataService, projectVersionRequestService);

        assertEquals(1, hubUsersRepository.count());
        assertEquals(Boolean.FALSE, hubUsersRepository.findAll().get(0).getActive());
        assertEquals(0, hubUserProjectVersionsRepository.count());
    }

    private Map<String, HubUsersEntity> createHubUsersMap(final List<HubUsersEntity> localHubUsersList) {
        final Map<String, HubUsersEntity> map = new HashMap<>();
        if (localHubUsersList != null) {
            for (final HubUsersEntity entity : localHubUsersList) {
                map.put(entity.getUsername(), entity);
            }
        }
        return map;
    }

}
