package com.blackducksoftware.integration.hub.alert.processor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;

public class NotificationTypeProcessor {
    private final Map<String, NotificationModel> modelMap = new LinkedHashMap<>(500);
    private final Collection<NotificationProcessingRule> processingRules;

    public NotificationTypeProcessor(final Collection<NotificationProcessingRule> processingRules) {
        this.processingRules = processingRules;
    }

    public Collection<NotificationProcessingRule> getProcessingRules() {
        return processingRules;
    }

    public boolean isApplicable(final CommonNotificationState commonNotificationState) {
        final boolean isApplicable = processingRules.parallelStream().anyMatch(rule -> {
            return rule.isApplicable(commonNotificationState);
        });
        return isApplicable;
    }

    public void process(final CommonNotificationState commonNotificationState) {
        processingRules.forEach(rule -> {
            if (rule.isApplicable(commonNotificationState)) {
                rule.apply(modelMap, commonNotificationState);
            }
        });
    }

    public List<NotificationModel> getModels() {
        final List<NotificationModel> modelList = modelMap.values().stream().sorted((model1, model2) -> {
            return model2.getCreatedAt().compareTo(model1.getCreatedAt());
        }).collect(Collectors.toList());
        return modelList;
    }
}
