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
package com.blackducksoftware.integration.alert.web;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.alert.channel.email.mock.MockEmailGlobalRestModel;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGlobalConfigRestModel;
import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.alert.enumeration.StatusEnum;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.provider.hub.mock.MockGlobalHubEntity;
import com.blackducksoftware.integration.alert.provider.hub.mock.MockGlobalHubRestModel;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

public class ObjectTransformerTest {

    @Test
    public void testTransformGlobalModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final MockGlobalHubRestModel globalHubMockUtils = new MockGlobalHubRestModel();
        final MockGlobalHubEntity mockGlobalHubEntity = new MockGlobalHubEntity();
        final GlobalHubConfigRestModel restModel = globalHubMockUtils.createGlobalRestModel();
        final GlobalHubConfigEntity configEntity = mockGlobalHubEntity.createGlobalEntity();

        final GlobalHubConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, GlobalHubConfigEntity.class);
        final GlobalHubConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(configEntity, GlobalHubConfigRestModel.class);

        assertNull(transformedConfigRestModel.getHubAlwaysTrustCertificate());
        assertEquals(restModel.getHubApiKey(), transformedConfigRestModel.getHubApiKey());
        assertNull(transformedConfigRestModel.getHubProxyHost());
        assertNull(transformedConfigRestModel.getHubProxyPassword());
        assertNull(transformedConfigRestModel.getHubProxyPort());
        assertNull(transformedConfigRestModel.getHubProxyUsername());
        assertEquals(restModel.getHubTimeout(), transformedConfigRestModel.getHubTimeout());
        assertNull(transformedConfigRestModel.getHubUrl());
        assertEquals(restModel.getId(), transformedConfigRestModel.getId());

        assertEquals(configEntity.getHubApiKey(), transformedConfigEntity.getHubApiKey());
        assertEquals(configEntity.getHubTimeout(), transformedConfigEntity.getHubTimeout());
        assertEquals(configEntity.getId(), transformedConfigEntity.getId());
    }

    @Test
    public void testTransformEmailModels() throws Exception {
        final MockEmailGlobalRestModel restGlobalEmailMockUtil = new MockEmailGlobalRestModel();
        final MockEmailGlobalEntity mockEmailGlobalEntity = new MockEmailGlobalEntity();
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final EmailGlobalConfigRestModel restModel = restGlobalEmailMockUtil.createGlobalRestModel();
        final EmailGlobalConfigEntity configEntity = mockEmailGlobalEntity.createGlobalEntity();

        final EmailGlobalConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, EmailGlobalConfigEntity.class);
        final EmailGlobalConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(configEntity, EmailGlobalConfigRestModel.class);

        assertEquals(restModel, transformedConfigRestModel);
        assertEquals(configEntity, transformedConfigEntity);
    }

    @Test
    public void testTransformHipchatModels() throws Exception {
        final MockHipChatGlobalEntity mockHipChatGlobalEntity = new MockHipChatGlobalEntity();
        final MockHipChatGlobalRestModel hipChatMockUtils = new MockHipChatGlobalRestModel();
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final HipChatGlobalConfigRestModel restModel = hipChatMockUtils.createGlobalRestModel();
        final HipChatGlobalConfigEntity configEntity = mockHipChatGlobalEntity.createGlobalEntity();

        final HipChatGlobalConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, HipChatGlobalConfigEntity.class);
        final HipChatGlobalConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(configEntity, HipChatGlobalConfigRestModel.class);
        assertEquals(restModel, transformedConfigRestModel);
        assertEquals(configEntity, transformedConfigEntity);
    }

    @Test
    public void testTransformListsOfModels() throws Exception {
        final MockEmailGlobalRestModel restGlobalEmailMockUtil = new MockEmailGlobalRestModel();
        final MockEmailGlobalEntity mockEmailGlobalEntity = new MockEmailGlobalEntity();
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final EmailGlobalConfigRestModel restModel = restGlobalEmailMockUtil.createGlobalRestModel();
        final EmailGlobalConfigEntity configEntity = mockEmailGlobalEntity.createGlobalEntity();

        List<EmailGlobalConfigEntity> transformedConfigEntities = objectTransformer.configRestModelsToDatabaseEntities(Arrays.asList(restModel), EmailGlobalConfigEntity.class);
        List<EmailGlobalConfigRestModel> transformedConfigRestModels = objectTransformer.databaseEntitiesToConfigRestModels(Arrays.asList(configEntity), EmailGlobalConfigRestModel.class);
        assertNotNull(transformedConfigEntities);
        assertNotNull(transformedConfigRestModels);
        assertTrue(transformedConfigEntities.size() == 1);
        assertTrue(transformedConfigRestModels.size() == 1);
        assertEquals(restModel, transformedConfigRestModels.get(0));
        assertEquals(configEntity, transformedConfigEntities.get(0));

        transformedConfigEntities = objectTransformer.configRestModelsToDatabaseEntities(null, EmailGlobalConfigEntity.class);
        transformedConfigRestModels = objectTransformer.databaseEntitiesToConfigRestModels(null, EmailGlobalConfigRestModel.class);
    }

    @Test
    public void testTransformNullModels() throws Exception {
        final MockEmailGlobalRestModel restGlobalEmailMockUtil = new MockEmailGlobalRestModel();
        final MockEmailGlobalEntity mockEmailGlobalEntity = new MockEmailGlobalEntity();
        final ObjectTransformer objectTransformer = new ObjectTransformer();

        EmailGlobalConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(null, EmailGlobalConfigEntity.class);
        EmailGlobalConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(null, EmailGlobalConfigRestModel.class);
        assertNull(transformedConfigRestModel);
        assertNull(transformedConfigEntity);

        transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(restGlobalEmailMockUtil.createGlobalRestModel(), null);
        transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(mockEmailGlobalEntity.createGlobalEntity(), null);
        assertNull(transformedConfigRestModel);
        assertNull(transformedConfigEntity);

        transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(null, null);
        transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(null, null);
        assertNull(transformedConfigRestModel);
        assertNull(transformedConfigEntity);
    }

    @Test
    public void testStringToLong() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        assertNull(objectTransformer.stringToLong(null));
        assertNull(objectTransformer.stringToLong(""));
        assertNull(objectTransformer.stringToLong("false"));
        assertNull(objectTransformer.stringToLong("hello"));
        assertEquals(Long.valueOf(2), objectTransformer.stringToLong("2"));
        assertEquals(Long.valueOf(212), objectTransformer.stringToLong("  212"));
        assertEquals(Long.valueOf(567), objectTransformer.stringToLong("567   "));
    }

    @Test
    public void testStringToBoolean() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        assertNull(objectTransformer.stringToBoolean(null));
        assertNull(objectTransformer.stringToBoolean(""));
        assertNull(objectTransformer.stringToBoolean("hello"));
        assertTrue(objectTransformer.stringToBoolean("  true"));
        assertTrue(objectTransformer.stringToBoolean("True"));
        assertTrue(objectTransformer.stringToBoolean("TRuE"));
        assertTrue(objectTransformer.stringToBoolean("TRUE   "));

        assertFalse(objectTransformer.stringToBoolean("  false"));
        assertFalse(objectTransformer.stringToBoolean("False"));
        assertFalse(objectTransformer.stringToBoolean("FAlsE"));
        assertFalse(objectTransformer.stringToBoolean("FALSE   "));
    }

    @Test
    public void testObjectToString() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        assertNull(objectTransformer.objectToString(null));
        assertEquals("", objectTransformer.objectToString(""));
        assertEquals("false", objectTransformer.objectToString(false));
        assertEquals("hello", objectTransformer.objectToString("hello"));
        assertEquals("123", objectTransformer.objectToString(123));
        assertEquals("REAL_TIME", objectTransformer.objectToString(DigestTypeEnum.REAL_TIME));
    }

    @Test
    public void testTransformExceptions() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();

        final Map<String, String> testMap = new HashMap<>();
        final TestDatabaseEntity databaseEntity = new TestDatabaseEntity();
        databaseEntity.map = testMap;
        final TestConfigRestModel restModel = new TestConfigRestModel();

        try {
            objectTransformer.configRestModelToDatabaseEntity(restModel, TestDatabaseEntity.class);
            fail();
        } catch (final Exception e) {
            final String expectedMessage = String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", TestConfigRestModel.class.getSimpleName(),
                    TestDatabaseEntity.class.getSimpleName(), "map", String.class.getSimpleName(), Map.class.getSimpleName());
            assertEquals(expectedMessage, e.getMessage());
        }
        try {
            objectTransformer.databaseEntityToConfigRestModel(databaseEntity, TestConfigRestModel.class);
            fail();
        } catch (final Exception e) {
            final String expectedMessage = String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", TestDatabaseEntity.class.getSimpleName(),
                    TestConfigRestModel.class.getSimpleName(), "map", Map.class.getSimpleName(), String.class.getSimpleName());
            assertEquals(expectedMessage, e.getMessage());
        }

        final TestDatabaseEntityDifferentField testDatabaseEntityDifferentField = objectTransformer.configRestModelToDatabaseEntity(new TestConfigRestModel(), TestDatabaseEntityDifferentField.class);
        final TestConfigRestModelDifferentField testConfigRestModelDifferentField = objectTransformer.databaseEntityToConfigRestModel(new TestDatabaseEntity(), TestConfigRestModelDifferentField.class);
        // Should have created the objects but the field names should not match up so it should have logged an error
        assertNotNull(testDatabaseEntityDifferentField);
        assertNotNull(testConfigRestModelDifferentField);

        assertNull(testDatabaseEntityDifferentField.newMap);
        assertNull(testConfigRestModelDifferentField.newMap);

        try {
            objectTransformer.configRestModelToDatabaseEntity(restModel, TestDatabaseEntityInstantiationException.class);
            fail();
        } catch (final Exception e) {
            final String expectedMessage = String.format("Could not transform object %s to %s:", TestConfigRestModel.class.getSimpleName(), TestDatabaseEntityInstantiationException.class.getSimpleName());
            assertTrue(e.getMessage().contains(expectedMessage));
        }
        try {
            objectTransformer.databaseEntityToConfigRestModel(databaseEntity, TestConfigRestModelInstantiationException.class);
            fail();
        } catch (final Exception e) {
            final String expectedMessage = String.format("Could not transform object %s to %s:", TestDatabaseEntity.class.getSimpleName(), TestConfigRestModelInstantiationException.class.getSimpleName());
            assertTrue(e.getMessage().contains(expectedMessage));
        }
    }

    @Test
    public void specificDatabaseEntityConversionTest() throws AlertException {
        final ObjectTransformer objectTransformer = new ObjectTransformer();

        final TestSpecificDatabaseEntity entity = new TestSpecificDatabaseEntity();
        entity.date = new Date(12345l);
        entity.digestType = DigestTypeEnum.DAILY;
        entity.status = StatusEnum.PENDING;

        final TestSpecificConfigRestModel restModel = objectTransformer.convert(entity, TestSpecificConfigRestModel.class);
        assertNotNull(restModel);
        assertEquals(entity.date.toString(), restModel.date);
        assertEquals(entity.digestType.toString(), restModel.digestType);
        assertTrue(entity.status.toString().equalsIgnoreCase(restModel.status));
    }

    @Test
    public void specificRestModelConversionTest() throws AlertException {
        final ObjectTransformer objectTransformer = new ObjectTransformer();

        final TestSpecificConfigRestModel restModel = new TestSpecificConfigRestModel();
        restModel.date = new Date(12345l).toString();
        restModel.digestType = DigestTypeEnum.DAILY.name();
        restModel.status = StatusEnum.PENDING.toString();

        final TestSpecificDatabaseEntity entity = objectTransformer.convert(restModel, TestSpecificDatabaseEntity.class);
        assertNotNull(restModel);
        assertEquals(restModel.date, entity.date.toString());
        assertEquals(restModel.digestType, entity.digestType.name());
        assertTrue(restModel.status.equalsIgnoreCase(entity.status.toString()));
    }

    public static class TestSpecificDatabaseEntity extends DatabaseEntity {
        public Date date;
        public DigestTypeEnum digestType;
        public NotificationCategoryEnum notificationCategory;
        public StatusEnum status;

        public TestSpecificDatabaseEntity() {
        }
    }

    public static class TestSpecificConfigRestModel extends ConfigRestModel {
        public String date;
        public String digestType;
        public String notificationCategory;
        public String status;

        public TestSpecificConfigRestModel() {
        }
    }

    public static class TestDatabaseEntity extends DatabaseEntity {
        public Map<String, String> map;

        public TestDatabaseEntity() {
        }
    }

    public static class TestConfigRestModel extends ConfigRestModel {
        public String map;

        public TestConfigRestModel() {
        }
    }

    public static class TestDatabaseEntityDifferentField extends DatabaseEntity {
        public Map<String, String> newMap;

        public TestDatabaseEntityDifferentField() {
        }
    }

    public static class TestConfigRestModelDifferentField extends ConfigRestModel {
        public String newMap;

        public TestConfigRestModelDifferentField() {
        }
    }

    public class TestDatabaseEntityInstantiationException extends DatabaseEntity {
        public Map<String, String> map;
    }

    public class TestConfigRestModelInstantiationException extends ConfigRestModel {
        public String map;
    }

}
