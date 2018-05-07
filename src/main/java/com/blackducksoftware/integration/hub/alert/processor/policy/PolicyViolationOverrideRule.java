package com.blackducksoftware.integration.hub.alert.processor.policy;

import java.util.Map;

import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.processor.NotificationProcessingRule;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;

public class PolicyViolationOverrideRule extends NotificationProcessingRule {

    public PolicyViolationOverrideRule() {
        super(NotificationType.POLICY_OVERRIDE);
    }

    @Override
    public void apply(final Map<String, NotificationModel> modelMap, final CommonNotificationState commonNotificationState) {

    }
}
