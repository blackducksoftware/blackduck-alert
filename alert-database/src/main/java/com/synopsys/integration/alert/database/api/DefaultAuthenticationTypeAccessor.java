/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationTypeDetails;
import com.synopsys.integration.alert.database.user.AuthenticationTypeEntity;
import com.synopsys.integration.alert.database.user.AuthenticationTypeRepository;

@Component
@Transactional
public class DefaultAuthenticationTypeAccessor implements AuthenticationTypeAccessor {
    private final AuthenticationTypeRepository authenticationTypeRepository;

    @Autowired
    public DefaultAuthenticationTypeAccessor(AuthenticationTypeRepository authenticationTypeRepository) {
        this.authenticationTypeRepository = authenticationTypeRepository;
    }

    @Override
    public Optional<AuthenticationTypeDetails> getAuthenticationTypeDetails(AuthenticationType authenticationType) {
        Optional<AuthenticationTypeEntity> authenticationTypeEntity = authenticationTypeRepository.findById(authenticationType.getId());
        return authenticationTypeEntity.map(entity -> new AuthenticationTypeDetails(entity.getId(), entity.getName()));
    }

    @Override
    public Optional<AuthenticationType> getAuthenticationType(Long id) {
        return AuthenticationType.getById(id);
    }
}
