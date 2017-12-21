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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.MockUtils;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.global.GlobalEmailConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalEmailConfigRestModel;

public class GlobalEmailConfigActionsTest {
    private final MockUtils mockUtils = new MockUtils();
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Test
    public void testDoesConfigExist() {
        final GlobalEmailRepository mockedEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        Mockito.when(mockedEmailRepository.exists(Mockito.anyLong())).thenReturn(true);

        final GlobalEmailConfigActions configActions = new GlobalEmailConfigActions(mockedEmailRepository, objectTransformer);
        assertTrue(configActions.doesConfigExist(1L));
        assertTrue(configActions.doesConfigExist("1"));

        Mockito.when(mockedEmailRepository.exists(Mockito.anyLong())).thenReturn(false);
        assertFalse(configActions.doesConfigExist(1L));
        assertFalse(configActions.doesConfigExist("1"));

        final String idString = null;
        final Long idLong = null;
        assertFalse(configActions.doesConfigExist(idString));
        assertFalse(configActions.doesConfigExist(idLong));
    }

    @Test
    public void testGetConfig() throws Exception {
        final GlobalEmailRepository mockedEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        Mockito.when(mockedEmailRepository.findOne(Mockito.anyLong())).thenReturn(mockUtils.createEmailConfigEntity());
        Mockito.when(mockedEmailRepository.findAll()).thenReturn(Arrays.asList(mockUtils.createEmailConfigEntity()));

        final GlobalEmailConfigRestModel maskedRestModel = mockUtils.createEmailConfigMaskedRestModel();

        final GlobalEmailConfigActions configActions = new GlobalEmailConfigActions(mockedEmailRepository, objectTransformer);
        List<GlobalEmailConfigRestModel> emailConfigsById = configActions.getConfig(1L);
        List<GlobalEmailConfigRestModel> allEmailConfigs = configActions.getConfig(null);

        assertTrue(emailConfigsById.size() == 1);
        assertTrue(allEmailConfigs.size() == 1);

        final GlobalEmailConfigRestModel emailConfigById = emailConfigsById.get(0);
        final GlobalEmailConfigRestModel emailConfig = allEmailConfigs.get(0);
        assertEquals(maskedRestModel, emailConfigById);
        assertEquals(maskedRestModel, emailConfig);

        Mockito.when(mockedEmailRepository.findOne(Mockito.anyLong())).thenReturn(null);
        Mockito.when(mockedEmailRepository.findAll()).thenReturn(null);

        emailConfigsById = configActions.getConfig(1L);
        allEmailConfigs = configActions.getConfig(null);

        assertNotNull(emailConfigsById);
        assertNotNull(allEmailConfigs);
        assertTrue(emailConfigsById.isEmpty());
        assertTrue(allEmailConfigs.isEmpty());
    }

    @Test
    public void testDeleteConfig() {
        final GlobalEmailRepository mockedEmailRepository = Mockito.mock(GlobalEmailRepository.class);

        final GlobalEmailConfigActions configActions = new GlobalEmailConfigActions(mockedEmailRepository, objectTransformer);
        configActions.deleteConfig(1L);
        verify(mockedEmailRepository, times(1)).delete(Mockito.anyLong());

        Mockito.reset(mockedEmailRepository);
        configActions.deleteConfig("1");
        verify(mockedEmailRepository, times(1)).delete(Mockito.anyLong());

        final String idString = null;
        final Long idLong = null;
        Mockito.reset(mockedEmailRepository);
        configActions.deleteConfig(idLong);
        verify(mockedEmailRepository, times(0)).delete(Mockito.anyLong());

        Mockito.reset(mockedEmailRepository);
        configActions.deleteConfig(idString);
        verify(mockedEmailRepository, times(0)).delete(Mockito.anyLong());
    }

    @Test
    public void testSaveConfig() throws Exception {
        final GlobalEmailRepository mockedEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final GlobalEmailConfigEntity expectedEmailConfigEntity = mockUtils.createEmailConfigEntity();
        Mockito.when(mockedEmailRepository.save(Mockito.any(GlobalEmailConfigEntity.class))).thenReturn(expectedEmailConfigEntity);

        GlobalEmailConfigActions configActions = new GlobalEmailConfigActions(mockedEmailRepository, objectTransformer);

        GlobalEmailConfigEntity emailConfigEntity = configActions.saveConfig(mockUtils.createEmailConfigRestModel());
        assertNotNull(emailConfigEntity);
        assertEquals(expectedEmailConfigEntity, emailConfigEntity);

        emailConfigEntity = configActions.saveConfig(null);
        assertNull(emailConfigEntity);

        Mockito.when(mockedEmailRepository.save(Mockito.any(GlobalEmailConfigEntity.class))).thenThrow(new RuntimeException("test"));
        try {
            emailConfigEntity = configActions.saveConfig(mockUtils.createEmailConfigRestModel());
            fail();
        } catch (final AlertException e) {
            assertEquals("test", e.getMessage());
        }

        final ObjectTransformer transformer = Mockito.mock(ObjectTransformer.class);
        Mockito.when(transformer.configRestModelToDatabaseEntity(Mockito.any(), Mockito.any())).thenReturn(null);
        configActions = new GlobalEmailConfigActions(mockedEmailRepository, transformer);

        emailConfigEntity = configActions.saveConfig(mockUtils.createEmailConfigRestModel());
        assertNull(emailConfigEntity);
    }

    @Test
    public void testValidateConfig() throws Exception {
        final GlobalEmailRepository mockedEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final GlobalEmailConfigActions configActions = new GlobalEmailConfigActions(mockedEmailRepository, objectTransformer);

        String response = configActions.validateConfig(mockUtils.createEmailConfigRestModel());
        assertEquals("Valid", response);

        final GlobalEmailConfigRestModel restModel = new GlobalEmailConfigRestModel("NotLong", "MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", true, "NotInteger", "NotInteger", "NotInteger", "MailSmtpFrom", "MailSmtpLocalhost",
                "NotBoolean", "NotBoolean", "MailSmtpDnsNotify", "MailSmtpDnsRet", "NotBoolean", "NotBoolean", "MailSmtpTemplateDirectory", "MailSmtpTemplateLogoImage", "MailSmtpSubjectLine");

        final Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("mailSmtpPort", "Not an Integer.");
        fieldErrors.put("mailSmtpConnectionTimeout", "Not an Integer.");
        fieldErrors.put("mailSmtpTimeout", "Not an Integer.");
        fieldErrors.put("mailSmtpEhlo", "Not an Boolean.");
        fieldErrors.put("mailSmtpAuth", "Not an Boolean.");
        fieldErrors.put("mailSmtpAllow8bitmime", "Not an Boolean.");
        fieldErrors.put("mailSmtpSendPartial", "Not an Boolean.");
        try {
            response = configActions.validateConfig(restModel);
            fail();
        } catch (final AlertFieldException e) {
            for (final Entry<String, String> entry : e.getFieldErrors().entrySet()) {
                assertTrue(fieldErrors.containsKey(entry.getKey()));
                final String expectedValue = fieldErrors.get(entry.getKey());
                assertEquals(expectedValue, entry.getValue());
            }
        }

        response = configActions.validateConfig(new GlobalEmailConfigRestModel());
        assertEquals("Valid", response);
    }

    @Test
    public void testConfigurationChangeTriggers() {
        final GlobalEmailConfigActions configActions = new GlobalEmailConfigActions(null, null);
        configActions.configurationChangeTriggers(null);
    }

    @Test
    public void testIsBoolean() {
        final GlobalEmailConfigActions configActions = new GlobalEmailConfigActions(null, null);
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

}
