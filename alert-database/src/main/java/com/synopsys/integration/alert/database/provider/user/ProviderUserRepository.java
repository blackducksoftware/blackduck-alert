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
package com.synopsys.integration.alert.database.provider.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProviderUserRepository extends JpaRepository<ProviderUserEntity, Long> {
    List<ProviderUserEntity> findByProviderConfigId(Long providerConfigId);

    @Query(value = "SELECT DISTINCT new ProviderUserEntity(providerUser.emailAddress, providerUser.optOut, providerUser.providerConfigId) "
                       + "FROM ProviderUserEntity providerUser "
                       + "WHERE providerUser.providerConfigId = :providerConfigId "
                       + "AND providerUser.emailAddress LIKE %:emailSearchTerm%")
    Page<ProviderUserEntity> findPageOfUsersByProviderAndEmailSearchTerm(@Param("provider") Long providerConfigId, @Param("emailSearchTerm") String emailSearchTerm, Pageable pageable);

    List<ProviderUserEntity> findByEmailAddressAndProviderConfigId(String emailAddress, Long providerConfigId);

    void deleteByProviderConfigIdAndEmailAddress(Long providerConfigId, String emailAddress);
}
