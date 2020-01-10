/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
