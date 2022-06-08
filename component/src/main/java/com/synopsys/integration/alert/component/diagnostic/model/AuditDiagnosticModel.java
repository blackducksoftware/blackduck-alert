package com.synopsys.integration.alert.component.diagnostic.model;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class AuditDiagnosticModel extends AlertSerializableModel {
    private static final long serialVersionUID = 6608629673526467492L;

    public static final String NO_AUDIT_CONTENT_MESSAGE = "No audit content found";

    private final Long numberOfAuditEntriesSuccessful;
    private final Long numberOfAuditEntriesFailed;
    private final Long numberOfAuditEntriesPending;
    @Nullable
    private final String averageAuditTime;

    public AuditDiagnosticModel(
        Long numberOfAuditEntriesSuccessful,
        Long numberOfAuditEntriesFailed,
        Long numberOfAuditEntriesPending,
        @Nullable String averageAuditTime
    ) {
        this.numberOfAuditEntriesSuccessful = numberOfAuditEntriesSuccessful;
        this.numberOfAuditEntriesFailed = numberOfAuditEntriesFailed;
        this.numberOfAuditEntriesPending = numberOfAuditEntriesPending;
        this.averageAuditTime = averageAuditTime;
    }

    public Long getNumberOfAuditEntriesSuccessful() {
        return numberOfAuditEntriesSuccessful;
    }

    public Long getNumberOfAuditEntriesFailed() {
        return numberOfAuditEntriesFailed;
    }

    public Long getNumberOfAuditEntriesPending() {
        return numberOfAuditEntriesPending;
    }

    public Optional<String> getAverageAuditTime() {
        return Optional.ofNullable(averageAuditTime);
    }
}
