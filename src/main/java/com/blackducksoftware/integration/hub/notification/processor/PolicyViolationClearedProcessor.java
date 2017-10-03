package com.blackducksoftware.integration.hub.notification.processor;

import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.processor.SubProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public class PolicyViolationClearedProcessor extends PolicyViolationProcessor {

    public PolicyViolationClearedProcessor(final SubProcessorCache cache, final MetaService metaService) {
        super(cache, metaService);
    }

    @Override
    public void process(final NotificationContentItem notification) throws HubIntegrationException {
        if (notification instanceof PolicyViolationClearedContentItem) {
            final PolicyViolationClearedContentItem policyViolationCleared = (PolicyViolationClearedContentItem) notification;
            final Map<String, Object> dataMap = new HashMap<>();
            for (final PolicyRuleView rule : policyViolationCleared.getPolicyRuleList()) {
                dataMap.put(POLICY_CONTENT_ITEM, policyViolationCleared);
                dataMap.put(POLICY_RULE, rule);
                final String eventKey = generateEventKey(dataMap);
                final Map<String, Object> dataSet = generateDataSet(dataMap);
                final NotificationEvent event = new NotificationEvent(eventKey, NotificationCategoryEnum.POLICY_VIOLATION, dataSet);
                if (getCache().hasEvent(event.getEventKey())) {
                    getCache().removeEvent(event);
                } else {
                    event.setCategoryType(NotificationCategoryEnum.POLICY_VIOLATION_CLEARED);
                    getCache().addEvent(event);
                }
            }
        }
    }
}
