package com.synopsys.integration.alert.startup.component;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.descriptor.EmailGlobalUIConfig;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.startup.EnvironmentVariableUtility;

public class AlertStartupInitializerTest {
    private static final SettingsDescriptorKey SETTINGS_DESCRIPTOR_KEY = new SettingsDescriptorKey();

    //TODO these tests need to be re-written

    @Test
    public void testInitializeConfigs() throws Exception {
        Environment environment = Mockito.mock(Environment.class);
        DescriptorAccessor baseDescriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        EncryptionSettingsValidator encryptionValidator = new EncryptionSettingsValidator(encryptionUtility);
        ChannelDescriptor channelDescriptor = new EmailDescriptor(new EmailGlobalUIConfig(encryptionValidator), null);
        SettingsDescriptorKey settingsDescriptorKey = new SettingsDescriptorKey();

        List<DescriptorKey> descriptorKeys = List.of(channelDescriptor.getDescriptorKey(), settingsDescriptorKey);
        List<Descriptor> descriptors = List.of(channelDescriptor);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        DescriptorMap descriptorMap = new DescriptorMap(descriptorKeys, descriptors);
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, baseDescriptorAccessor, descriptorKeys);
        Mockito.when(baseDescriptorAccessor.getFieldsForDescriptor(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.copyOf(channelDescriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL)));

