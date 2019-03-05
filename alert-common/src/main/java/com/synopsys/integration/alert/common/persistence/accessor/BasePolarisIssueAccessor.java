package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.PolarisIssueModel;

public interface BasePolarisIssueAccessor {
    List<PolarisIssueModel> getProjectIssues(final String projectHref) throws AlertDatabaseConstraintException;

    PolarisIssueModel updateIssueType(final String projectHref, final String issueType, final Integer newCount) throws AlertDatabaseConstraintException;
}
