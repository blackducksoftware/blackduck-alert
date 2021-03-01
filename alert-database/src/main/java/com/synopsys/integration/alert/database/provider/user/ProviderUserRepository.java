/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.provider.user;

import java.util.Collection;
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

    List<ProviderUserEntity> findByEmailAddressInAndProviderConfigId(Collection<String> emailAddresses, Long providerConfigId);
}
