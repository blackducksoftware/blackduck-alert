/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.alert.web.controller.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubRepository;
import com.blackducksoftware.integration.alert.provider.hub.descriptor.HubContentConverter;
import com.blackducksoftware.integration.alert.provider.hub.mock.MockGlobalHubRestModel;
import com.blackducksoftware.integration.alert.web.exception.AlertFieldException;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfig;
import com.blackducksoftware.integration.alert.web.provider.hub.GlobalHubConfigActions;
import com.blackducksoftware.integration.alert.web.provider.hub.GlobalHubConfig;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.google.gson.Gson;

public class CommonConfigHandlerTest {
    private final MockGlobalHubRestModel mockGlobalHubRestModel = new MockGlobalHubRestModel();
    final Gson gson = new Gson();
    final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());

    @Test
    public void getConfigTest() throws AlertException {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        final List<GlobalHubConfig> restModel = Arrays.asList(mockGlobalHubRestModel.createEmptyGlobalRestModel());
        Mockito.doReturn(restModel).when(configActions).getConfig(Mockito.anyLong());

        final List<GlobalHubConfig> list = handler.getConfig(1L);
        assertEquals(restModel, list);
    }

    @Test
    public void getConfigHandleExceptionTest() throws AlertException {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.getConfig(Mockito.anyLong())).thenThrow(new AlertException());

        Exception thrownException = null;
        List<GlobalHubConfig> list = null;
        try {
            list = handler.getConfig(1L);
        } catch (final Exception e) {
            thrownException = e;
        }
        assertNull(thrownException);
        assertEquals(Collections.emptyList(), list);
    }

    @Test
    public void postConfigTest() throws AlertException {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString())).thenReturn(false);
        Mockito.when(configActions.saveConfig(Mockito.any())).thenReturn(new GlobalHubConfigEntity());

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.postConfig(restModel);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void postNullConfigTest() {
        final CommonConfigHandler<CommonDistributionConfigEntity, CommonDistributionConfig, CommonDistributionRepository> handler = new CommonConfigHandler<>(CommonDistributionConfigEntity.class,
                CommonDistributionConfig.class, null, contentConverter);

        final ResponseEntity<String> response = handler.postConfig(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postConfigWithConflictTest() {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        Mockito.when(configActions.doesConfigExist(Mockito.anyString())).thenReturn(true);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.postConfig(restModel);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void postWithInvalidConfigTest() throws AlertFieldException {
        final HubContentConverter commonDistributionContentConverter = new HubContentConverter(contentConverter);
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString())).thenReturn(false);
        Mockito.when(configActions.validateConfig(Mockito.any())).thenThrow(new AlertFieldException(Collections.emptyMap()));
        Mockito.when(configActions.getDatabaseContentConverter()).thenReturn(commonDistributionContentConverter);

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.postConfig(restModel);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postWithInternalServerErrorTest() throws IntegrationException {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString())).thenReturn(false);
        Mockito.doNothing().when(configActions).configurationChangeTriggers(Mockito.any());
        Mockito.when(configActions.saveConfig(Mockito.any())).thenThrow(new AlertException());

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.postConfig(restModel);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void putConfigTest() throws IntegrationException {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString())).thenReturn(true);
        Mockito.when(configActions.validateConfig(Mockito.any())).thenReturn("");
        Mockito.when(configActions.saveNewConfigUpdateFromSavedConfig(Mockito.any())).thenReturn(new GlobalHubConfigEntity());

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.putConfig(restModel);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    public void putNullConfigTest() {
        final CommonConfigHandler<CommonDistributionConfigEntity, CommonDistributionConfig, CommonDistributionRepository> handler = new CommonConfigHandler<>(CommonDistributionConfigEntity.class,
                CommonDistributionConfig.class, null, contentConverter);

        final ResponseEntity<String> response = handler.putConfig(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void putConfigWithInvalidIdTest() {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString())).thenReturn(false);

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.putConfig(restModel);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void putWithInvalidConfigTest() throws AlertFieldException {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString())).thenReturn(true);
        Mockito.when(configActions.validateConfig(Mockito.any())).thenThrow(new AlertFieldException(Collections.emptyMap()));
        final HubContentConverter commonDistributionContentConverter = new HubContentConverter(contentConverter);
        Mockito.when(configActions.getDatabaseContentConverter()).thenReturn(commonDistributionContentConverter);

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.putConfig(restModel);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void putWithInternalServerErrorTest() throws IntegrationException {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(configActions).configurationChangeTriggers(Mockito.any());
        Mockito.when(configActions.saveNewConfigUpdateFromSavedConfig(Mockito.any())).thenThrow(new AlertException());

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.putConfig(restModel);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void deleteConfigTest() throws AlertException {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(configActions).deleteConfig(Mockito.anyLong());

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.deleteConfig(restModel);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    public void deleteNullConfigTest() {
        final CommonConfigHandler<CommonDistributionConfigEntity, CommonDistributionConfig, CommonDistributionRepository> handler = new CommonConfigHandler<>(CommonDistributionConfigEntity.class,
                CommonDistributionConfig.class, null, contentConverter);

        final ResponseEntity<String> response = handler.deleteConfig(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void deleteConfigWithInvalidIdTest() {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString())).thenReturn(false);

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.deleteConfig(restModel);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void validateConfigTest() {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.validateConfig(restModel);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void validateConfigNullTest() {
        final CommonConfigHandler<CommonDistributionConfigEntity, CommonDistributionConfig, CommonDistributionRepository> handler = new CommonConfigHandler<>(CommonDistributionConfigEntity.class,
                CommonDistributionConfig.class, null, contentConverter);

        final ResponseEntity<String> response = handler.validateConfig(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void validateConfigWithInvalidConfigTest() throws AlertFieldException {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> handler = new CommonConfigHandler<>(GlobalHubConfigEntity.class,
                GlobalHubConfig.class, configActions, contentConverter);

        Mockito.when(configActions.validateConfig(Mockito.any())).thenThrow(new AlertFieldException(Collections.emptyMap()));
        final HubContentConverter commonDistributionContentConverter = new HubContentConverter(contentConverter);
        Mockito.when(configActions.getDatabaseContentConverter()).thenReturn(commonDistributionContentConverter);

        final GlobalHubConfig restModel = mockGlobalHubRestModel.createGlobalRestModel();
        final ResponseEntity<String> response = handler.validateConfig(restModel);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // @Test
    // public void testConfigTest() {
    // final CommonDistributionConfigActions configActions = Mockito.mock(CommonDistributionConfigActions.class);
    // final CommonConfigHandler<CommonDistributionConfigEntity, CommonDistributionConfigRestModel, CommonDistributionRepository> handler = new CommonConfigHandler<>(CommonDistributionConfigEntity.class,
    // CommonDistributionConfigRestModel.class, configActions, objectTransformer);
    //
    // final CommonDistributionConfigRestModel restModel = mockCommonDistributionRestModel.createRestModel();
    // final ResponseEntity<String> response = handler.testConfig(restModel);
    // assertEquals(HttpStatus.OK, response.getStatusCode());
    // }

    @Test
    public void testNullConfigTest() {
        final CommonConfigHandler<CommonDistributionConfigEntity, CommonDistributionConfig, CommonDistributionRepository> handler = new CommonConfigHandler<>(CommonDistributionConfigEntity.class,
                CommonDistributionConfig.class, null, contentConverter);

        final ResponseEntity<String> response = handler.testConfig(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // @Test
    // public void testConfigWithRestExceptionTest() throws Exception {
    // final CommonDistributionConfigActions configActions = Mockito.mock(CommonDistributionConfigActions.class);
    // final CommonConfigHandler<CommonDistributionConfigEntity, CommonDistributionConfigRestModel, CommonDistributionRepository> handler = new CommonConfigHandler<>(CommonDistributionConfigEntity.class,
    // CommonDistributionConfigRestModel.class, configActions, objectTransformer);
    //
    // final int responseCode = HttpStatus.BAD_GATEWAY.value();
    // Mockito.when(configActions.testConfig(Mockito.any())).thenThrow(new IntegrationRestException(responseCode, "", ""));
    // Mockito.when(configActions.getObjectTransformer()).thenReturn(objectTransformer);
    //
    // final CommonDistributionConfigRestModel restModel = mockCommonDistributionRestModel.createRestModel();
    // final ResponseEntity<String> response = handler.testConfig(restModel);
    // assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    // }
    //
    // @Test
    // public void testConfigWithAlertFieldExceptionTest() throws Exception {
    // final CommonDistributionConfigActions configActions = Mockito.mock(CommonDistributionConfigActions.class);
    // final CommonConfigHandler<CommonDistributionConfigEntity, CommonDistributionConfigRestModel, CommonDistributionRepository> handler = new CommonConfigHandler<>(CommonDistributionConfigEntity.class,
    // CommonDistributionConfigRestModel.class, configActions, objectTransformer);
    //
    // Mockito.when(configActions.testConfig(Mockito.any())).thenThrow(new AlertFieldException(Collections.emptyMap()));
    // Mockito.when(configActions.getObjectTransformer()).thenReturn(objectTransformer);
    //
    // final CommonDistributionConfigRestModel restModel = mockCommonDistributionRestModel.createRestModel();
    // final ResponseEntity<String> response = handler.testConfig(restModel);
    // assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    // }
    //
    // @Test
    // public void testConfigWithExceptionTest() throws Exception {
    // final CommonDistributionConfigActions configActions = Mockito.mock(CommonDistributionConfigActions.class);
    // final CommonConfigHandler<CommonDistributionConfigEntity, CommonDistributionConfigRestModel, CommonDistributionRepository> handler = new CommonConfigHandler<>(CommonDistributionConfigEntity.class,
    // CommonDistributionConfigRestModel.class, configActions, objectTransformer);
    //
    // Mockito.when(configActions.testConfig(Mockito.any())).thenThrow(new NullPointerException());
    // Mockito.when(configActions.getObjectTransformer()).thenReturn(objectTransformer);
    //
    // final CommonDistributionConfigRestModel restModel = mockCommonDistributionRestModel.createRestModel();
    // final ResponseEntity<String> response = handler.testConfig(restModel);
    // assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    // }

}
