/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Optional;

import com.blackduck.integration.alert.common.enumeration.AuthenticationType;
import com.blackduck.integration.alert.common.persistence.model.AuthenticationTypeDetails;

public interface AuthenticationTypeAccessor {
    Optional<AuthenticationTypeDetails> getAuthenticationTypeDetails(AuthenticationType authenticationType);

    Optional<AuthenticationType> getAuthenticationType(Long id);
}
