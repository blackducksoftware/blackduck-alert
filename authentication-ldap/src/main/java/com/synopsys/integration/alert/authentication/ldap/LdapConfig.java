package com.synopsys.integration.alert.authentication.ldap;

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
