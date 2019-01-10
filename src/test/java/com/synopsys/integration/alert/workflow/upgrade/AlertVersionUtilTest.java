package com.synopsys.integration.alert.workflow.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.common.database.BaseSettingsKeyAccessor;
import com.synopsys.integration.alert.database.api.settingskey.SettingsKeyModel;

public class AlertVersionUtilTest {

    @Test
    public void alertVersionFullTest() {
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        final BaseSettingsKeyAccessor baseSettingsKeyAccessor = Mockito.mock(BaseSettingsKeyAccessor.class);

        Mockito.when(aboutReader.getProductVersion()).thenReturn("1");
        final Optional<SettingsKeyModel> optionalSettingsKeyModel = Optional.of(new SettingsKeyModel(1L, AlertVersionUtil.KEY_ALERT_VERSION, "2"));
        Mockito.when(baseSettingsKeyAccessor.getSettingsKeyByKey(Mockito.anyString())).thenReturn(optionalSettingsKeyModel);

        final AlertVersionUtil alertVersionUtil = new AlertVersionUtil(baseSettingsKeyAccessor, aboutReader);

        final AlertVersion alertVersion = alertVersionUtil.findAlertVersion();

        assertEquals("1", alertVersion.getFileVersion());
        assertEquals("2", alertVersion.getDbVersion());
    }

    @Test
    public void alertDoVersionsMatchTest() {
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        final BaseSettingsKeyAccessor baseSettingsKeyAccessor = Mockito.mock(BaseSettingsKeyAccessor.class);

        Mockito.when(aboutReader.getProductVersion()).thenReturn("1");
        final Optional<SettingsKeyModel> optionalSettingsKeyModel = Optional.of(new SettingsKeyModel(1L, AlertVersionUtil.KEY_ALERT_VERSION, "1"));
        Mockito.when(baseSettingsKeyAccessor.getSettingsKeyByKey(Mockito.anyString())).thenReturn(optionalSettingsKeyModel);

        final AlertVersionUtil alertVersionUtil = new AlertVersionUtil(baseSettingsKeyAccessor, aboutReader);

        final AlertVersion alertVersion = alertVersionUtil.findAlertVersion();
        assertTrue(alertVersionUtil.doVersionsMatch(alertVersion.getDbVersion(), alertVersion.getFileVersion()));
    }

    @Test
    public void alertDoVersionsMatchFailedTest() {
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        final BaseSettingsKeyAccessor baseSettingsKeyAccessor = Mockito.mock(BaseSettingsKeyAccessor.class);

        Mockito.when(aboutReader.getProductVersion()).thenReturn("1");
        final Optional<SettingsKeyModel> optionalSettingsKeyModel = Optional.of(new SettingsKeyModel(1L, AlertVersionUtil.KEY_ALERT_VERSION, "2"));
        Mockito.when(baseSettingsKeyAccessor.getSettingsKeyByKey(Mockito.anyString())).thenReturn(optionalSettingsKeyModel);

        final AlertVersionUtil alertVersionUtil = new AlertVersionUtil(baseSettingsKeyAccessor, aboutReader);

        final AlertVersion alertVersion = alertVersionUtil.findAlertVersion();
        assertFalse(alertVersionUtil.doVersionsMatch(alertVersion.getDbVersion(), alertVersion.getFileVersion()));
    }
}
