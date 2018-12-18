package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.FieldRegistrationIntegrationTest;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class DescriptorTestConfigTest extends FieldRegistrationIntegrationTest {
    protected Gson gson;
    protected DistributionEvent channelEvent;

    protected ContentConverter contentConverter;
    protected TestProperties properties;

    @Autowired
    protected ConfigurationAccessor configurationAccessor;

    @Autowired
    protected DescriptorAccessor descriptorAccessor;

    protected ConfigurationAccessor.ConfigurationModel provider_global;
    protected ConfigurationAccessor.ConfigurationModel global_config;
    protected ConfigurationAccessor.ConfigurationModel distribution_config;

    public abstract DistributionEvent createChannelEvent();

    @BeforeEach
    public void init() throws Exception {
        registerDescriptors();
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        properties = new TestProperties();
        global_config = saveGlobalConfiguration();
        distribution_config = saveDistributionConfiguration();
        channelEvent = createChannelEvent();
    }

    @AfterEach
    public void cleanupTest() throws Exception {
        if (global_config != null) {
            configurationAccessor.deleteConfiguration(global_config);
        }

        if (distribution_config != null) {
            configurationAccessor.deleteConfiguration(distribution_config);
        }

        if (provider_global != null) {
            configurationAccessor.deleteConfiguration(provider_global);
        }
        final List<DescriptorAccessor.RegisteredDescriptorModel> registeredDescriptorModels = descriptorAccessor.getRegisteredDescriptors();

        for (final DescriptorAccessor.RegisteredDescriptorModel registeredDescriptor : registeredDescriptorModels) {
            try {
                descriptorAccessor.unregisterDescriptor(registeredDescriptor.getName());
            } catch (final AlertDatabaseConstraintException ex) {
                ex.printStackTrace();
            }
        }
    }

    public abstract ConfigurationAccessor.ConfigurationModel saveGlobalConfiguration() throws Exception;

    public abstract ConfigurationAccessor.ConfigurationModel saveDistributionConfiguration() throws Exception;

    public Map<String, FieldValueModel> createFieldModelMap() {
        final List<ConfigurationFieldModel> configModels = Stream.concat(global_config.getCopyOfFieldList().stream(), distribution_config.getCopyOfFieldList().stream()).collect(Collectors.toList());
        final Map<String, FieldValueModel> fieldModelMap = new HashMap<>();
        for (final ConfigurationFieldModel model : configModels) {
            final String key = model.getFieldKey();
            Collection<String> values = Collections.emptyList();
            if (!model.isSensitive()) {
                values = model.getFieldValues();
            }
            final FieldValueModel fieldValueModel = new FieldValueModel(values, model.isSet());
            fieldModelMap.put(key, fieldValueModel);
        }
        return fieldModelMap;
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
    public void testCreateChannelEvent() {
        final DistributionEvent channelEvent = this.createChannelEvent();

        assertEquals(distribution_config.getConfigurationId(), channelEvent.getConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
    }
}
