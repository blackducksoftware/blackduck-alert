package com.synopsys.integration.alert.authentication.saml.security;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;

@Component
public class SAMLSignedAssertionsValidator {
    private final SAMLConfigAccessor configAccessor;

    public SAMLSignedAssertionsValidator(SAMLConfigAccessor configAccessor, AlertRelyingPartyRegistrationRepository alertRelyingPartyRegistrationRepository) {
        this.configAccessor = configAccessor;
    }

    public ValidationResult validateSignedAssertions(OpenSaml4AuthenticationProvider.AssertionToken assertionToken) {
        Optional<SAMLConfigModel> optionalSAMLConfigModel = configAccessor.getConfiguration();

        if (optionalSAMLConfigModel.isPresent()) {
            boolean wantAssertionsSigned = BooleanUtils.toBoolean(optionalSAMLConfigModel.get().getWantAssertionsSigned());
            // Force assertions to be signed since some APs have to option of signing their response but not assertions
            // See https://github.com/spring-projects/spring-security/issues/10844
            if (wantAssertionsSigned && !assertionToken.getAssertion().isSigned()) {
                return ValidationResult.INVALID;
            }
        }

        return ValidationResult.VALID;
    }
}
