package com.blackducksoftware.integration.hub.alert.processor;

import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationSubProcessor;
import com.blackducksoftware.integration.hub.notification.processor.SubProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEventConstants;

public class PolicyViolationProcessor extends NotificationSubProcessor {

    public final static String POLICY_CONTENT_ITEM = "policyContentItem";

    public final static String POLICY_RULE = "policyRule";

    public PolicyViolationProcessor(final SubProcessorCache cache, final MetaService metaService) {
        super(cache, metaService);
    }

    @Override
    public void process(final NotificationContentItem notification) throws HubIntegrationException {
        if (notification instanceof PolicyViolationContentItem) {
            final PolicyViolationContentItem policyViolationContentItem = (PolicyViolationContentItem) notification;
            final Map<String, Object> dataMap = new HashMap<>();
            for (final PolicyRuleView rule : policyViolationContentItem.getPolicyRuleList()) {
                dataMap.put(POLICY_CONTENT_ITEM, policyViolationContentItem);
                dataMap.put(POLICY_RULE, rule);
                final String eventKey = generateEventKey(dataMap);
                final Map<String, Object> dataSet = generateDataSet(dataMap);
                final NotificationEvent event = new NotificationEvent(eventKey, NotificationCategoryEnum.POLICY_VIOLATION, dataSet);
                getCache().addEvent(event);
            }
        }
    }

    @Override
    public String generateEventKey(final Map<String, Object> dataMap) throws HubIntegrationException {
        final PolicyViolationContentItem content = (PolicyViolationContentItem) dataMap.get(POLICY_CONTENT_ITEM);
        final PolicyRuleView rule = (PolicyRuleView) dataMap.get(POLICY_RULE);
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_ISSUE_TYPE_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_ISSUE_TYPE_VALUE_POLICY);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_PROJECT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(content.getProjectVersion().getUrl()));
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_COMPONENT_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(content.getComponentUrl()));
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_COMPONENT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(content.getComponentVersionUrl()));
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_POLICY_RULE_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(getMetaService().getHref(rule)));
        final String key = keyBuilder.toString();
        return key;
    }

    @Override
    public Map<String, Object> generateDataSet(final Map<String, Object> inputData) {
        final Map<String, Object> dataSet = new HashMap<>();
        final PolicyViolationContentItem policyViolationContentItem = (PolicyViolationContentItem) inputData.get(POLICY_CONTENT_ITEM);
        final PolicyRuleView rule = (PolicyRuleView) inputData.get(POLICY_RULE);

        dataSet.put(ItemTypeEnum.RULE.name(), rule.name);
        dataSet.put(ItemTypeEnum.COMPONENT.name(), policyViolationContentItem.getComponentName());
        dataSet.put(ItemTypeEnum.VERSION.name(), policyViolationContentItem.getComponentVersion().versionName);
        dataSet.put(NotificationEvent.DATA_SET_KEY_NOTIFICATION_CONTENT, policyViolationContentItem);
        return dataSet;
    }

}
