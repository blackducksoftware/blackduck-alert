package com.blackducksoftware.integration.hub.alert.processor.policy;

import java.util.Arrays;

import com.blackducksoftware.integration.hub.alert.processor.NotificationTypeProcessor;

public class PolicyNotificationTypeProcessor extends NotificationTypeProcessor {

    public PolicyNotificationTypeProcessor() {
        super(Arrays.asList(new PolicyViolationRule(), new PolicyViolationClearedRule(), new PolicyViolationOverrideRule()));
    }
}
