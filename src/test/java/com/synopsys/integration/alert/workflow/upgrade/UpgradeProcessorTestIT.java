package com.synopsys.integration.alert.workflow.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.common.database.BaseSettingsKeyAccessor;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertUpgradeException;
import com.synopsys.integration.alert.database.api.settingskey.SettingsKeyModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.workflow.upgrade.step.UpgradeStep;
import com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.DescriptorRegistrar;

public class UpgradeProcessorTestIT extends AlertIntegrationTest {
    private int upgradeCounter = 0;

    @Autowired
    private BaseSettingsKeyAccessor baseSettingsKeyAccessor;

    @AfterEach
    public void deleteVersionKey() {
        baseSettingsKeyAccessor.deleteSettingsKeyByKey(AlertVersionUtil.KEY_ALERT_VERSION);
    }

    @Test
    public void runUpgradeTest() throws AlertUpgradeException, AlertDatabaseConstraintException {
        baseSettingsKeyAccessor.deleteSettingsKeyByKey(AlertVersionUtil.KEY_ALERT_VERSION);

        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn("2.0.0");
        final DescriptorRegistrar descriptorRegistrar = Mockito.mock(DescriptorRegistrar.class);
        Mockito.doNothing().when(descriptorRegistrar).registerDescriptors();
        final AlertVersionUtil alertVersionUtil = new AlertVersionUtil(baseSettingsKeyAccessor, aboutReader);
        final BooleanStep booleanStep = new BooleanStep();
        final UpgradeProcessor upgradeProcessor = new UpgradeProcessor(alertVersionUtil, descriptorRegistrar, List.of(booleanStep, new NothingStep()));

        upgradeProcessor.runUpgrade();

        final Optional<SettingsKeyModel> fullAlertVersion = baseSettingsKeyAccessor.getSettingsKeyByKey(AlertVersionUtil.KEY_ALERT_VERSION);
        assertTrue(fullAlertVersion.isPresent());
        assertEquals("2.0.0", fullAlertVersion.map(SettingsKeyModel::getValue).orElse(""));
        assertEquals(2, upgradeCounter);
        upgradeCounter = 0;

        assertTrue(booleanStep.getHasUpgraded());
    }

    @Test
    public void runMidwayUpgradeTest() throws AlertUpgradeException, AlertDatabaseConstraintException {
        final BooleanStep booleanStep = new BooleanStep();
        final NothingStep nothingStep = new NothingStep();

        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn(nothingStep.getVersion());
        final DescriptorRegistrar descriptorRegistrar = Mockito.mock(DescriptorRegistrar.class);
        Mockito.doNothing().when(descriptorRegistrar).registerDescriptors();

        final AlertVersionUtil alertVersionUtil = new AlertVersionUtil(baseSettingsKeyAccessor, aboutReader);
        alertVersionUtil.updateVersionInDB(booleanStep.getVersion());

        final UpgradeProcessor upgradeProcessor = new UpgradeProcessor(alertVersionUtil, descriptorRegistrar, List.of(booleanStep, nothingStep));

        upgradeProcessor.runUpgrade();

        final Optional<SettingsKeyModel> fullAlertVersion = baseSettingsKeyAccessor.getSettingsKeyByKey(AlertVersionUtil.KEY_ALERT_VERSION);
        assertTrue(fullAlertVersion.isPresent());
        assertEquals("2.0.0", fullAlertVersion.map(SettingsKeyModel::getValue).orElse(""));
        assertEquals(1, upgradeCounter);
        upgradeCounter = 0;

        assertFalse(booleanStep.getHasUpgraded());
    }

    @Test
    public void shouldUpgradeTest() {
        baseSettingsKeyAccessor.saveSettingsKey(AlertVersionUtil.KEY_ALERT_VERSION, "2");

        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn("2");
        final AlertVersionUtil alertVersionUtil = new AlertVersionUtil(baseSettingsKeyAccessor, aboutReader);
        final UpgradeProcessor upgradeProcessor = new UpgradeProcessor(alertVersionUtil, null, List.of());

        assertFalse(upgradeProcessor.shouldUpgrade());
    }

    @Test
    public void shouldNotUpgradeTest() {
        baseSettingsKeyAccessor.saveSettingsKey(AlertVersionUtil.KEY_ALERT_VERSION, "1");

        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn("2");
        final AlertVersionUtil alertVersionUtil = new AlertVersionUtil(baseSettingsKeyAccessor, aboutReader);
        final UpgradeProcessor upgradeProcessor = new UpgradeProcessor(alertVersionUtil, null, List.of());

        assertTrue(upgradeProcessor.shouldUpgrade());
    }

    private class BooleanStep extends UpgradeStep {
        private Boolean hasUpgraded;

        public BooleanStep() {
            super("1.0.0");
            hasUpgraded = false;
        }

        @Override
        public void runUpgrade() throws AlertUpgradeException {
            upgradeCounter++;
            hasUpgraded = true;
        }

        public Boolean getHasUpgraded() {
            return hasUpgraded;
        }
    }

    private class NothingStep extends UpgradeStep {

        public NothingStep() {
            super("2.0.0");
        }

        @Override
        public void runUpgrade() throws AlertUpgradeException {
            upgradeCounter++;
        }
    }
}
