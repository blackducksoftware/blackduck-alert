package com.blackducksoftware.integration.hub.alert.startup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.blackducksoftware.integration.hub.alert.channel.AbstractChannelPropertyInitializer;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailChannelPropertyInitializer;
import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;

public class AlertStartupInitializerTestIT {

    @Test
    public void testInitializeConfigs() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepositoryWrapper globalEmailRepository = Mockito.mock(GlobalEmailRepositoryWrapper.class);
        final List<AbstractChannelPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment);
        initializer.initializeConfigs();
        assertFalse(initializer.getAlertProperties().isEmpty());
        assertFalse(initializer.getAlertPropertyNameSet().isEmpty());
    }

    @Test
    public void testInitializeConfigsEmptyInitializerList() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final List<AbstractChannelPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Collections.emptyList();
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment);
        initializer.initializeConfigs();
        assertTrue(initializer.getAlertProperties().isEmpty());
        assertTrue(initializer.getAlertPropertyNameSet().isEmpty());
    }

    @Test
    public void testSetRestModelValue() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepositoryWrapper globalEmailRepository = Mockito.mock(GlobalEmailRepositoryWrapper.class);
        final List<AbstractChannelPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment);
        initializer.initializeConfigs();
        final String value = "newValue";

        final GlobalEmailConfigRestModel globalRestModel = new GlobalEmailConfigRestModel();
        final AlertStartupProperty property = initializer.getAlertProperties().get(0);
        initializer.setRestModelValue(value, globalRestModel, property);

        final Field declaredField = globalRestModel.getClass().getDeclaredField(property.getFieldName());
        final boolean accessible = declaredField.isAccessible();

        declaredField.setAccessible(true);
        final Object fieldValue = declaredField.get(globalRestModel);
        declaredField.setAccessible(accessible);
        assertEquals(value, fieldValue);
    }

    @Test
    public void testSetRestModelValueEmtpyValue() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepositoryWrapper globalEmailRepository = Mockito.mock(GlobalEmailRepositoryWrapper.class);
        final List<AbstractChannelPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment);
        initializer.initializeConfigs();

        final GlobalEmailConfigRestModel globalRestModel = new GlobalEmailConfigRestModel();
        final AlertStartupProperty property = initializer.getAlertProperties().get(0);
        initializer.setRestModelValue(null, globalRestModel, property);

        final Field declaredField = globalRestModel.getClass().getDeclaredField(property.getFieldName());
        final boolean accessible = declaredField.isAccessible();

        declaredField.setAccessible(true);
        final Object fieldValue = declaredField.get(globalRestModel);
        declaredField.setAccessible(accessible);
        assertNull(fieldValue);
    }

    @Test
    public void testInitializeConfigsThrowsIllegalArgumentException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        Mockito.doThrow(new IllegalArgumentException()).when(environment).getProperty(Mockito.anyString());
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepositoryWrapper globalEmailRepository = Mockito.mock(GlobalEmailRepositoryWrapper.class);
        throwExceptionTest(environment, objectTransformer, globalEmailRepository);
    }

    @Test
    public void testInitializeConfigsThrowsSecurityException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        Mockito.doThrow(new SecurityException()).when(environment).getProperty(Mockito.anyString());
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepositoryWrapper globalEmailRepository = Mockito.mock(GlobalEmailRepositoryWrapper.class);
        throwExceptionTest(environment, objectTransformer, globalEmailRepository);
    }

    @Test
    public void testInitializeConfigsThrowsAlertException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ObjectTransformer objectTransformer = Mockito.mock(ObjectTransformer.class);
        Mockito.doThrow(new AlertException()).when(objectTransformer).configRestModelToDatabaseEntity(Mockito.any(), Mockito.any());
        final GlobalEmailRepositoryWrapper globalEmailRepository = Mockito.mock(GlobalEmailRepositoryWrapper.class);
        throwExceptionTest(environment, objectTransformer, globalEmailRepository);
    }

    private void throwExceptionTest(final Environment environment, final ObjectTransformer objectTransformer, final GlobalEmailRepositoryWrapper globalEmailRepository) throws Exception {
        final List<AbstractChannelPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment);
        initializer.initializeConfigs();
        assertFalse(initializer.getAlertPropertyNameSet().isEmpty());
    }
}
