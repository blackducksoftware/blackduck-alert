package com.blackducksoftware.integration.hub.alert.processor.policy;

import java.util.Map;

import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.processor.NotificationProcessingRule;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;

public class PolicyViolationClearedRule extends NotificationProcessingRule {

    public PolicyViolationClearedRule() {
        super(NotificationType.RULE_VIOLATION_CLEARED);
    }

    @Override
    public void apply(final Map<String, NotificationModel> modelMap, final CommonNotificationState commonNotificationState) {

    }

}
