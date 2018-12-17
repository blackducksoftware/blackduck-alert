package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.channel.email.descriptor.EmailGlobalDescriptorActionApi;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;

public class AlertStartupInitializerTestIT {

    @Test
    public void testInitializeConfigs() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final BaseDescriptorAccessor baseDescriptorAccessor = Mockito.mock(BaseDescriptorAccessor.class);
        final BaseConfigurationAccessor baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        final EmailGlobalDescriptorActionApi descriptorConfig = Mockito.mock(EmailGlobalDescriptorActionApi.class);
        final List<DescriptorActionApi> restApis = Arrays.asList(descriptorConfig);
        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(), Arrays.asList(), Arrays.asList(), restApis);
        final AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environment, baseDescriptorAccessor, baseConfigurationAccessor);
        initializer.initializeConfigs(true);
        assertFalse(initializer.getAlertPropertyNameSet().isEmpty());
    }

    @Test
    public void testInitializeConfigsEmptyInitializerList() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final BaseDescriptorAccessor baseDescriptorAccessor = Mockito.mock(BaseDescriptorAccessor.class);
        final BaseConfigurationAccessor baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        final EmailGlobalDescriptorActionApi descriptorConfig = Mockito.mock(EmailGlobalDescriptorActionApi.class);
        final List<DescriptorActionApi> restApis = Arrays.asList(descriptorConfig);
        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(), Arrays.asList(), Arrays.asList(), restApis);
        final AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environment, baseDescriptorAccessor, baseConfigurationAccessor);
        initializer.initializeConfigs(true);
        assertTrue(initializer.getAlertPropertyNameSet().isEmpty());
    }

    @Test
    public void testSetRestModelValue() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final BaseDescriptorAccessor descriptorAccessor = Mockito.mock(BaseDescriptorAccessor.class);
        final BaseConfigurationAccessor configurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        final EmailGlobalDescriptorActionApi descriptorConfig = Mockito.mock(EmailGlobalDescriptorActionApi.class);
        final List<DescriptorActionApi> restApis = Arrays.asList(descriptorConfig);
        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(), Arrays.asList(), Arrays.asList(), restApis);
        final AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environment, descriptorAccessor, configurationAccessor);
        initializer.initializeConfigs(true);
        final String value = "newValue";
        final int times = descriptorMap.getDescriptorMap().keySet().size();
        Mockito.verify(descriptorAccessor, Mockito.times(times)).getFieldsForDescriptor(Mockito.anyString(), ConfigContextEnum.GLOBAL);
        Mockito.verify(configurationAccessor, Mockito.times(times)).getConfigurationByDescriptorNameAndContext(Mockito.anyString(), ConfigContextEnum.GLOBAL);
        Mockito.verify(configurationAccessor, Mockito.times(times)).updateConfiguration(Mockito.anyLong(), Mockito.anyCollection());
    }

    //    @Test
    //    public void testSetRestModelValueEmtpyValue() throws Exception {
    //        final Environment environment = Mockito.mock(Environment.class);
    //        final ConversionService conversionService = new DefaultConversionService();
    //        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);
    //        final PropertyInitializer propertyInitializer = new PropertyInitializer();
    //        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
    //        final EmailGlobalTypeConverter emailGlobalContentConverter = new EmailGlobalTypeConverter(contentConverter);
    //        final EmailGlobalRepositoryAccessor emailGlobalRepositoryAccessor = new EmailGlobalRepositoryAccessor(emailGlobalRepository);
    //        final EntityPropertyMapper entityPropertyMapper = new EntityPropertyMapper();
    //        final EmailGlobalDescriptorActionApi descriptorConfig = new EmailGlobalDescriptorActionApi(emailGlobalContentConverter, emailGlobalRepositoryAccessor, new EmailGlobalStartupComponent(entityPropertyMapper), null);
    //        final List<DescriptorActionApi> restApis = Arrays.asList(descriptorConfig);
    //        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(), Arrays.asList(), Arrays.asList(), restApis);
    //        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptorMap, environment, conversionService);
    //        initializer.initializeConfigs();
    //
    //        final EmailGlobalConfig globalRestModel = new EmailGlobalConfig();
    //        final AlertStartupProperty property = initializer.getAlertStartupProperties().get(0);
    //        initializer.setRestModelValue(null, globalRestModel, property);
    //
    //        final Field declaredField = globalRestModel.getClass().getDeclaredField(property.getFieldName());
    //        final boolean accessible = declaredField.isAccessible();
    //
    //        declaredField.setAccessible(true);
    //        final Object fieldValue = declaredField.get(globalRestModel);
    //        declaredField.setAccessible(accessible);
    //        assertNull(fieldValue);
    //    }
    //
    //    @Test
    //    public void testCanConvertFalse() throws Exception {
    //        final Environment environment = Mockito.mock(Environment.class);
    //        final ConversionService conversionService = Mockito.mock(ConversionService.class);
    //        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenReturn(false);
    //        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);
    //        final PropertyInitializer propertyInitializer = new PropertyInitializer();
    //        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
    //        final EmailGlobalTypeConverter emailGlobalContentConverter = new EmailGlobalTypeConverter(contentConverter);
    //        final EmailGlobalRepositoryAccessor emailGlobalRepositoryAccessor = new EmailGlobalRepositoryAccessor(emailGlobalRepository);
    //        final EntityPropertyMapper entityPropertyMapper = new EntityPropertyMapper();
    //        final EmailGlobalDescriptorActionApi descriptorConfig = new EmailGlobalDescriptorActionApi(emailGlobalContentConverter, emailGlobalRepositoryAccessor, new EmailGlobalStartupComponent(entityPropertyMapper), null);
    //        final List<DescriptorActionApi> restApis = Arrays.asList(descriptorConfig);
    //        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(), Arrays.asList(), Arrays.asList(), restApis);
    //        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptorMap, environment, conversionService);
    //        initializer.initializeConfigs();
    //        final EmailGlobalConfig globalRestModel = new EmailGlobalConfig();
    //        final AlertStartupProperty property = initializer.getAlertStartupProperties().get(0);
    //        initializer.setRestModelValue("a value that can't be converted", globalRestModel, property);
    //
    //        final Field declaredField = globalRestModel.getClass().getDeclaredField(property.getFieldName());
    //        final boolean accessible = declaredField.isAccessible();
    //
    //        declaredField.setAccessible(true);
    //        final Object fieldValue = declaredField.get(globalRestModel);
    //        declaredField.setAccessible(accessible);
    //        assertNull(fieldValue);
    //    }
    //
    //    @Test
    //    public void testSystemPropertyExists() throws Exception {
    //        final Environment environment = Mockito.mock(Environment.class);
    //        final ConversionService conversionService = Mockito.mock(ConversionService.class);
    //        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenReturn(true);
    //        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);
    //        final MockEmailGlobalEntity mockEmailGlobalEntity = new MockEmailGlobalEntity();
    //        Mockito.when(emailGlobalRepository.save(Mockito.any())).thenReturn(mockEmailGlobalEntity.createGlobalEntity());
    //        final PropertyInitializer propertyInitializer = new PropertyInitializer();
    //        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
    //        final EmailGlobalTypeConverter emailGlobalContentConverter = new EmailGlobalTypeConverter(contentConverter);
    //        final EmailGlobalRepositoryAccessor emailGlobalRepositoryAccessor = new EmailGlobalRepositoryAccessor(emailGlobalRepository);
    //        final EntityPropertyMapper entityPropertyMapper = new EntityPropertyMapper();
    //        final EmailGlobalDescriptorActionApi descriptorConfig = new EmailGlobalDescriptorActionApi(emailGlobalContentConverter, emailGlobalRepositoryAccessor, new EmailGlobalStartupComponent(entityPropertyMapper), null);
    //        final List<DescriptorActionApi> restApis = Arrays.asList(descriptorConfig);
    //        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(), Arrays.asList(), Arrays.asList(), restApis);
    //        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptorMap, environment, conversionService);
    //
    //        initializer.initializeConfigs(true);
    //        final AlertStartupProperty property = initializer.getAlertStartupProperties().get(0);
    //        final String value = "a system property value";
    //        System.setProperty(property.getPropertyKey(), value);
    //        initializer.initializeConfigs(true);
    //    }
    //
    //    @Test
    //    public void testSetRestModelSecurityException() throws Exception {
    //        final Environment environment = Mockito.mock(Environment.class);
    //        final ConversionService conversionService = Mockito.mock(ConversionService.class);
    //        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);
    //        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenThrow(new SecurityException());
    //        final PropertyInitializer propertyInitializer = new PropertyInitializer();
    //        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
    //        final EmailGlobalTypeConverter emailGlobalContentConverter = new EmailGlobalTypeConverter(contentConverter);
    //        final EmailGlobalRepositoryAccessor emailGlobalRepositoryAccessor = new EmailGlobalRepositoryAccessor(emailGlobalRepository);
    //        final EntityPropertyMapper entityPropertyMapper = new EntityPropertyMapper();
    //        final EmailGlobalDescriptorActionApi descriptorConfig = new EmailGlobalDescriptorActionApi(emailGlobalContentConverter, emailGlobalRepositoryAccessor, new EmailGlobalStartupComponent(entityPropertyMapper), null);
    //        final List<DescriptorActionApi> restApis = Arrays.asList(descriptorConfig);
    //        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(), Arrays.asList(), Arrays.asList(), restApis);
    //        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptorMap, environment, conversionService);
    //        initializer.initializeConfigs(true);
    //    }
    //
    //    @Test
    //    public void testSetRestModelIllegalArgumentException() throws Exception {
    //        final Environment environment = Mockito.mock(Environment.class);
    //        final ConversionService conversionService = Mockito.mock(ConversionService.class);
    //        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);
    //        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenThrow(new IllegalArgumentException());
    //        final PropertyInitializer propertyInitializer = new PropertyInitializer();
    //        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
    //        final EmailGlobalTypeConverter emailGlobalContentConverter = new EmailGlobalTypeConverter(contentConverter);
    //        final EmailGlobalRepositoryAccessor emailGlobalRepositoryAccessor = new EmailGlobalRepositoryAccessor(emailGlobalRepository);
    //        final EntityPropertyMapper entityPropertyMapper = new EntityPropertyMapper();
    //        final EmailGlobalDescriptorActionApi descriptorConfig = new EmailGlobalDescriptorActionApi(emailGlobalContentConverter, emailGlobalRepositoryAccessor, new EmailGlobalStartupComponent(entityPropertyMapper), null);
    //        final List<DescriptorActionApi> restApis = Arrays.asList(descriptorConfig);
    //        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(), Arrays.asList(), Arrays.asList(), restApis);
    //        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptorMap, environment, conversionService);
    //        initializer.initializeConfigs(true);
    //    }
    //
    //    @Test
    //    public void testInitializeConfigsThrowsIllegalArgumentException() throws Exception {
    //        final Environment environment = Mockito.mock(Environment.class);
    //        final ConversionService conversionService = new DefaultConversionService();
    //        Mockito.doThrow(new IllegalArgumentException()).when(environment).getProperty(Mockito.anyString());
    //        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);
    //        throwExceptionTest(environment, conversionService, emailGlobalRepository);
    //    }
    //
    //    @Test
    //    public void testInitializeConfigsThrowsSecurityException() throws Exception {
    //        final Environment environment = Mockito.mock(Environment.class);
    //        final ConversionService conversionService = new DefaultConversionService();
    //        Mockito.doThrow(new SecurityException()).when(environment).getProperty(Mockito.anyString());
    //        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);
    //        throwExceptionTest(environment, conversionService, emailGlobalRepository);
    //    }
    //
    //    private void throwExceptionTest(final Environment environment, final ConversionService conversionService, final EmailGlobalRepository emailGlobalRepository) throws Exception {
    //        final PropertyInitializer propertyInitializer = new PropertyInitializer();
    //        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
    //        final EmailGlobalTypeConverter emailGlobalContentConverter = new EmailGlobalTypeConverter(contentConverter);
    //        final EmailGlobalRepositoryAccessor emailGlobalRepositoryAccessor = new EmailGlobalRepositoryAccessor(emailGlobalRepository);
    //        final EntityPropertyMapper entityPropertyMapper = new EntityPropertyMapper();
    //        final EmailGlobalDescriptorActionApi descriptorConfig = new EmailGlobalDescriptorActionApi(emailGlobalContentConverter, emailGlobalRepositoryAccessor, new EmailGlobalStartupComponent(entityPropertyMapper), null);
    //        final List<DescriptorActionApi> restApis = Arrays.asList(descriptorConfig);
    //        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(), Arrays.asList(), Arrays.asList(), restApis);
    //        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptorMap, environment, conversionService);
    //        initializer.initializeConfigs();
    //        assertFalse(initializer.getAlertPropertyNameSet().isEmpty());
    //    }
}
