package com.synopsys.integration.alert.web.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.database.api.SystemStatusUtility;
import com.synopsys.integration.alert.database.system.SystemMessage;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.web.config.ConfigActions;

public class SystemActionsTest {
    private static final SettingsDescriptorKey SETTINGS_DESCRIPTOR_KEY = new SettingsDescriptorKey();

    private SystemStatusUtility systemStatusUtility;
    private SystemMessageUtility systemMessageUtility;
    private ConfigActions configActions;

    @BeforeEach
    public void initiailize() {
        systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        configActions = Mockito.mock(ConfigActions.class);
        final List<SystemMessage> messages = createSystemMessageList();
        Mockito.when(systemMessageUtility.getSystemMessages()).thenReturn(messages);
        Mockito.when(systemMessageUtility.getSystemMessagesAfter(Mockito.any())).thenReturn(messages);
    }

    @Test
    public void getSystemMessagesSinceStartup() {
        final SystemActions systemActions = new SystemActions(SETTINGS_DESCRIPTOR_KEY, systemStatusUtility, systemMessageUtility, configActions);
        systemActions.getSystemMessagesSinceStartup();
        Mockito.verify(systemStatusUtility).getStartupTime();
        Mockito.verify(systemMessageUtility).getSystemMessagesAfter(Mockito.any());
    }

    @Test
    public void testGetSystemMessagesAfter() throws Exception {
        final SystemActions systemActions = new SystemActions(SETTINGS_DESCRIPTOR_KEY, systemStatusUtility, systemMessageUtility, configActions);
        systemActions.getSystemMessagesAfter("2018-11-13T00:00:00.000Z");
        Mockito.verify(systemMessageUtility).getSystemMessagesAfter(Mockito.any());
    }

    @Test
    public void testGetSystemMessagesBefore() throws Exception {
        final SystemActions systemActions = new SystemActions(SETTINGS_DESCRIPTOR_KEY, systemStatusUtility, systemMessageUtility, configActions);
        systemActions.getSystemMessagesBefore("2018-11-13T00:00:00.000Z");
        Mockito.verify(systemMessageUtility).getSystemMessagesBefore(Mockito.any());
    }

    @Test
    public void testGetSystemMessagesBetween() throws Exception {
        final SystemActions systemActions = new SystemActions(SETTINGS_DESCRIPTOR_KEY, systemStatusUtility, systemMessageUtility, configActions);
        systemActions.getSystemMessagesBetween("2018-11-13T00:00:00.000Z", "2018-11-13T01:00:00.000Z");
        Mockito.verify(systemMessageUtility).findBetween(Mockito.any());
    }

    @Test
    public void testGetSystemMessages() {
        final SystemActions systemActions = new SystemActions(SETTINGS_DESCRIPTOR_KEY, systemStatusUtility, systemMessageUtility, configActions);
        systemActions.getSystemMessages();
        Mockito.verify(systemMessageUtility).getSystemMessages();
    }

    @Test
    public void testIsInitiailzed() {
        final SystemActions systemActions = new SystemActions(SETTINGS_DESCRIPTOR_KEY, systemStatusUtility, systemMessageUtility, configActions);

        assertFalse(systemActions.isSystemInitialized());
        Mockito.when(systemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        assertTrue(systemActions.isSystemInitialized());
    }

    @Test
    public void testGetCurrentSystemSetup() throws Exception {
        final SystemActions systemActions = new SystemActions(SETTINGS_DESCRIPTOR_KEY, systemStatusUtility, systemMessageUtility, configActions);
        final String defaultAdminPassword = "defaultPassword";
        final String globalEncryptionPassword = "password";
        final String globalEncryptionSalt = "salt";
        final String proxyHost = "host";
        final String proxyPort = "port";
        final String proxyUsername = "username";
        final String proxyPassword = "password";

        final Map<String, FieldValueModel> valueMap = new HashMap<>();
        final FieldModel expected = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), "GLOBAL", valueMap);
        expected.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of(defaultAdminPassword), true));
        expected.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(globalEncryptionPassword), true));
        expected.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(globalEncryptionSalt), true));
        expected.putField(SettingsDescriptor.KEY_PROXY_HOST, new FieldValueModel(List.of(proxyHost), true));
        expected.putField(SettingsDescriptor.KEY_PROXY_PORT, new FieldValueModel(List.of(proxyPort), true));
        expected.putField(SettingsDescriptor.KEY_PROXY_USERNAME, new FieldValueModel(List.of(proxyUsername), true));
        expected.putField(SettingsDescriptor.KEY_PROXY_PWD, new FieldValueModel(List.of(proxyPassword), true));

        Mockito.when(configActions.getConfigs(Mockito.any(), Mockito.anyString())).thenReturn(List.of(expected));

        final FieldModel actual = systemActions.getCurrentSystemSetup();

        assertEquals(globalEncryptionPassword, actual.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).flatMap(field -> field.getValue()).orElse(null));
        assertEquals(globalEncryptionSalt, actual.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).flatMap(field -> field.getValue()).orElse(null));
        assertEquals(proxyHost, actual.getFieldValueModel(SettingsDescriptor.KEY_PROXY_HOST).flatMap(field -> field.getValue()).orElse(null));
        assertEquals(proxyPort, actual.getFieldValueModel(SettingsDescriptor.KEY_PROXY_PORT).flatMap(field -> field.getValue()).orElse(null));
        assertEquals(proxyUsername, actual.getFieldValueModel(SettingsDescriptor.KEY_PROXY_USERNAME).flatMap(field -> field.getValue()).orElse(null));
        assertEquals(proxyPassword, actual.getFieldValueModel(SettingsDescriptor.KEY_PROXY_PWD).flatMap(field -> field.getValue()).orElse(null));
    }

    @Test
    public void testSaveRequiredInformation() throws Exception {
        final SystemActions systemActions = new SystemActions(SETTINGS_DESCRIPTOR_KEY, systemStatusUtility, systemMessageUtility, configActions);
        final String defaultAdminPassword = "defaultPassword";
        final String globalEncryptionPassword = "password";
        final String globalEncryptionSalt = "salt";
        final String proxyHost = "host";
        final String proxyPort = "port";
        final String proxyUsername = "username";
        final String proxyPassword = "password";

        final Map<String, FieldValueModel> valueMap = new HashMap<>();
        final FieldModel model = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), "GLOBAL", valueMap);
        model.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of(defaultAdminPassword), true));
        model.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(globalEncryptionPassword), true));
        model.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(globalEncryptionSalt), true));
        model.putField(SettingsDescriptor.KEY_PROXY_HOST, new FieldValueModel(List.of(proxyHost), true));
        model.putField(SettingsDescriptor.KEY_PROXY_PORT, new FieldValueModel(List.of(proxyPort), true));
        model.putField(SettingsDescriptor.KEY_PROXY_USERNAME, new FieldValueModel(List.of(proxyUsername), true));
        model.putField(SettingsDescriptor.KEY_PROXY_PWD, new FieldValueModel(List.of(proxyPassword), true));

        final Map<String, String> fieldErrors = new HashMap<>();
        systemActions.saveRequiredInformation(model, fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    private List<SystemMessage> createSystemMessageList() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        zonedDateTime = zonedDateTime.minusMinutes(1);
        return Collections.singletonList(new SystemMessage(Date.from(zonedDateTime.toInstant()), "type", "content", "type"));
    }
}
