package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Optional;

import com.synopsys.integration.alert.common.persistence.model.AuthenticationType;

public interface AuthenticationTypeAccessor {
    Optional<AuthenticationType> getAuthenticationType(Long id);
}
