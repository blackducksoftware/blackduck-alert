package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.channel.email.descriptor.EmailGlobalDescriptorActionApi;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatGlobalUIConfig;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;

public class AlertStartupInitializerTest {

    @Test
    public void testInitializeConfigs() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final BaseDescriptorAccessor baseDescriptorAccessor = Mockito.mock(BaseDescriptorAccessor.class);
        final BaseConfigurationAccessor baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        final EmailGlobalDescriptorActionApi descriptorConfig = Mockito.mock(EmailGlobalDescriptorActionApi.class);
        final ChannelDescriptor channelDescriptor = new HipChatDescriptor(null, null, null, null, new HipChatGlobalUIConfig());
        Mockito.when(baseDescriptorAccessor.getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class))).thenReturn(List.copyOf(channelDescriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL)));
        final List<ChannelDescriptor> channelDescriptors = List.of(channelDescriptor);
        final List<ProviderDescriptor> providerDescriptors = List.of();
        final List<ComponentDescriptor> componentDescriptors = List.of();
        final DescriptorMap descriptorMap = new DescriptorMap(channelDescriptors, providerDescriptors, componentDescriptors);
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
        final DescriptorMap descriptorMap = new DescriptorMap(List.of(), List.of(), List.of());
        final AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environment, baseDescriptorAccessor, baseConfigurationAccessor);
        initializer.initializeConfigs(true);
        assertTrue(initializer.getAlertPropertyNameSet().isEmpty());
    }

    @Test
    public void testSetRestModelValueCreate() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final BaseDescriptorAccessor descriptorAccessor = Mockito.mock(BaseDescriptorAccessor.class);
        final BaseConfigurationAccessor configurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        final EmailGlobalDescriptorActionApi descriptorConfig = Mockito.mock(EmailGlobalDescriptorActionApi.class);
        final List<DescriptorActionApi> restApis = List.of(descriptorConfig);
        final ChannelDescriptor channelDescriptor = new HipChatDescriptor(null, null, null, null, new HipChatGlobalUIConfig());
        Mockito.when(descriptorAccessor.getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class))).thenReturn(List.copyOf(channelDescriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL)));
        final List<ChannelDescriptor> channelDescriptors = List.of(channelDescriptor);
        final List<ProviderDescriptor> providerDescriptors = List.of();
        final List<ComponentDescriptor> componentDescriptors = List.of();
        final DescriptorMap descriptorMap = new DescriptorMap(channelDescriptors, providerDescriptors, componentDescriptors);
        final String value = "newValue";
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn(value);

        final AlertStartupInitializer initializer = new AlertStartupInitializer(descriptorMap, environment, descriptorAccessor, configurationAccessor);
        initializer.initializeConfigs(true);
        final int times = descriptorMap.getDescriptorMap().keySet().size();
        Mockito.verify(descriptorAccessor, Mockito.times(times)).getFieldsForDescriptor(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(configurationAccessor, Mockito.times(times)).getConfigurationByDescriptorNameAndContext(Mockito.anyString(), Mockito.any(ConfigContextEnum.class));
        Mockito.verify(configurationAccessor, Mockito.times(times)).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
    }
}
