package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.processor.api.filter.model.FilterableNotificationWrapper;
import com.synopsys.integration.alert.processor.api.filter.model.NotificationFilterMapModel;
import com.synopsys.integration.datastructure.SetMap;

@Component
public class DefaultJobNotificationExtractor implements JobNotificationExtractor {
    private JobAccessor jobAccessor;

    @Autowired
    public DefaultJobNotificationExtractor(JobAccessor jobAccessor) {
        this.jobAccessor = jobAccessor;
    }

    /*
     * Filter Items:
     * Frequency (Passed into processor)
     * Notification Type (From notification)
     * Filter By Project (Projects from notification if applicable)
     *   Project Name
     *   Project Name Pattern
     * Filter by Vulnerability severity (From notification if applicable)
     * Filter by Policy name (From notification if applicable)
     */

    @Override
    public Map<NotificationFilterMapModel, List<FilterableNotificationWrapper<?>>> mapJobsToNotifications(List<? extends FilterableNotificationWrapper<?>> filterableNotifications, @Nullable FrequencyType frequency) {
        SetMap<CollapsibleNotificationFilter, FilterableNotificationWrapper<?>> groupedFilterableNotifications = SetMap.createDefault();
        for (FilterableNotificationWrapper filterableNotificationWrapper : filterableNotifications) {
            CollapsibleNotificationFilter collapsibleNotificationFilter = CollapsibleNotificationFilter.from(filterableNotificationWrapper);
            groupedFilterableNotifications.add(collapsibleNotificationFilter, filterableNotificationWrapper);
        }
        for (Map.Entry<CollapsibleNotificationFilter, Set<FilterableNotificationWrapper<?>>> groupedNotifications : groupedFilterableNotifications.entrySet()) {
            new FilteredDistributionJobRequestModel(
                frequency,
                
                );
            jobAccessor.getMatchingEnabledJobs();
        }
    }

    private static class CollapsibleNotificationFilter extends AlertSerializableModel {
        private String notificationType;
        private List<String> projectNames;
        private List<String> vulnerabilitySeverities;
        private List<String> policyNames;

        public static CollapsibleNotificationFilter from(FilterableNotificationWrapper filterableNotificationWrapper) {
            return new CollapsibleNotificationFilter(
                filterableNotificationWrapper.extractNotificationType(),
                filterableNotificationWrapper.getProjectNames(),
                filterableNotificationWrapper.getVulnerabilitySeverities(),
                filterableNotificationWrapper.getPolicyNames()
            );
        }

        public CollapsibleNotificationFilter(String notificationType, List<String> projectNames, List<String> vulnerabilitySeverities, List<String> policyNames) {
            this.notificationType = notificationType;
            this.projectNames = projectNames;
            this.vulnerabilitySeverities = vulnerabilitySeverities;
            this.policyNames = policyNames;
        }

        public String getNotificationType() {
            return notificationType;
        }

        public List<String> getProjectNames() {
            return projectNames;
        }

        public List<String> getVulnerabilitySeverities() {
            return vulnerabilitySeverities;
        }

        public List<String> getPolicyNames() {
            return policyNames;
        }
    }
}
