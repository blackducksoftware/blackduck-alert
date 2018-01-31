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
package com.blackducksoftware.integration.hub.alert.web.actions.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.datasource.SimpleKeyRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.mock.NotificationTypeMockUtils;
import com.blackducksoftware.integration.hub.alert.mock.ProjectMockUtils;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockCommonDistributionEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public abstract class ActionsTest<R extends CommonDistributionConfigRestModel, E extends DistributionChannelConfigEntity, W extends SimpleKeyRepositoryWrapper<E, ?>, DCA extends DistributionConfigActions<E, R, W>> {
    private final ProjectMockUtils projectMockUtils;
    private final NotificationTypeMockUtils notificationMockUtil;
    private final MockCommonDistributionEntity distributionMockUtils;
    private DCA configActions;

    public ActionsTest() {
        configActions = getMockedConfigActions();
        projectMockUtils = new ProjectMockUtils();
        notificationMockUtil = new NotificationTypeMockUtils();
        distributionMockUtils = new MockCommonDistributionEntity();
    }

    public abstract MockEntityUtil<E> getEntityMockUtil();

    public abstract MockRestModelUtil<R> getRestMockUtil();

    public abstract DCA getMockedConfigActions();

    @Test
    public void testDoesConfigExist() {
        Mockito.when(configActions.getCommonDistributionRepository().exists(Mockito.anyLong())).thenReturn(true);
        assertTrue(configActions.doesConfigExist(1L));
        assertTrue(configActions.doesConfigExist("1"));

        Mockito.when(configActions.getCommonDistributionRepository().exists(Mockito.anyLong())).thenReturn(false);
        assertFalse(configActions.doesConfigExist(1L));
        assertFalse(configActions.doesConfigExist("1"));

        final String idString = null;
        final Long idLong = null;
        assertFalse(configActions.doesConfigExist(idString));
        assertFalse(configActions.doesConfigExist(idLong));
    }

    @Test
    public void testGetConfig() throws AlertException {
        Mockito.when(configActions.getCommonDistributionRepository().findByDistributionConfigIdAndDistributionType(Mockito.any(), Mockito.any())).thenReturn(distributionMockUtils.createEntity());
        Mockito.when(configActions.getRepository().findOne(Mockito.anyLong())).thenReturn(getEntityMockUtil().createEntity());
        Mockito.when(configActions.getRepository().findAll()).thenReturn(Arrays.asList(getEntityMockUtil().createEntity()));
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findOne(1L)).thenReturn(projectMockUtils.getProjectOneEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findOne(2L)).thenReturn(projectMockUtils.getProjectTwoEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findOne(3L)).thenReturn(projectMockUtils.getProjectThreeEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findOne(4L)).thenReturn(projectMockUtils.getProjectFourEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getDistributionProjectRepository().findByCommonDistributionConfigId(Mockito.anyLong())).thenReturn(projectMockUtils.getProjectRelations());
        Mockito.when(configActions.getNotificationTypesActions().getNotificationTypeRepository().findOne(1L)).thenReturn(notificationMockUtil.createEntity());
        Mockito.when(configActions.getNotificationTypesActions().getDistributionNotificationTypeRepository().findByCommonDistributionConfigId(Mockito.anyLong())).thenReturn(notificationMockUtil.getNotificationTypeRelations());

        // We must mask the rest model because the configActions will have masked those returned by getConfig(...)
        final R restModel = getRestMockUtil().createRestModel();
        configActions.maskRestModel(restModel);

        List<R> configsById = configActions.getConfig(1L);
        List<R> allConfigs = configActions.getConfig(null);

        assertTrue(configsById.size() == 1);
        assertTrue(allConfigs.size() == 1);

        final R configById = configsById.get(0);
        final R config = allConfigs.get(0);

        assertEquals(restModel, configById);
        assertEquals(restModel, config);

        Mockito.when(configActions.getRepository().findOne(Mockito.anyLong())).thenReturn(null);
        Mockito.when(configActions.getRepository().findAll()).thenReturn(Arrays.asList());

        configsById = configActions.getConfig(1L);
        allConfigs = configActions.getConfig(null);

        assertNotNull(configsById);
        assertNotNull(allConfigs);
        assertTrue(configsById.isEmpty());
        assertTrue(allConfigs.isEmpty());
    }

    @Test
    public void testDeleteConfig() {
        Mockito.when(configActions.getCommonDistributionRepository().findOne(Mockito.anyLong())).thenReturn(distributionMockUtils.createEntity());
        configActions.deleteConfig(1L);
        verify(configActions.getRepository(), times(1)).delete(Mockito.anyLong());

        Mockito.reset(configActions.getRepository());

        configActions.deleteConfig("1");

        verify(configActions.getRepository(), times(1)).delete(Mockito.anyLong());

        final String idString = null;
        final Long idLong = null;

        Mockito.reset(configActions.getRepository());
        configActions.deleteConfig(idLong);

        verify(configActions.getRepository(), times(0)).delete(Mockito.anyLong());

        Mockito.reset(configActions.getRepository());
        configActions.deleteConfig(idString);

        verify(configActions.getRepository(), times(0)).delete(Mockito.anyLong());
    }

    @Test
    public void testSaveConfig() throws Exception {
        final E expectedConfigEntity = getEntityMockUtil().createEntity();
        Mockito.when(configActions.getRepository().save(Mockito.any(getConfigEntityClass()))).thenReturn(expectedConfigEntity);
        Mockito.when(configActions.getCommonDistributionRepository().save(Mockito.any(CommonDistributionConfigEntity.class))).thenReturn(distributionMockUtils.createEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findByProjectName(projectMockUtils.getProjectOne())).thenReturn(projectMockUtils.getProjectOneEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findByProjectName(projectMockUtils.getProjectTwo())).thenReturn(projectMockUtils.getProjectTwoEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findByProjectName(projectMockUtils.getProjectThree())).thenReturn(projectMockUtils.getProjectThreeEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findByProjectName(projectMockUtils.getProjectFour())).thenReturn(projectMockUtils.getProjectFourEntity());
        Mockito.when(configActions.getNotificationTypesActions().getNotificationTypeRepository().findByType(notificationMockUtil.getType())).thenReturn(notificationMockUtil.createEntity());

        E actualConfigEntity = configActions.saveConfig(getRestMockUtil().createRestModel());
        assertNotNull(actualConfigEntity);
        assertEquals(expectedConfigEntity, actualConfigEntity);

        actualConfigEntity = configActions.saveConfig(null);
        assertNull(actualConfigEntity);

        Mockito.when(configActions.getRepository().save(Mockito.any(getConfigEntityClass()))).thenThrow(new RuntimeException("test"));
        try {
            actualConfigEntity = configActions.saveConfig(getRestMockUtil().createRestModel());
            fail();
        } catch (final AlertException e) {
            assertEquals("test", e.getMessage());
        }

        final ObjectTransformer transformer = Mockito.mock(ObjectTransformer.class);
        Mockito.when(transformer.configRestModelToDatabaseEntity(Mockito.any(), Mockito.any())).thenReturn(null);
        configActions = createMockedConfigActionsUsingObjectTransformer(transformer);

        actualConfigEntity = configActions.saveConfig(getRestMockUtil().createRestModel());
        assertNull(actualConfigEntity);
    }

    public abstract Class<E> getConfigEntityClass();

    public abstract DCA createMockedConfigActionsUsingObjectTransformer(ObjectTransformer objectTransformer);

    @Test
    public void testIsBoolean() {
        assertFalse(configActions.isBoolean(null));
        assertFalse(configActions.isBoolean(""));
        assertFalse(configActions.isBoolean("string"));
        assertFalse(configActions.isBoolean(" cat"));
        assertTrue(configActions.isBoolean("true"));
        assertTrue(configActions.isBoolean("false"));
        assertTrue(configActions.isBoolean("TRUE"));
        assertTrue(configActions.isBoolean("FALSE"));
        assertTrue(configActions.isBoolean("  TruE"));
        assertTrue(configActions.isBoolean("FaLSE  "));
    }

    @Test
    public void testConstructRestModel() throws AlertException {
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findOne(1L)).thenReturn(projectMockUtils.getProjectOneEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findOne(2L)).thenReturn(projectMockUtils.getProjectTwoEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findOne(3L)).thenReturn(projectMockUtils.getProjectThreeEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getConfiguredProjectsRepository().findOne(4L)).thenReturn(projectMockUtils.getProjectFourEntity());
        Mockito.when(configActions.getConfiguredProjectsActions().getDistributionProjectRepository().findByCommonDistributionConfigId(Mockito.anyLong())).thenReturn(projectMockUtils.getProjectRelations());

        final CommonDistributionConfigEntity commonEntity = distributionMockUtils.createEntity();

        final R actualRestModel = configActions.constructRestModel(commonEntity, getEntityMockUtil().createEntity());
        final R expectedRestModel = getRestMockUtil().createRestModel();
        expectedRestModel.setConfiguredProjects(null);
        expectedRestModel.setNotificationTypes(null);
        assertEquals(expectedRestModel, actualRestModel);
    }

}
