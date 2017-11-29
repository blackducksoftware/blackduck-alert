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
package com.blackducksoftware.integration.hub.alert.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.MockUtils;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;

public class ObjectTransformerTest {
    private final MockUtils mockUtils = new MockUtils();

    @Test
    public void testTransformGlobalModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalConfigRestModel restModel = mockUtils.createGlobalConfigRestModel();
        final GlobalConfigEntity configEntity = mockUtils.createGlobalConfigEntity();

        final GlobalConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, GlobalConfigEntity.class);
        final GlobalConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(configEntity, GlobalConfigRestModel.class);

        assertEquals(restModel.getAccumulatorCron(), transformedConfigRestModel.getAccumulatorCron());
        assertEquals(restModel.getDailyDigestCron(), transformedConfigRestModel.getDailyDigestCron());
        assertNull(transformedConfigRestModel.getHubAlwaysTrustCertificate());
        assertEquals(restModel.getHubPassword(), transformedConfigRestModel.getHubPassword());
        assertNull(transformedConfigRestModel.getHubProxyHost());
        assertNull(transformedConfigRestModel.getHubProxyPassword());
        assertNull(transformedConfigRestModel.getHubProxyPort());
        assertNull(transformedConfigRestModel.getHubProxyUsername());
        assertEquals(restModel.getHubTimeout(), transformedConfigRestModel.getHubTimeout());
        assertNull(transformedConfigRestModel.getHubUrl());
        assertEquals(restModel.getHubUsername(), transformedConfigRestModel.getHubUsername());
        assertEquals(restModel.getId(), transformedConfigRestModel.getId());

        assertEquals(configEntity.getAccumulatorCron(), transformedConfigEntity.getAccumulatorCron());
        assertEquals(configEntity.getDailyDigestCron(), transformedConfigEntity.getDailyDigestCron());
        assertEquals(configEntity.getHubPassword(), transformedConfigEntity.getHubPassword());
        assertEquals(configEntity.getHubTimeout(), transformedConfigEntity.getHubTimeout());
        assertEquals(configEntity.getHubUsername(), transformedConfigEntity.getHubUsername());
        assertEquals(configEntity.getId(), transformedConfigEntity.getId());
    }

    @Test
    public void testTransformEmailModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final EmailConfigRestModel restModel = mockUtils.createEmailConfigRestModel();
        final EmailConfigEntity configEntity = mockUtils.createEmailConfigEntity();

        final EmailConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, EmailConfigEntity.class);
        final EmailConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(configEntity, EmailConfigRestModel.class);

        assertEquals(restModel, transformedConfigRestModel);
        assertEquals(configEntity, transformedConfigEntity);
    }

    @Test
    public void testTransformHipchatModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final HipChatConfigRestModel restModel = mockUtils.createHipChatConfigRestModel();
        final HipChatConfigEntity configEntity = mockUtils.createHipChatConfigEntity();

        final HipChatConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, HipChatConfigEntity.class);
        final HipChatConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(configEntity, HipChatConfigRestModel.class);
        assertEquals(restModel, transformedConfigRestModel);
        assertEquals(configEntity, transformedConfigEntity);
    }

    @Test
    public void testTransformListsOfModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final EmailConfigRestModel restModel = mockUtils.createEmailConfigRestModel();
        final EmailConfigEntity configEntity = mockUtils.createEmailConfigEntity();

        List<EmailConfigEntity> transformedConfigEntities = objectTransformer.configRestModelsToDatabaseEntities(Arrays.asList(restModel), EmailConfigEntity.class);
        List<EmailConfigRestModel> transformedConfigRestModels = objectTransformer.databaseEntitiesToConfigRestModels(Arrays.asList(configEntity), EmailConfigRestModel.class);
        assertNotNull(transformedConfigEntities);
        assertNotNull(transformedConfigRestModels);
        assertTrue(transformedConfigEntities.size() == 1);
        assertTrue(transformedConfigRestModels.size() == 1);
        assertEquals(restModel, transformedConfigRestModels.get(0));
        assertEquals(configEntity, transformedConfigEntities.get(0));

        transformedConfigEntities = objectTransformer.configRestModelsToDatabaseEntities(null, EmailConfigEntity.class);
        transformedConfigRestModels = objectTransformer.databaseEntitiesToConfigRestModels(null, EmailConfigRestModel.class);
    }

    @Test
    public void testTransformNullModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();

        EmailConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(null, EmailConfigEntity.class);
        EmailConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(null, EmailConfigRestModel.class);
        assertNull(transformedConfigRestModel);
        assertNull(transformedConfigEntity);

        transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(mockUtils.createEmailConfigRestModel(), null);
        transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(mockUtils.createEmailConfigEntity(), null);
        assertNull(transformedConfigRestModel);
        assertNull(transformedConfigEntity);

        transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(null, null);
        transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(null, null);
        assertNull(transformedConfigRestModel);
        assertNull(transformedConfigEntity);
    }

    @Test
    public void testStringToInteger() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        assertNull(objectTransformer.stringToInteger(null));
        assertNull(objectTransformer.stringToInteger(""));
        assertNull(objectTransformer.stringToInteger("false"));
        assertNull(objectTransformer.stringToInteger("hello"));
        assertEquals(Integer.valueOf(2), objectTransformer.stringToInteger("2"));
        assertEquals(Integer.valueOf(212), objectTransformer.stringToInteger("  212"));
        assertEquals(Integer.valueOf(567), objectTransformer.stringToInteger("567   "));
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

    @SuppressWarnings("serial")
    public static class TestDatabaseEntity extends DatabaseEntity {
        public Map<String, String> map;

        public TestDatabaseEntity() {
        }
    }

    @SuppressWarnings("serial")
    public static class TestConfigRestModel extends ConfigRestModel {
        public String map;

        public TestConfigRestModel() {
        }
    }

    @SuppressWarnings("serial")
    public static class TestDatabaseEntityDifferentField extends DatabaseEntity {
        public Map<String, String> newMap;

        public TestDatabaseEntityDifferentField() {
        }
    }

    @SuppressWarnings("serial")
    public static class TestConfigRestModelDifferentField extends ConfigRestModel {
        public String newMap;

        public TestConfigRestModelDifferentField() {
        }
    }

    @SuppressWarnings("serial")
    public class TestDatabaseEntityInstantiationException extends DatabaseEntity {
        public Map<String, String> map;
    }

    @SuppressWarnings("serial")
    public class TestConfigRestModelInstantiationException extends ConfigRestModel {
        public String map;
    }

}
