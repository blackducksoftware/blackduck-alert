/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.common;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LdapProperties {
    @Value("${alert.ldap.enabled:}")
    private String enabled;
    @Value("${alert.ldap.server:}")
    private String server;
    @Value("${alert.ldap.manager.dn:}")
    private String managerDN;
    @Value("${alert.ldap.manager.password:}")
    private String managerPassword;

    @Value("${alert.ldap.authentication.type:}")
    private String authenticationType;
    @Value("${alert.ldap.referral:}")
    private String ldapReferral;

    @Value("${alert.ldap.user.search.base:}")
    private String userSearchBase;
    @Value("${alert.ldap.user.search.filter:}")
    private String userSearchFilter;
    @Value("${alert.ldap.user.dn.patterns:}")
    private String userDNPatterns;
    @Value("${alert.ldap.user.attributes:}")
    private String userAttributes;

    @Value("${alert.ldap.group.search.base:}")
    private String groupSearchBase;
    @Value("${alert.ldap.group.search.filter:}")
    private String groupSearchFilter;
    @Value("${alert.ldap.group.role.attribute:}")
    private String groupRoleAttribute;
    @Value("${alert.ldap.role.prefix:}")
    private String rolePrefix;

    private String[] createSetFromCSV(final String commaSeparatedString) {
        return StringUtils.split(commaSeparatedString, ",");
    }

    public String[] getUserDNPatternArray() {
        return createSetFromCSV(userDNPatterns);
    }

    public String[] getUserAttributeArray() {
        return createSetFromCSV(userAttributes);
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(getEnabled());
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(final String enabled) {
        this.enabled = enabled;
    }

    public String getServer() {
        return server;
    }

    public void setServer(final String server) {
        this.server = server;
    }

    public String getManagerDN() {
        return managerDN;
    }

    public void setManagerDN(final String managerDN) {
        this.managerDN = managerDN;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public void setManagerPassword(final String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(final String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getLdapReferral() {
        return ldapReferral;
    }

    public void setLdapReferral(final String ldapReferral) {
        this.ldapReferral = ldapReferral;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public void setUserSearchBase(final String userSearchBase) {
        this.userSearchBase = userSearchBase;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(final String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public String getUserDNPatterns() {
        return userDNPatterns;
    }

    public void setUserDNPatterns(final String userDNPatterns) {
        this.userDNPatterns = userDNPatterns;
    }

    public String getUserAttributes() {
        return userAttributes;
    }

    public void setUserAttributes(final String userAttributes) {
        this.userAttributes = userAttributes;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public void setGroupSearchBase(final String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(final String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }

    public void setGroupRoleAttribute(final String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public String getRolePrefix() {
        return rolePrefix;
    }

    public void setRolePrefix(final String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }
}
