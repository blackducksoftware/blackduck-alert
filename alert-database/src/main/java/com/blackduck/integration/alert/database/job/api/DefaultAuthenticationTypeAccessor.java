/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.common.enumeration.AuthenticationType;
import com.blackduck.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.blackduck.integration.alert.common.persistence.model.AuthenticationTypeDetails;
import com.blackduck.integration.alert.database.user.AuthenticationTypeEntity;
import com.blackduck.integration.alert.database.user.AuthenticationTypeRepository;

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
