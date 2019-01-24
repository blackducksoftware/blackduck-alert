package com.synopsys.integration.alert.channel.hipchat;

import static com.synopsys.integration.alert.util.FieldModelUtil.addFieldValueToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatGlobalDescriptorActionApi;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatGlobalUIConfig;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.common.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.mock.MockDescriptorAccessor;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;
import com.synopsys.integration.alert.web.model.configuration.TestConfigModel;
import com.synopsys.integration.rest.connection.RestConnection;

public class HipChatGlobalDescriptorActionApiTest {

    @Test
    public void validateConfigEmptyTest() {
        final HipChatChannel hipChatChannel = Mockito.mock(HipChatChannel.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final HipChatGlobalUIConfig uiConfig = new HipChatGlobalUIConfig();
        final BaseDescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(uiConfig.createFields());
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel, modelConverter);

        final FieldModel fieldModel = new FieldModel(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), Map.of());
        final Map<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        hipChatGlobalDescriptorActionApi.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(HipChatDescriptor.KEY_API_KEY));
    }

    @Test
    public void validateConfigInvalidTest() {
        final HipChatChannel hipChatChannel = Mockito.mock(HipChatChannel.class);
        final HipChatGlobalUIConfig uiConfig = new HipChatGlobalUIConfig();
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final BaseDescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(uiConfig.createFields());
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel, modelConverter);

        final Map<String, FieldValueModel> fields = new HashMap<>();
        addFieldValueToMap(fields, HipChatDescriptor.KEY_API_KEY, "");
        addFieldValueToMap(fields, HipChatDescriptor.KEY_HOST_SERVER, "anything");
        final FieldModel fieldModel = new FieldModel(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), fields);
        final Map<String, String> fieldErrors = new HashMap<>();

        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        hipChatGlobalDescriptorActionApi.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(HipChatDescriptor.KEY_API_KEY));
    }

    @Test
    public void validateConfigValidTest() {
        final HipChatChannel hipChatChannel = Mockito.mock(HipChatChannel.class);
        final HipChatGlobalUIConfig uiConfig = new HipChatGlobalUIConfig();
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final BaseDescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(uiConfig.createFields());
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel, modelConverter);

        final Map<String, FieldValueModel> fields = new HashMap<>();
        addFieldValueToMap(fields, HipChatDescriptor.KEY_API_KEY, "API Token");
        addFieldValueToMap(fields, HipChatDescriptor.KEY_HOST_SERVER, "anything");
        final FieldModel fieldModel = new FieldModel(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), fields);
        final Map<String, String> fieldErrors = new HashMap<>();

        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        hipChatGlobalDescriptorActionApi.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testConfigWithoutGlobalConfigTest() throws Exception {
        final HipChatChannel hipChatChannel = Mockito.mock(HipChatChannel.class);
        final HipChatGlobalUIConfig uiConfig = new HipChatGlobalUIConfig();
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final BaseDescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(uiConfig.createFields());
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel, modelConverter);

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

        final HipChatGlobalUIConfig uiConfig = new HipChatGlobalUIConfig();
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final BaseDescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(uiConfig.createFields());
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel, modelConverter);

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

        final HipChatGlobalUIConfig uiConfig = new HipChatGlobalUIConfig();
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final BaseDescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(uiConfig.createFields());
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel, modelConverter);

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

        final HipChatGlobalUIConfig uiConfig = new HipChatGlobalUIConfig();
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        final BaseDescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(uiConfig.createFields());
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor);
        final HipChatGlobalDescriptorActionApi hipChatGlobalDescriptorActionApi = new HipChatGlobalDescriptorActionApi(hipChatChannel, modelConverter);

        final Map<String, FieldValueModel> keyToValues = new HashMap<>();
        addFieldValueToMap(keyToValues, HipChatDescriptor.KEY_API_KEY, properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY));

        final FieldModel fieldModel = new FieldModel(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), keyToValues);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel, properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));
        hipChatGlobalDescriptorActionApi.testConfig(testConfigModel);
    }
}
