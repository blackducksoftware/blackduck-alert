package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Optional;

import com.blackduck.integration.alert.common.enumeration.AuthenticationType;
import com.blackduck.integration.alert.common.persistence.model.AuthenticationTypeDetails;

public interface AuthenticationTypeAccessor {
    Optional<AuthenticationTypeDetails> getAuthenticationTypeDetails(AuthenticationType authenticationType);

    Optional<AuthenticationType> getAuthenticationType(Long id);
}
