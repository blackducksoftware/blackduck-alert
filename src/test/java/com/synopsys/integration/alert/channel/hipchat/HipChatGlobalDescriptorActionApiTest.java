package com.synopsys.integration.alert.channel.hipchat;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static com.synopsys.integration.alert.util.FieldModelUtil.addFieldValueToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatGlobalDescriptorActionApi;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.rest.connection.RestConnection;

public class HipChatGlobalDescriptorActionApiTest {

    @Test
    public void validateConfigEmptyTest() {
        final HipChatChannel hipChatChannel = Mockito.mock(HipChatChannel.class);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel);

        final FieldAccessor fieldAccessor = new FieldAccessor(new HashMap<>());
        final Map<String, String> fieldErrors = new HashMap<>();

        hipChatGlobalDescriptorActionApi.validateConfig(fieldAccessor, fieldErrors);
        assertEquals("ApiKey can't be blank", fieldErrors.get("apiKey"));
    }

    @Test
    public void validateConfigInvalidTest() {
        final HipChatChannel hipChatChannel = Mockito.mock(HipChatChannel.class);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel);

        final Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        addConfigurationFieldToMap(fields, HipChatDescriptor.KEY_API_KEY, "");
        addConfigurationFieldToMap(fields, HipChatDescriptor.KEY_HOST_SERVER, "anything");

        final FieldAccessor fieldAccessor = new FieldAccessor(fields);
        final Map<String, String> fieldErrors = new HashMap<>();

        hipChatGlobalDescriptorActionApi.validateConfig(fieldAccessor, fieldErrors);
        assertEquals("ApiKey can't be blank", fieldErrors.get("apiKey"));
    }

    @Test
    public void validateConfigValidTest() {
        final HipChatChannel hipChatChannel = Mockito.mock(HipChatChannel.class);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel);

        final Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        addConfigurationFieldToMap(fields, HipChatDescriptor.KEY_API_KEY, "API Token");
        addConfigurationFieldToMap(fields, HipChatDescriptor.KEY_HOST_SERVER, "anything");

        final FieldAccessor fieldAccessor = new FieldAccessor(fields);
        final Map<String, String> fieldErrors = new HashMap<>();

        hipChatGlobalDescriptorActionApi.validateConfig(fieldAccessor, fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testConfigWithoutGlobalConfigTest() throws Exception {
        final HipChatChannel hipChatChannel = Mockito.mock(HipChatChannel.class);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel);

        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel, "fake");
        try {
            hipChatGlobalDescriptorActionApi.testConfig(testConfigModel);
            fail("Should have thrown exception");
        } catch (final AlertException e) {
            assertTrue(e.getMessage().contains("ERROR: Missing global config."));
        }
    }

    @Test
    public void testConfigInvalidDestinationTest() throws Exception {
        final ChannelRestConnectionFactory restConnectionFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HipChatChannel hipChatChannel = Mockito.mock(HipChatChannel.class);

        Mockito.when(restConnectionFactory.createRestConnection()).thenReturn(restConnection);
        Mockito.when(hipChatChannel.getChannelRestConnectionFactory()).thenReturn(restConnectionFactory);
        ////////////////////////////////////////

        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel);

        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        addFieldValueToMap(keyToValues, HipChatDescriptor.KEY_API_KEY, "API Token");
        addFieldValueToMap(keyToValues, HipChatDescriptor.KEY_HOST_SERVER, "anything");

        final FieldModel fieldModel = new FieldModel(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel, "fake");
        try {
            hipChatGlobalDescriptorActionApi.testConfig(testConfigModel);
            fail("Should have thrown exception");
        } catch (final AlertException e) {
            assertTrue(e.getMessage().contains("The provided room id is an invalid number"));
        }
    }

    @Test
    public void testConfigTest() throws Exception {
        final ChannelRestConnectionFactory restConnectionFactory = Mockito.mock(ChannelRestConnectionFactory.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HipChatChannel hipChatChannel = Mockito.mock(HipChatChannel.class);

        Mockito.when(restConnectionFactory.createRestConnection()).thenReturn(restConnection);
        Mockito.when(hipChatChannel.getChannelRestConnectionFactory()).thenReturn(restConnectionFactory);
        ////////////////////////////////////////

        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel);

        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        addFieldValueToMap(keyToValues, HipChatDescriptor.KEY_API_KEY, "API Token");
        addFieldValueToMap(keyToValues, HipChatDescriptor.KEY_HOST_SERVER, "anything");

        final FieldModel fieldModel = new FieldModel(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel, "123142");

        hipChatGlobalDescriptorActionApi.testConfig(testConfigModel);

        final ArgumentCaptor<String> hostServer = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> apiKey = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Integer> roomId = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<Boolean> notify = ArgumentCaptor.forClass(Boolean.class);
        final ArgumentCaptor<String> color = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

        Mockito.verify(hipChatChannel).createRequest(hostServer.capture(), apiKey.capture(), roomId.capture(), notify.capture(), color.capture(), message.capture());

        assertEquals("anything", hostServer.getValue());
        assertEquals("API Token", apiKey.getValue());
        assertTrue(123142 == roomId.getValue());
        assertTrue(notify.getValue());
        assertEquals("red", color.getValue());
        assertEquals("This is a test message sent by Alert.", message.getValue());

    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void testConfigITTest() throws Exception {
        final TestProperties properties = new TestProperties();

        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);

        final HipChatChannel hipChatChannel = new HipChatChannel(new Gson(), testAlertProperties, auditUtility, channelRestConnectionFactory);

        ////////////////////////////////////////

        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel);

        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        addFieldValueToMap(keyToValues, HipChatDescriptor.KEY_API_KEY, properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY));

        final FieldModel fieldModel = new FieldModel(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel, properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));

        hipChatGlobalDescriptorActionApi.testConfig(testConfigModel);
    }
}
