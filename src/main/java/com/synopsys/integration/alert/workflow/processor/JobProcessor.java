package com.synopsys.integration.alert.workflow.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.distribution.CommonDistributionConfigReader;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.workflow.filter.NotificationFilter;

@Component
public class JobProcessor {

    private final CommonDistributionConfigReader commonDistributionConfigReader;
    private final List<ProviderDescriptor> providerDescriptors;
    private final NotificationFilter notificationFilter;

    @Autowired
    public JobProcessor(final List<ProviderDescriptor> providerDescriptors, final CommonDistributionConfigReader commonDistributionConfigReader, final NotificationFilter notificationFilter) {
        this.providerDescriptors = providerDescriptors;
        this.commonDistributionConfigReader = commonDistributionConfigReader;
        this.notificationFilter = notificationFilter;
    }

    public List<TopicContent> processNotifications(final FrequencyType frequency, final Collection<NotificationContent> notificationList) {
        List<TopicContent> topicContentList = new LinkedList<>();

        final List<CommonDistributionConfig> unfilteredDistributionConfigs = commonDistributionConfigReader.getPopulatedConfigs();
        if (unfilteredDistributionConfigs.isEmpty()) {
            return Collections.emptyList();
        }

        final Predicate<CommonDistributionConfig> frequencyFilter = config -> frequency.name().equals(config.getFrequency());
        final List<CommonDistributionConfig> distributionConfigs = applyFilter(unfilteredDistributionConfigs, frequencyFilter);
        if (distributionConfigs.isEmpty()) {
            return Collections.emptyList();
        }

        topicContentList = distributionConfigs.parallelStream().flatMap(jobConfiguration -> collectTopics(jobConfiguration, notificationList).stream()).collect(Collectors.toList());

        return topicContentList;
    }

    private Collection<NotificationContent> filterNotifications(final CommonDistributionConfig jobConfiguration, final Collection<NotificationContent> notificationCollection) {
        Predicate<NotificationContent> providerFilter = (notificationContent) -> jobConfiguration.getProviderName().equals(notificationContent.getProvider());
        Collection<NotificationContent> providerNotifications = applyFilter(notificationCollection, providerFilter);
        final Collection<NotificationContent> filteredNotificationList = notificationFilter.extractApplicableNotifications(jobConfiguration, providerNotifications);
        return filteredNotificationList;
    }

    private List<TopicContent> collectTopics(final CommonDistributionConfig jobConfiguration, final Collection<NotificationContent> notificationCollection) {
        final FormatType formatType = FormatType.valueOf(jobConfiguration.getFormatType());
        final Map<String, TopicCollector> collectorMap = new HashMap<>();

        for (final ProviderDescriptor providerDescriptor : providerDescriptors) {
            final Set<TopicCollector> providerTopicCollectors = providerDescriptor.createTopicCollectors();
            for (final TopicCollector collector : providerTopicCollectors) {
                for (final String notificationType : collector.getSupportedNotificationTypes()) {
                    collectorMap.put(notificationType, collector);
                }
            }
        }
        final Collection<NotificationContent> notificationsForJob = filterNotifications(jobConfiguration, notificationCollection);
        notificationsForJob.forEach(notificationContent -> {
            String notificationType = notificationContent.getNotificationType();
            if(collectorMap.containsKey(notificationType)) {
                collectorMap.get(notificationType).insert(notificationContent);
            }
        });

        return collectorMap.values().stream().flatMap(collector -> collector.collect(formatType).stream()).collect(Collectors.toList());
    }

    //TODO notificationFilter also has this should it be made common?
    private <T> List<T> applyFilter(final Collection<T> notificationList, final Predicate<T> filter) {
        return notificationList
                   .parallelStream()
                   .filter(filter)
                   .collect(Collectors.toList());
    }

}
