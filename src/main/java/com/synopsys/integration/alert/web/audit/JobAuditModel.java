package com.synopsys.integration.alert.web.audit;

import com.synopsys.integration.alert.web.model.MaskedModel;

public class JobAuditModel extends MaskedModel {
    private String timeAuditCreated;
    private String timeLastSent;
    private String status;

    public JobAuditModel() {
    }

    public JobAuditModel(final String timeAuditCreated, final String timeLastSent, final String status) {
        this.timeAuditCreated = timeAuditCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
    }

    public String getTimeAuditCreated() {
        return timeAuditCreated;
    }

    public String getTimeLastSent() {
        return timeLastSent;
    }

    public String getStatus() {
        return status;
    }
}
