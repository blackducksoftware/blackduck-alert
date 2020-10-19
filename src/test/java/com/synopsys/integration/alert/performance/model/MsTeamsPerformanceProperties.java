package com.synopsys.integration.alert.performance.model;

import com.synopsys.integration.alert.channel.msteams.MsTeamsKey;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;

public class MsTeamsPerformanceProperties extends TestProperties {
    private final String msTeamsChannelKey;
    private final String msTeamsWebhook;

    public MsTeamsPerformanceProperties() {
        this.msTeamsChannelKey = new MsTeamsKey().getUniversalKey();

        this.msTeamsWebhook = getProperty(TestPropertyKey.TEST_MSTEAMS_WEBHOOK);
    }

    public String getMsTeamsChannelKey() {
        return msTeamsChannelKey;
    }

    public String getMsTeamsWebhook() {
        return msTeamsWebhook;
    }
}
