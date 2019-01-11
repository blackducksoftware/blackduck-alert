package com.synopsys.integration.alert.workflow.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals("1", alertVersionUtil.findFileVersion());
        assertEquals("2", alertVersionUtil.findDBVersion());
    }
}
