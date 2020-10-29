package com.synopsys.integration.alert.common.persistence.model.job.details;

public class MSTeamsJobDetailsModel extends DistributionJobDetailsModel {
    private final String webhook;

    public MSTeamsJobDetailsModel(String webhook) {
        super("msteamskey");
        this.webhook = webhook;
    }

    public String getWebhook() {
        return webhook;
    }

}
