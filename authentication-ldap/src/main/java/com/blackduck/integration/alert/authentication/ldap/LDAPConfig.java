/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;

@Configuration
public class LDAPConfig {
    @Bean
    public InetOrgPersonContextMapper ldapUserContextMapper() {
        return new InetOrgPersonContextMapper();
    }

}
