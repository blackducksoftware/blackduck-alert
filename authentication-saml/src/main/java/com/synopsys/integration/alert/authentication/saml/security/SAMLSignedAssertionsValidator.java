package com.synopsys.integration.alert.authentication.saml.security;

import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import org.apache.commons.lang3.BooleanUtils;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SAMLSignedAssertionsValidator {
    private final SAMLConfigAccessor configAccessor;
    private final AlertRelyingPartyRegistrationRepository alertRelyingPartyRegistrationRepository;

    public SAMLSignedAssertionsValidator(SAMLConfigAccessor configAccessor, AlertRelyingPartyRegistrationRepository alertRelyingPartyRegistrationRepository) {
        this.configAccessor = configAccessor;
        this.alertRelyingPartyRegistrationRepository = alertRelyingPartyRegistrationRepository;
    }

    public ValidationResult validateSignedAssertions(OpenSaml4AuthenticationProvider.AssertionToken assertionToken) {
        Optional<SAMLConfigModel> optionalSAMLConfigModel = configAccessor.getConfiguration();

        if (optionalSAMLConfigModel.isPresent()) {
            boolean wantAssertionsSigned = BooleanUtils.toBoolean(optionalSAMLConfigModel.get().getWantAssertionsSigned());
            String alertRegisteredAssertingPartyId = alertRelyingPartyRegistrationRepository.findByRegistrationId("default").getAssertingPartyDetails().getEntityId();

            if (wantAssertionsSigned
                && !(assertionToken.getAssertion().isSigned()
                     && alertRegisteredAssertingPartyId.equals(assertionToken.getAssertion().getIssuer().getValue()))
            ) {
                return ValidationResult.INVALID;
            }
        }

        return ValidationResult.VALID;
    }
}
