package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.exception.AlertUpgradeException;
import com.synopsys.integration.alert.util.OutputLogger;

public class UpgradeStep_4_0_0Test {

    @Test
    public void runUpgradeTest() throws IOException, AlertUpgradeException {
        UpgradeStep_4_0_0 upgradeStep400 = new UpgradeStep_4_0_0(List.of());

        String expectedVersion = "4.0.0";
        String expectedWarning = "ALERT_CHANNEL_EMAIL_MAIL_SMTP_DSN_RET";

        OutputLogger outputLogger = new OutputLogger();
        upgradeStep400.runUpgrade();
        final boolean lineContainingText = outputLogger.isLineContainingText(expectedWarning);

        assertEquals(expectedVersion, upgradeStep400.getVersion());
        assertTrue(lineContainingText);
    }
}
