package com.blackducksoftware.integration.hub.notification.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.processor.SubProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public class PolicyOverrideProcessor extends PolicyViolationProcessor {

    public PolicyOverrideProcessor(final SubProcessorCache cache, final MetaService metaService) {
        super(cache, metaService);
    }

    @Override
    public void process(final NotificationContentItem notification) throws HubIntegrationException {
        final PolicyOverrideContentItem policyOverrideContentItem = (PolicyOverrideContentItem) notification;
        final Map<String, Object> dataMap = new HashMap<>();
        for (final PolicyRuleView rule : policyOverrideContentItem.getPolicyRuleList()) {
            dataMap.put(POLICY_CONTENT_ITEM, policyOverrideContentItem);
            dataMap.put(POLICY_RULE, rule);
            final String eventKey = generateEventKey(dataMap);
            final Map<String, Object> dataSet = generateDataSet(dataMap);
            final NotificationEvent event = new NotificationEvent(eventKey, NotificationCategoryEnum.POLICY_VIOLATION, dataSet);
            if (getCache().hasEvent(event.getEventKey())) {
                getCache().removeEvent(event);
            } else {
                event.setCategoryType(NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE);
                getCache().addEvent(event);
            }
        }
    }

    @Override
    public Map<String, Object> generateDataSet(final Map<String, Object> inputData) {
        final PolicyOverrideContentItem policyOverride = (PolicyOverrideContentItem) inputData.get(POLICY_CONTENT_ITEM);
        final Map<String, Object> dataSet = super.generateDataSet(inputData);
        final String person = StringUtils.join(" ", policyOverride.getFirstName(), policyOverride.getLastName());
        dataSet.put(ItemTypeEnum.PERSON.name(), person);
        return dataSet;
    }

}
