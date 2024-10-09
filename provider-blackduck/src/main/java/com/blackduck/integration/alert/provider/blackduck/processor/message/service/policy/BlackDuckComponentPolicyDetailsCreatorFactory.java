package com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.blackduck.service.BlackDuckApiClient;

@Component
public class BlackDuckComponentPolicyDetailsCreatorFactory {
    private final BlackDuckPolicySeverityConverter policySeverityConverter;

    @Autowired
    public BlackDuckComponentPolicyDetailsCreatorFactory(BlackDuckPolicySeverityConverter policySeverityConverter) {
        this.policySeverityConverter = policySeverityConverter;
    }

    public BlackDuckComponentPolicyDetailsCreator createBlackDuckComponentPolicyDetailsCreator(BlackDuckApiClient blackDuckApiClient) {
        return new BlackDuckComponentPolicyDetailsCreator(policySeverityConverter, blackDuckApiClient);
    }
}
