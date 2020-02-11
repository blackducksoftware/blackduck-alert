package com.synopsys.integration.alert.provider.blackduck.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckValidator;

@Component
public class BlackDuckValidatorFactory {
    private final AlertProperties alertProperties;
    private final SystemMessageUtility systemMessageUtility;

    @Autowired
    public BlackDuckValidatorFactory(AlertProperties alertProperties, SystemMessageUtility systemMessageUtility) {
        this.alertProperties = alertProperties;
        this.systemMessageUtility = systemMessageUtility;
    }

    public BlackDuckValidator createValidator(BlackDuckProperties properties) {
        return new BlackDuckValidator(alertProperties, properties, systemMessageUtility);
    }
}
