package com.synopsys.integration.alert.authentication.ldap.action;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigTestModel;
import com.synopsys.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.rest.api.ConfigurationTestHelper;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class LDAPTestAction {
    private final ConfigurationValidationHelper configurationValidationHelper;
    private final ConfigurationTestHelper configurationTestHelper;
    private final LDAPConfigurationValidator ldapConfigurationValidator;
    private final LdapManager ldapManager;

    @Autowired
    public LDAPTestAction(
        AuthorizationManager authorizationManager,
        AuthenticationDescriptorKey authenticationDescriptorKey,
        LDAPConfigurationValidator ldapConfigurationValidator,
        LdapManager ldapManager
    ) {
        this.configurationValidationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
        this.configurationTestHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
        this.ldapConfigurationValidator = ldapConfigurationValidator;
        this.ldapManager = ldapManager;
    }

    public ActionResponse<ValidationResponseModel> testAuthentication(LDAPConfigTestModel ldapConfigTestModel) {
        testConfigModelContent(ldapConfigTestModel);
        return null;
    }

    protected ConfigurationTestResult testConfigModelContent(LDAPConfigTestModel ldapConfigTestModel) {
        try {
            Optional<LdapAuthenticationProvider> ldapProvider = ldapManager.createAuthProvider(ldapConfigTestModel.getLdapConfigModel());
        } catch (AlertConfigurationException ex) {
            //
        }
        return null;
    }
}
