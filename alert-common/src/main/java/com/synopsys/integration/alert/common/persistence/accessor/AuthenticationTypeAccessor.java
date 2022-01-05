/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Optional;

import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationTypeDetails;

public interface AuthenticationTypeAccessor {
    Optional<AuthenticationTypeDetails> getAuthenticationTypeDetails(AuthenticationType authenticationType);

    Optional<AuthenticationType> getAuthenticationType(Long id);
}