        FieldModelProcessor fieldModelProcessor = new FieldModelProcessor(modelConverter, new FieldValidationUtility(), new DescriptorProcessor(descriptorMap, baseConfigurationAccessor, List.of()));
        SettingsUtility settingsUtility = Mockito.mock(SettingsUtility.class);
        Mockito.when(settingsUtility.getKey()).thenReturn(settingsDescriptorKey);
        AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environmentVariableUtility, baseDescriptorAccessor, baseConfigurationAccessor, modelConverter, fieldModelProcessor, settingsUtility);
        initializer.initializeComponent();
        Mockito.verify(baseDescriptorAccessor, Mockito.times(4)).getFieldsForDescriptor(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor, Mockito.times(2)).getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class));
    }

    @Test
    public void testInitializeConfigsEmptyInitializerList() throws Exception {
        Environment environment = Mockito.mock(Environment.class);
        DescriptorAccessor baseDescriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(), List.of());
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, baseDescriptorAccessor, List.of());
        FieldModelProcessor fieldModelProcessor = new FieldModelProcessor(modelConverter, new FieldValidationUtility(), new DescriptorProcessor(descriptorMap, baseConfigurationAccessor, List.of()));
        SettingsUtility settingsUtility = Mockito.mock(SettingsUtility.class);
        Mockito.when(settingsUtility.getKey()).thenReturn(new SettingsDescriptorKey());
        AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environmentVariableUtility, baseDescriptorAccessor, baseConfigurationAccessor, modelConverter, fieldModelProcessor, settingsUtility);
        initializer.initializeComponent();
        // called to get the settings component configuration and fields.
        Mockito.verify(baseDescriptorAccessor, Mockito.times(1)).getFieldsForDescriptor(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor, Mockito.times(1)).getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class));
        // nothing should be saved
        //Mockito.verify(baseConfigurationAccessor, Mockito.times(1)).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
    }

    @Test
    public void testSetRestModelValueCreate() throws Exception {
        Environment environment = Mockito.mock(Environment.class);
        DescriptorAccessor baseDescriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        EncryptionSettingsValidator encryptionValidator = new EncryptionSettingsValidator(encryptionUtility);
        ChannelDescriptor channelDescriptor = new EmailDescriptor(new EmailGlobalUIConfig(encryptionValidator), null);

        List<DescriptorKey> descriptorKeys = List.of(channelDescriptor.getDescriptorKey(), SETTINGS_DESCRIPTOR_KEY);
        List<Descriptor> descriptors = List.of(channelDescriptor);

        DescriptorMap descriptorMap = new DescriptorMap(descriptorKeys, descriptors);
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, baseDescriptorAccessor, descriptorKeys);
        Mockito.when(baseDescriptorAccessor.getFieldsForDescriptor(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.copyOf(channelDescriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL)));
        final String value = "newValue";
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(value);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);

        FieldModelProcessor fieldModelProcessor = new FieldModelProcessor(modelConverter, new FieldValidationUtility(), new DescriptorProcessor(descriptorMap, baseConfigurationAccessor, List.of()));
        SettingsUtility settingsUtility = Mockito.mock(SettingsUtility.class);
        Mockito.when(settingsUtility.getKey()).thenReturn(new SettingsDescriptorKey());
        AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environmentVariableUtility, baseDescriptorAccessor, baseConfigurationAccessor, modelConverter, fieldModelProcessor, settingsUtility);
        initializer.initializeComponent();
        Mockito.verify(baseDescriptorAccessor, Mockito.times(4)).getFieldsForDescriptor(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor, Mockito.times(2)).createConfiguration(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
        Mockito.verify(baseConfigurationAccessor, Mockito.times(2)).getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class));
    }

    @Test
    public void testOverwrite() throws Exception {
        Environment environment = Mockito.mock(Environment.class);
        DescriptorAccessor baseDescriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        EncryptionSettingsValidator encryptionValidator = new EncryptionSettingsValidator(encryptionUtility);
        ChannelDescriptor channelDescriptor = new EmailDescriptor(new EmailGlobalUIConfig(encryptionValidator), null);
        ConfigurationModel settingsModel = Mockito.mock(ConfigurationModel.class);
        ConfigurationFieldModel envOverrideField = Mockito.mock(ConfigurationFieldModel.class);
        ConfigurationModel slackModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(envOverrideField.getFieldValue()).thenReturn(Optional.of("true"));
        Mockito.when(baseConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(SETTINGS_DESCRIPTOR_KEY, ConfigContextEnum.GLOBAL)).thenReturn(List.of(settingsModel));
        Mockito.when(baseConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(channelDescriptor.getDescriptorKey(), ConfigContextEnum.GLOBAL)).thenReturn(List.of(slackModel));

        List<DescriptorKey> descriptorKeys = List.of(channelDescriptor.getDescriptorKey(), SETTINGS_DESCRIPTOR_KEY);
        List<Descriptor> descriptors = List.of(channelDescriptor);

        DescriptorMap descriptorMap = new DescriptorMap(descriptorKeys, descriptors);
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, baseDescriptorAccessor, descriptorKeys);
        Mockito.when(baseDescriptorAccessor.getFieldsForDescriptor(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.copyOf(channelDescriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL)));

        final String value = "newValue";
        Mockito.when(environment.getProperty(Mockito.startsWith("ALERT_CHANNEL_"))).thenReturn(value);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);

        FieldModelProcessor fieldModelProcessor = new FieldModelProcessor(modelConverter, new FieldValidationUtility(), new DescriptorProcessor(descriptorMap, baseConfigurationAccessor, List.of()));
        SettingsUtility settingsUtility = Mockito.mock(SettingsUtility.class);
        Mockito.when(settingsUtility.getKey()).thenReturn(new SettingsDescriptorKey());
        AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environmentVariableUtility, baseDescriptorAccessor, baseConfigurationAccessor, modelConverter, fieldModelProcessor, settingsUtility);
        initializer.initializeComponent();

        Mockito.verify(baseDescriptorAccessor, Mockito.times(2)).getFieldsForDescriptor(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(baseConfigurationAccessor, Mockito.times(0)).createConfiguration(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
        Mockito.verify(baseConfigurationAccessor, Mockito.times(2)).getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class));
        //Mockito.verify(baseConfigurationAccessor, Mockito.times(2)).updateConfiguration(Mockito.anyLong(), Mockito.anyCollection());
    }

}
