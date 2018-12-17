package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class DescriptorTestConfigTest extends AlertIntegrationTest {
    protected Gson gson;
    protected DistributionEvent channelEvent;

    protected ContentConverter contentConverter;
    protected TestProperties properties;

    @Autowired
    protected ConfigurationAccessor configurationAccessor;

    protected ConfigurationAccessor.ConfigurationModel global_config;
    protected ConfigurationAccessor.ConfigurationModel distribution_config;

    public abstract DistributionEvent createChannelEvent();

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        properties = new TestProperties();
        channelEvent = createChannelEvent();
    }

    @After
    public void cleanupTest() throws Exception {
        if (global_config != null) {
            configurationAccessor.deleteConfiguration(global_config);
        }

        if (distribution_config != null) {
            configurationAccessor.deleteConfiguration(distribution_config);
        }
    }

    public abstract Map<String, String> createGlobalConfiguration();

    public abstract Map<String, String> createDistributionConfiguration();

    public Map<String, String> getAllConfigurationMap() {
        final Map<String, String> globalConfiguration = createGlobalConfiguration();
        final Map<String, String> distributionConfiguration = createDistributionConfiguration();
        final Map<String, String> combinedMap = new HashMap<>(globalConfiguration.size() + distributionConfiguration.size());
        combinedMap.putAll(globalConfiguration);
        combinedMap.putAll(distributionConfiguration);
        return combinedMap;
    }

    public abstract ChannelDescriptor getDescriptor();

    public abstract FieldModel getFieldModel();

    @Test
    public void testSendTestMessage() throws Exception {
        final DescriptorActionApi descriptorActionApi = getDescriptor().getRestApi(ConfigContextEnum.DISTRIBUTION);
        final DescriptorActionApi spyDescriptorConfig = Mockito.spy(descriptorActionApi);
        final FieldModel restModel = getFieldModel();
        try {
            spyDescriptorConfig.testConfig(new TestConfigModel(restModel));
        } catch (final IntegrationException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Mockito.verify(spyDescriptorConfig).testConfig(Mockito.any());
    }

    @Test
    public void testCreateChannelEvent() throws Exception {
        final DistributionEvent channelEvent = this.createChannelEvent();

        assertEquals(Long.valueOf(1L), channelEvent.getConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
    }
}
