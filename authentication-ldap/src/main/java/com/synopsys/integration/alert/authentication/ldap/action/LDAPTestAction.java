package com.synopsys.integration.alert.authentication.ldap.action;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
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
    private final LDAPManager ldapManager;
    private final LDAPConfigAccessor ldapConfigAccessor;

    @Autowired
    public LDAPTestAction(
        AuthorizationManager authorizationManager,
        AuthenticationDescriptorKey authenticationDescriptorKey,
        LDAPConfigurationValidator ldapConfigurationValidator,
        LDAPManager ldapManager,
        LDAPConfigAccessor ldapConfigAccessor
    ) {
        this.configurationValidationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
        this.configurationTestHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
        this.ldapConfigurationValidator = ldapConfigurationValidator;
        this.ldapManager = ldapManager;
        this.ldapConfigAccessor = ldapConfigAccessor;
    }

    public ActionResponse<ValidationResponseModel> testAuthentication(LDAPConfigTestModel ldapConfigTestModel) {
        Supplier<ValidationActionResponse> validationSupplier = () -> configurationValidationHelper.validate(() -> ldapConfigurationValidator.validate(ldapConfigTestModel));
        return configurationTestHelper.test(validationSupplier, () -> testConfigModelContent(ldapConfigTestModel));
    }

    protected ConfigurationTestResult testConfigModelContent(LDAPConfigTestModel ldapConfigTestModel) {
        String username = ldapConfigTestModel.getTestLDAPUsername();
        String password = ldapConfigTestModel.getTestLDAPPassword();
        Authentication pendingAuthentication = new UsernamePasswordAuthenticationToken(username, password);

        LDAPConfigModel ldapConfigModel = ldapConfigTestModel.getLDAPConfigModel();
        // If the Password is NOT set and IsManagerPasswordSet is true in the current model, get the PW from the DB
        if (StringUtils.isBlank(ldapConfigModel.getManagerPassword().orElse("")) && Boolean.TRUE.equals(ldapConfigModel.getIsManagerPasswordSet())) {
            ldapConfigAccessor.getConfiguration()
                .flatMap(LDAPConfigModel::getManagerPassword)
                .ifPresent(ldapConfigModel::setManagerPassword);
        }

        // ldapManager.createAuthProvider() requires LDAP to be enabled. Update model that is passed in to do this
        //   so that we don't have to require customers to enable LDAP in the UI prior to testing
        ldapConfigModel.setEnabled(true);

        try {
            Optional<LdapAuthenticationProvider> ldapAuthenticationProvider = ldapManager.createAuthProvider(ldapConfigTestModel.getLDAPConfigModel());
            if (ldapAuthenticationProvider.isEmpty()) {
                return ConfigurationTestResult.failure("LDAP Test Configuration failed. Please check your configuration.");
            } else {
                Authentication authentication = ldapAuthenticationProvider.get().authenticate(pendingAuthentication);
                if (!authentication.isAuthenticated()) {
                    return ConfigurationTestResult.failure(String.format("LDAP authentication failed for test user %s.", username));
                }
                authentication.setAuthenticated(false);
            }
        } catch (Exception ex) {
            return ConfigurationTestResult.failure("LDAP Test Configuration failed. " + ex.getMessage());
        }
        return ConfigurationTestResult.success("LDAP Test Configuration successful.");
    }
}
