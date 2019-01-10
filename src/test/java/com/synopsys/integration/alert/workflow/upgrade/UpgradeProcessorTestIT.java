package com.synopsys.integration.alert.workflow.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.common.database.BaseSettingsKeyAccessor;
import com.synopsys.integration.alert.database.api.settingskey.SettingsKeyModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class UpgradeProcessorTestIT extends AlertIntegrationTest {

    @Autowired
    private BaseSettingsKeyAccessor baseSettingsKeyAccessor;

    @AfterEach
    public void deleteVersionKey() {
        baseSettingsKeyAccessor.deleteSettingsKeyByKey(AlertVersionUtil.KEY_ALERT_VERSION);
    }

    @Test
    public void runUpgradeTest() {
        baseSettingsKeyAccessor.deleteSettingsKeyByKey(AlertVersionUtil.KEY_ALERT_VERSION);

        final DescriptorRegistrar descriptorRegistrar = Mockito.mock(DescriptorRegistrar.class);
        Mockito.doNothing().when(descriptorRegistrar).registerDescriptors();
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn("2");
        final AlertVersionUtil alertVersionUtil = new AlertVersionUtil(baseSettingsKeyAccessor, aboutReader);
        final UpgradeProcessor upgradeProcessor = new UpgradeProcessor(alertVersionUtil, descriptorRegistrar);

        upgradeProcessor.runUpgrade();

        final Optional<SettingsKeyModel> fullAlertVersion = baseSettingsKeyAccessor.getSettingsKeyByKey(AlertVersionUtil.KEY_ALERT_VERSION);
        assertTrue(fullAlertVersion.isPresent());
        assertEquals("2", fullAlertVersion.map(SettingsKeyModel::getValue).orElse(""));
    }

    @Test
    public void shouldUpgradeTest() {
        baseSettingsKeyAccessor.saveSettingsKey(AlertVersionUtil.KEY_ALERT_VERSION, "2");

        final DescriptorRegistrar descriptorRegistrar = Mockito.mock(DescriptorRegistrar.class);
        Mockito.doNothing().when(descriptorRegistrar).registerDescriptors();
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn("2");
        final AlertVersionUtil alertVersionUtil = new AlertVersionUtil(baseSettingsKeyAccessor, aboutReader);
        final UpgradeProcessor upgradeProcessor = new UpgradeProcessor(alertVersionUtil, descriptorRegistrar);

        assertFalse(upgradeProcessor.shouldUpgrade());
    }

    @Test
    public void shouldNotUpgradeTest() {
        baseSettingsKeyAccessor.saveSettingsKey(AlertVersionUtil.KEY_ALERT_VERSION, "1");

        final DescriptorRegistrar descriptorRegistrar = Mockito.mock(DescriptorRegistrar.class);
        Mockito.doNothing().when(descriptorRegistrar).registerDescriptors();
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn("2");
        final AlertVersionUtil alertVersionUtil = new AlertVersionUtil(baseSettingsKeyAccessor, aboutReader);
        final UpgradeProcessor upgradeProcessor = new UpgradeProcessor(alertVersionUtil, descriptorRegistrar);

        assertTrue(upgradeProcessor.shouldUpgrade());
    }
}
