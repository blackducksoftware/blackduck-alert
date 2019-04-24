package com.synopsys.integration.alert.workflow.startup;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatGlobalUIConfig;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.workflow.event.ConfigurationEventPublisher;
import com.synopsys.integration.alert.component.settings.SettingsDescriptor;
import com.synopsys.integration.alert.web.config.FieldModelProcessor;

public class AlertStartupInitializerTest {

    @Test
    public void testInitializeConfigs() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final DescriptorAccessor baseDescriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ChannelDescriptor channelDescriptor = new HipChatDescriptor(null, null, null, new HipChatGlobalUIConfig());
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, baseDescriptorAccessor);
        Mockito.when(baseDescriptorAccessor.getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class))).thenReturn(List.copyOf(channelDescriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL)));
        final List<ChannelDescriptor> channelDescriptors = List.of(channelDescriptor);
        final List<ProviderDescriptor> providerDescriptors = List.of();
        final List<ComponentDescriptor> componentDescriptors = List.of();
        final DescriptorMap descriptorMap = new DescriptorMap(channelDescriptors, providerDescriptors, componentDescriptors);
        final FieldModelProcessor fieldModelProcessor = new FieldModelProcessor(baseDescriptorAccessor, baseConfigurationAccessor, descriptorMap, modelConverter, null, null);
        final AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environment, baseDescriptorAccessor, baseConfigurationAccessor, modelConverter, fieldModelProcessor);
        initializer.initializeConfigs();
        Mockito.verify(baseDescriptorAccessor, Mockito.times(2)).getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        // 2 times for the settings descriptor and once for the HipChatDescriptor
        Mockito.verify(baseConfigurationAccessor, Mockito.times(3)).getConfigurationByDescriptorNameAndContext(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor, Mockito.times(0)).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());

    }

    @Test
    public void testInitializeConfigsEmptyInitializerList() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final DescriptorAccessor baseDescriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final DescriptorMap descriptorMap = new DescriptorMap(List.of(), List.of(), List.of());
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, baseDescriptorAccessor);
        final FieldModelProcessor fieldModelProcessor = new FieldModelProcessor(baseDescriptorAccessor, baseConfigurationAccessor, descriptorMap, modelConverter, null, null);
        final AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environment, baseDescriptorAccessor, baseConfigurationAccessor, modelConverter, fieldModelProcessor);
        initializer.initializeConfigs();
        // called to get the settings component configuration and fields.
        Mockito.verify(baseDescriptorAccessor).getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor, Mockito.times(2)).getConfigurationByDescriptorNameAndContext(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        // nothing should be saved
        Mockito.verify(baseConfigurationAccessor, Mockito.times(0)).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
    }

    @Test
    public void testSetRestModelValueCreate() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final DescriptorAccessor baseDescriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ChannelDescriptor channelDescriptor = new HipChatDescriptor(null, null, null, new HipChatGlobalUIConfig());
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, baseDescriptorAccessor);
        Mockito.when(baseDescriptorAccessor.getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class))).thenReturn(List.copyOf(channelDescriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL)));
        final List<ChannelDescriptor> channelDescriptors = List.of(channelDescriptor);
        final List<ProviderDescriptor> providerDescriptors = List.of();
        final List<ComponentDescriptor> componentDescriptors = List.of();
        final DescriptorMap descriptorMap = new DescriptorMap(channelDescriptors, providerDescriptors, componentDescriptors);
        final String value = "newValue";
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(value);

        final ConfigurationEventPublisher configurationEventPublisher = Mockito.mock(ConfigurationEventPublisher.class);
        Mockito.doNothing().when(configurationEventPublisher).publishConfigurationEvent(Mockito.any(FieldModel.class), Mockito.any());
        final FieldModelProcessor fieldModelProcessor = new FieldModelProcessor(baseDescriptorAccessor, baseConfigurationAccessor, descriptorMap, modelConverter, null, configurationEventPublisher);
        final AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environment, baseDescriptorAccessor, baseConfigurationAccessor, modelConverter, fieldModelProcessor);
        initializer.initializeConfigs();
        Mockito.verify(baseDescriptorAccessor, Mockito.times(4)).getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor, Mockito.times(2)).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
        Mockito.verify(baseConfigurationAccessor, Mockito.times(3)).getConfigurationByDescriptorNameAndContext(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
    }

    @Test
    public void testGetSettingsThrowsException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final DescriptorAccessor baseDescriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ChannelDescriptor channelDescriptor = new HipChatDescriptor(null, null, null, new HipChatGlobalUIConfig());
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, baseDescriptorAccessor);
        Mockito.when(baseConfigurationAccessor.getConfigurationByDescriptorNameAndContext(Mockito.anyString(), Mockito.any(ConfigContextEnum.class))).thenThrow(new AlertDatabaseConstraintException("Test Exception"));
        final List<ChannelDescriptor> channelDescriptors = List.of(channelDescriptor);
        final List<ProviderDescriptor> providerDescriptors = List.of();
        final List<ComponentDescriptor> componentDescriptors = List.of();
        final DescriptorMap descriptorMap = new DescriptorMap(channelDescriptors, providerDescriptors, componentDescriptors);
        final FieldModelProcessor fieldModelProcessor = new FieldModelProcessor(baseDescriptorAccessor, baseConfigurationAccessor, descriptorMap, modelConverter, null, null);
        final AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environment, baseDescriptorAccessor, baseConfigurationAccessor, modelConverter, fieldModelProcessor);
        initializer.initializeConfigs();
        Mockito.verify(baseDescriptorAccessor, Mockito.times(2)).getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor, Mockito.times(3)).getConfigurationByDescriptorNameAndContext(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor, Mockito.times(0)).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
    }

    @Test
    public void testOverwrite() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final DescriptorAccessor baseDescriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ChannelDescriptor channelDescriptor = new HipChatDescriptor(null, null, null, new HipChatGlobalUIConfig());
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final ConfigurationModel settingsModel = Mockito.mock(ConfigurationModel.class);
        final ConfigurationFieldModel envOverrideField = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationModel hipChatModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(envOverrideField.getFieldValue()).thenReturn(Optional.of("true"));
        Mockito.when(settingsModel.getField(SettingsDescriptor.KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE)).thenReturn(Optional.of(envOverrideField));
        Mockito.when(baseConfigurationAccessor.getConfigurationByDescriptorNameAndContext(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL)).thenReturn(List.of(settingsModel));
        Mockito.when(baseConfigurationAccessor.getConfigurationByDescriptorNameAndContext(channelDescriptor.getName(), ConfigContextEnum.GLOBAL)).thenReturn(List.of(hipChatModel));

        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, baseDescriptorAccessor);
        Mockito.when(baseDescriptorAccessor.getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class))).thenReturn(List.copyOf(channelDescriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL)));

        final List<ChannelDescriptor> channelDescriptors = List.of(channelDescriptor);
        final List<ProviderDescriptor> providerDescriptors = List.of();
        final List<ComponentDescriptor> componentDescriptors = List.of();
        final DescriptorMap descriptorMap = new DescriptorMap(channelDescriptors, providerDescriptors, componentDescriptors);
        final String value = "newValue";
        Mockito.when(environment.getProperty(Mockito.startsWith("ALERT_CHANNEL_"))).thenReturn(value);

        final ConfigurationEventPublisher configurationEventPublisher = Mockito.mock(ConfigurationEventPublisher.class);
        Mockito.doNothing().when(configurationEventPublisher).publishConfigurationEvent(Mockito.any(FieldModel.class), Mockito.any());
        final FieldModelProcessor fieldModelProcessor = new FieldModelProcessor(baseDescriptorAccessor, baseConfigurationAccessor, descriptorMap, modelConverter, null, configurationEventPublisher);
        final AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environment, baseDescriptorAccessor, baseConfigurationAccessor, modelConverter, fieldModelProcessor);
        initializer.initializeConfigs();

        Mockito.verify(baseDescriptorAccessor, Mockito.times(3)).getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor, Mockito.times(0)).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
        Mockito.verify(baseConfigurationAccessor, Mockito.times(3)).getConfigurationByDescriptorNameAndContext(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor).updateConfiguration(Mockito.anyLong(), Mockito.anyCollection());
    }
}
