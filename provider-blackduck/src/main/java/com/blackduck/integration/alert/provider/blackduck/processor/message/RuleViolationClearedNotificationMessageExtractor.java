package com.blackduck.integration.alert.provider.blackduck.processor.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicyComponentConcernCreator;
import com.blackduck.integration.alert.provider.blackduck.processor.model.RuleViolationClearedUniquePolicyNotificationContent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class RuleViolationClearedNotificationMessageExtractor extends AbstractRuleViolationNotificationMessageExtractor<RuleViolationClearedUniquePolicyNotificationContent> {
    @Autowired
    public RuleViolationClearedNotificationMessageExtractor(
        BlackDuckProviderKey blackDuckProviderKey,
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache,
        BlackDuckPolicyComponentConcernCreator policyComponentConcernCreator,
        BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory,
        BomComponent404Handler bomComponent404Handler
    ) {
        super(NotificationType.RULE_VIOLATION_CLEARED, RuleViolationClearedUniquePolicyNotificationContent.class, ItemOperation.DELETE, blackDuckProviderKey, servicesFactoryCache, policyComponentConcernCreator, detailsCreatorFactory,
            bomComponent404Handler);
    }

}
