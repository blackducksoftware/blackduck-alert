package com.synopsys.integration.alert.web.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.database.api.DefaultSystemStatusUtility;
import com.synopsys.integration.alert.database.system.DefaultSystemMessageUtility;
import com.synopsys.integration.alert.web.api.system.MultiSystemMessageModel;
import com.synopsys.integration.alert.web.api.system.SystemActions;
import com.synopsys.integration.alert.web.common.field.FieldModelProcessor;
import com.synopsys.integration.rest.RestConstants;

public class SystemActionsTest {
    private static final SettingsDescriptorKey SETTINGS_DESCRIPTOR_KEY = new SettingsDescriptorKey();

    private DefaultSystemStatusUtility defaultSystemStatusUtility;
    private DefaultSystemMessageUtility defaultSystemMessageUtility;
    private FieldModelProcessor fieldModelProcessor;
    private SettingsUtility settingsUtility;

    @BeforeEach
    public void initiailize() {
        defaultSystemStatusUtility = Mockito.mock(DefaultSystemStatusUtility.class);
        defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        settingsUtility = Mockito.mock(SettingsUtility.class);
        fieldModelProcessor = Mockito.mock(FieldModelProcessor.class);
        List<SystemMessageModel> messages = createSystemMessageList();
        Mockito.when(defaultSystemMessageUtility.getSystemMessages()).thenReturn(messages);
        Mockito.when(defaultSystemMessageUtility.getSystemMessagesBefore(Mockito.any())).thenReturn(messages);
        Mockito.when(defaultSystemMessageUtility.getSystemMessagesAfter(Mockito.any())).thenReturn(messages);
        Mockito.when(defaultSystemMessageUtility.findBetween(Mockito.any())).thenReturn(messages);
        Mockito.when(settingsUtility.getKey()).thenReturn(SETTINGS_DESCRIPTOR_KEY);
        Mockito.when(settingsUtility.doesConfigurationExist()).thenReturn(true);
    }

    public static List<Pair<String, String>> getStartAndEndTimes() {
        return List.of(
            Pair.of(null, null),
            Pair.of("2018-11-13T00:00:00.000Z", null),
            Pair.of(null, "2018-11-13T00:00:00.000Z"),
            Pair.of("2018-11-13T00:00:00.000Z", "2018-11-13T01:00:00.000Z")
        );
    }

    @ParameterizedTest
    @MethodSource("getStartAndEndTimes")
    public void testGetSystemMessages(Pair<String, String> startAndEndTimes) {
        SystemActions systemActions = new SystemActions(defaultSystemStatusUtility, defaultSystemMessageUtility, fieldModelProcessor, settingsUtility);
        String startTime = startAndEndTimes.getLeft();
        String endTime = startAndEndTimes.getRight();
        ActionResponse<MultiSystemMessageModel> response = systemActions.getSystemMessages(startTime, endTime);
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        List<SystemMessageModel> messages = response.getContent()
                                                .map(MultiSystemMessageModel::getSystemMessages)
                                                .orElse(List.of());
        assertFalse(messages.isEmpty());
    }

    @Test
    public void getSystemMessagesSinceStartup() {
        SystemActions systemActions = new SystemActions(defaultSystemStatusUtility, defaultSystemMessageUtility, fieldModelProcessor, settingsUtility);
        ActionResponse<MultiSystemMessageModel> response = systemActions.getSystemMessagesSinceStartup();
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        List<SystemMessageModel> messages = response.getContent()
                                                .map(MultiSystemMessageModel::getSystemMessages)
                                                .orElse(List.of());
        assertFalse(messages.isEmpty());
    }

    @Test
    public void testInvalidDate() {
        String invalidDate = "2018-13--13T00/00:00.000Z";
        SystemActions systemActions = new SystemActions(defaultSystemStatusUtility, defaultSystemMessageUtility, fieldModelProcessor, settingsUtility);
        ActionResponse<MultiSystemMessageModel> response = systemActions.getSystemMessages(invalidDate, null);
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    }

    private List<SystemMessageModel> createSystemMessageList() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        zonedDateTime = zonedDateTime.minusMinutes(1);
        return List.of(new SystemMessageModel("1", RestConstants.formatDate(Date.from(zonedDateTime.toInstant())), "type", "content", "type"));
    }

}
