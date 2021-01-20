package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;

import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.processor.api.filter.model.FilterableNotificationWrapper;

public class DefaultJobNotificationFilter implements JobNotificationFilter {
    
    @Override
    public List<FilterableNotificationWrapper<?>> filter(final DistributionJobModel job, final List<FilterableNotificationWrapper<?>> notifications) {
        return null;
    }
}
