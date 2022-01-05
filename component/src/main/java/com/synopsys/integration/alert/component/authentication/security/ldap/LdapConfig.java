/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.ldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;

@Configuration
public class LdapConfig {
    @Bean
    public InetOrgPersonContextMapper ldapUserContextMapper() {
        return new InetOrgPersonContextMapper();
    }

}
