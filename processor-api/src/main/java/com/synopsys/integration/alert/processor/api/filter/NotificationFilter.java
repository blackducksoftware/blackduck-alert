package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;

import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;

public interface NotificationFilter {
    List<FilterableNotificationWrapper<?>> filter(DistributionJobModel job, List<FilterableNotificationWrapper<?>> notifications);

}
