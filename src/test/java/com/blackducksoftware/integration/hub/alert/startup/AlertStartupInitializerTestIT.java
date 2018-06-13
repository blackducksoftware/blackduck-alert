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
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;

import com.blackducksoftware.integration.hub.alert.channel.AbstractPropertyInitializer;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailChannelPropertyInitializer;
import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;

public class AlertStartupInitializerTestIT {

    @Test
    public void testInitializeConfigs() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment, conversionService);
        initializer.initializeConfigs();
        assertFalse(initializer.getAlertProperties().isEmpty());
        assertFalse(initializer.getAlertPropertyNameSet().isEmpty());
    }

    @Test
    public void testInitializeConfigsEmptyInitializerList() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Collections.emptyList();
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment, conversionService);
        initializer.initializeConfigs();
        assertTrue(initializer.getAlertProperties().isEmpty());
        assertTrue(initializer.getAlertPropertyNameSet().isEmpty());
    }

    @Test
    public void testSetRestModelValue() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment, conversionService);
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
        final ConversionService conversionService = new DefaultConversionService();
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment, conversionService);
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
    public void testCanConvertFalse() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = Mockito.mock(ConversionService.class);
        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenReturn(false);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment, conversionService);
        initializer.initializeConfigs();
        final GlobalEmailConfigRestModel globalRestModel = new GlobalEmailConfigRestModel();
        final AlertStartupProperty property = initializer.getAlertProperties().get(0);
        initializer.setRestModelValue("a value that can't be converted", globalRestModel, property);

        final Field declaredField = globalRestModel.getClass().getDeclaredField(property.getFieldName());
        final boolean accessible = declaredField.isAccessible();

        declaredField.setAccessible(true);
        final Object fieldValue = declaredField.get(globalRestModel);
        declaredField.setAccessible(accessible);
        assertNull(fieldValue);
    }

    @Test
    public void testSystemPropertyExists() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = Mockito.mock(ConversionService.class);
        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenReturn(true);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment, conversionService);

        initializer.initializeConfigs();
        final AlertStartupProperty property = initializer.getAlertProperties().get(0);
        final String value = "a system property value";
        System.setProperty(property.getPropertyKey(), value);
        initializer.initializeConfigs();
    }

    @Test
    public void testSetRestModelSecurityException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = Mockito.mock(ConversionService.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenThrow(new SecurityException());
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment, conversionService);
        initializer.initializeConfigs();
    }

    @Test
    public void testSetRestModelIllegalArgumentException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = Mockito.mock(ConversionService.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenThrow(new IllegalArgumentException());
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment, conversionService);
        initializer.initializeConfigs();
    }

    @Test
    public void testInitializeConfigsThrowsIllegalArgumentException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        Mockito.doThrow(new IllegalArgumentException()).when(environment).getProperty(Mockito.anyString());
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        throwExceptionTest(environment, conversionService, objectTransformer, globalEmailRepository);
    }

    @Test
    public void testInitializeConfigsThrowsSecurityException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        Mockito.doThrow(new SecurityException()).when(environment).getProperty(Mockito.anyString());
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        throwExceptionTest(environment, conversionService, objectTransformer, globalEmailRepository);
    }

    @Test
    public void testInitializeConfigsThrowsAlertException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        final ObjectTransformer objectTransformer = Mockito.mock(ObjectTransformer.class);
        Mockito.doThrow(new AlertException()).when(objectTransformer).configRestModelToDatabaseEntity(Mockito.any(), Mockito.any());
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        throwExceptionTest(environment, conversionService, objectTransformer, globalEmailRepository);
    }

    private void throwExceptionTest(final Environment environment, final ConversionService conversionService, final ObjectTransformer objectTransformer, final GlobalEmailRepository globalEmailRepository) throws Exception {
        final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList = Arrays.asList(new EmailChannelPropertyInitializer(globalEmailRepository));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(objectTransformer, propertyManagerList, environment, conversionService);
        initializer.initializeConfigs();
        assertFalse(initializer.getAlertPropertyNameSet().isEmpty());
    }
}
