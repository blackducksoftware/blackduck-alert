/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckDistributionConfigurationValidator;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckGlobalConfigurationFieldModelValidator;

@Component
public class BlackDuckDescriptor extends ProviderDescriptor {
    public static final String KEY_BLACKDUCK_URL = "blackduck.url";
    public static final String KEY_BLACKDUCK_API_KEY = "blackduck.api.key";
    public static final String KEY_BLACKDUCK_TIMEOUT = "blackduck.timeout";
    public static final String KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER = "blackduck.policy.notification.filter";
    public static final String KEY_BLACKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER = "blackduck.vulnerability.notification.filter";

    public static final String BLACKDUCK_LABEL = "Black Duck";
    public static final String BLACKDUCK_URL = "blackduck";
    public static final String BLACKDUCK_DESCRIPTION = "This is the configuration to connect to the Black Duck server. Configuring this will cause Alert to start pulling data from Black Duck.";

    public static final String LABEL_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER = "Policy Notification Type Filter";
    public static final String LABEL_BLACKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER = "Vulnerability Notification Type Filter";

    private final BlackDuckGlobalConfigurationFieldModelValidator globalValidator;
    private final DistributionConfigurationValidator distributionValidator;

    @Autowired
    public BlackDuckDescriptor(BlackDuckProviderKey blackDuckProviderKey, BlackDuckGlobalConfigurationFieldModelValidator globalValidator, BlackDuckDistributionConfigurationValidator distributionValidator) {
        super(blackDuckProviderKey);
        this.globalValidator = globalValidator;
        this.distributionValidator = distributionValidator;
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.of(globalValidator);
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.of(distributionValidator);
    }

}
