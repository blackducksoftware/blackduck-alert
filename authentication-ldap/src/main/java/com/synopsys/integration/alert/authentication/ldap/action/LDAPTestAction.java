package com.synopsys.integration.alert.authentication.ldap.action;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigTestModel;
import com.synopsys.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
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
        Supplier<ValidationActionResponse> validationSupplier = () -> configurationValidationHelper.validate(() -> ldapConfigurationValidator.validate(ldapConfigTestModel.getLdapConfigModel()));
        return configurationTestHelper.test(validationSupplier, () -> testConfigModelContent(ldapConfigTestModel));
    }

    protected ConfigurationTestResult testConfigModelContent(LDAPConfigTestModel ldapConfigTestModel) {
        String username = ldapConfigTestModel.getTestLDAPUsername();
        String password = ldapConfigTestModel.getTestLDAPPassword();
        Authentication pendingAuthentication = new UsernamePasswordAuthenticationToken(username, password);

        try {
            Optional<LdapAuthenticationProvider> ldapAuthenticationProvider = ldapManager.createAuthProvider(ldapConfigTestModel.getLdapConfigModel());
            if (ldapAuthenticationProvider.isEmpty()) {
                return ConfigurationTestResult.failure("LDAP Test Configuration failed. Please check your configuration.");
            } else {
                Authentication authentication = ldapAuthenticationProvider.get().authenticate(pendingAuthentication);
                if (!authentication.isAuthenticated()) {
                    return ConfigurationTestResult.failure(String.format("LDAP authentication failed for test user %s.", username));
                }
                authentication.setAuthenticated(false);
            }
        } catch (AlertConfigurationException ex) {
            return ConfigurationTestResult.failure("LDAP Test Configuration failed." + ex.getMessage());
        }
        return ConfigurationTestResult.success("LDAP Test Configuration successful.");
    }
}
