package com.synopsys.integration.alert.web.channel.handler;

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

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.mock.model.MockCommonDistributionRestModel;
import com.synopsys.integration.alert.web.channel.actions.ChannelDistributionConfigActions;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class ChannelConfigHandlerTest {
    private final MockCommonDistributionRestModel mockCommonDistributionRestModel = new MockCommonDistributionRestModel();
    private final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());

    @Test
    public void getConfigTest() throws AlertException {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        final List<CommonDistributionConfig> restModel = Arrays.asList(mockCommonDistributionRestModel.createEmptyRestModel());
        Mockito.doReturn(restModel).when(configActions).getConfig(Mockito.anyLong(), Mockito.any());

        final List<? extends Config> list = handler.getConfig(1L, descriptor);
        assertEquals(restModel, list);
    }

    @Test
    public void getConfigHandleExceptionTest() throws AlertException {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(configActions.getConfig(Mockito.anyLong(), Mockito.any())).thenThrow(new AlertException());

        Exception thrownException = null;
        List<? extends Config> list = null;
        try {
            list = handler.getConfig(1L, descriptor);
        } catch (final Exception e) {
            thrownException = e;
        }
        assertNull(thrownException);
        assertEquals(Collections.emptyList(), list);
    }

    @Test
    public void postConfigTest() throws AlertException {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(configActions.doesConfigExist(Mockito.anyString(), Mockito.any())).thenReturn(false);
        Mockito.when(configActions.saveConfig(Mockito.any(), Mockito.any())).thenReturn(new CommonDistributionConfigEntity());

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.postConfig(restModel, descriptor);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void postNullConfigTest() {
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, null);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(descriptor.getName()).thenReturn("TestDescriptor");
        final ResponseEntity<String> response = handler.postConfig(null, descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postConfigWithConflictTest() {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(configActions.doesConfigExist(Mockito.anyString(), Mockito.any())).thenReturn(true);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.postConfig(restModel, descriptor);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void postWithInvalidConfigTest() throws AlertFieldException {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(configActions.doesConfigExist(Mockito.anyString(), Mockito.any())).thenReturn(false);
        Mockito.when(configActions.validateConfig(Mockito.any(), Mockito.any())).thenThrow(new AlertFieldException(Collections.emptyMap()));
        Mockito.when(configActions.getContentConverter()).thenReturn(contentConverter);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.postConfig(restModel, descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postWithInternalServerErrorTest() throws IntegrationException {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(configActions.doesConfigExist(Mockito.anyString(), Mockito.any())).thenReturn(false);
        Mockito.when(configActions.saveConfig(Mockito.any(), Mockito.any())).thenThrow(new AlertException());

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.postConfig(restModel, descriptor);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void putConfigTest() throws IntegrationException {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(configActions.doesConfigExist(Mockito.anyString(), Mockito.any())).thenReturn(true);
        Mockito.when(configActions.validateConfig(Mockito.any(), Mockito.any())).thenReturn("");
        Mockito.when(configActions.saveNewConfigUpdateFromSavedConfig(Mockito.any(), Mockito.any())).thenReturn(new CommonDistributionConfigEntity());

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.putConfig(restModel, descriptor);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    public void putNullConfigTest() {
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, null);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(descriptor.getName()).thenReturn("TestDescriptor");
        final ResponseEntity<String> response = handler.putConfig(null, descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void putConfigWithInvalidIdTest() {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(configActions.doesConfigExist(Mockito.anyString(), Mockito.any())).thenReturn(false);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.putConfig(restModel, descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void putWithInvalidConfigTest() throws AlertFieldException {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString(), Mockito.any())).thenReturn(true);
        Mockito.when(configActions.validateConfig(Mockito.any(), Mockito.any())).thenThrow(new AlertFieldException(Collections.emptyMap()));
        Mockito.when(configActions.getContentConverter()).thenReturn(contentConverter);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.putConfig(restModel, descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void putWithInternalServerErrorTest() throws IntegrationException {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString(), Mockito.any())).thenReturn(true);
        Mockito.when(configActions.saveNewConfigUpdateFromSavedConfig(Mockito.any(), Mockito.any())).thenThrow(new AlertException());

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.putConfig(restModel, descriptor);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void deleteConfigTest() throws AlertException {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);

        Mockito.when(configActions.doesConfigExist(Mockito.anyLong(), Mockito.any())).thenReturn(true);
        Mockito.doNothing().when(configActions).deleteConfig(Mockito.anyLong(), Mockito.any());

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.deleteConfig(contentConverter.getLongValue(restModel.getId()), descriptor);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    public void deleteNullConfigTest() {
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, null);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(descriptor.getName()).thenReturn("TestDescriptor");
        final ResponseEntity<String> response = handler.deleteConfig(null, descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void deleteConfigWithInvalidIdTest() {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);

        Mockito.when(configActions.doesConfigExist(Mockito.anyString(), Mockito.any())).thenReturn(false);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.deleteConfig(contentConverter.getLongValue(restModel.getId()), descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void validateConfigTest() {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.validateConfig(restModel, descriptor);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void validateConfigNullTest() {
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, null);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(descriptor.getName()).thenReturn("TestDescriptor");
        final ResponseEntity<String> response = handler.validateConfig(null, descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void validateConfigWithInvalidConfigTest() throws AlertFieldException {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);

        Mockito.when(configActions.validateConfig(Mockito.any(), Mockito.any())).thenThrow(new AlertFieldException(Collections.emptyMap()));
        Mockito.when(configActions.getContentConverter()).thenReturn(contentConverter);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.validateConfig(restModel, descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testConfigTest() {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.testConfig(restModel, descriptor);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testNullConfigTest() {
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, null);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        Mockito.when(descriptor.getName()).thenReturn("TestDescriptor");
        final ResponseEntity<String> response = handler.testConfig(null, descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testConfigWithRestExceptionTest() throws Exception {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);
        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);
        final int responseCode = HttpStatus.BAD_GATEWAY.value();
        Mockito.when(configActions.testConfig(Mockito.any(), Mockito.any())).thenThrow(new IntegrationRestException(responseCode, "", ""));
        Mockito.when(configActions.getContentConverter()).thenReturn(contentConverter);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.testConfig(restModel, descriptor);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    }

    @Test
    public void testConfigWithAlertFieldExceptionTest() throws Exception {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);

        Mockito.when(configActions.testConfig(Mockito.any(), Mockito.any())).thenThrow(new AlertFieldException(Collections.emptyMap()));
        Mockito.when(configActions.getContentConverter()).thenReturn(contentConverter);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.testConfig(restModel, descriptor);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testConfigWithExceptionTest() throws Exception {
        final ChannelDistributionConfigActions configActions = Mockito.mock(ChannelDistributionConfigActions.class);
        final ChannelConfigHandler<CommonDistributionConfig> handler = new ChannelConfigHandler<>(contentConverter, configActions);

        final ChannelDescriptor descriptor = Mockito.mock(ChannelDescriptor.class);

        Mockito.when(configActions.testConfig(Mockito.any(), Mockito.any())).thenThrow(new NullPointerException());
        Mockito.when(configActions.getContentConverter()).thenReturn(contentConverter);

        final CommonDistributionConfig restModel = mockCommonDistributionRestModel.createRestModel();
        final ResponseEntity<String> response = handler.testConfig(restModel, descriptor);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
