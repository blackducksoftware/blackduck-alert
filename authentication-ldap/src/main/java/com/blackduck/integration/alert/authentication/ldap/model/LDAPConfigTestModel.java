/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LDAPConfigTestModel extends AlertSerializableModel {
    private static final long serialVersionUID = 6421872579523951087L;

    @JsonProperty("ldapConfigModel")
    private LDAPConfigModel ldapConfigModel;
    private String testLDAPUsername;
    private String testLDAPPassword;

    public LDAPConfigTestModel() {
        // For serialization
    }

    public LDAPConfigTestModel(LDAPConfigModel ldapConfigModel, String testLDAPUsername, String testLDAPPassword) {
        this.ldapConfigModel = ldapConfigModel;
        this.testLDAPUsername = testLDAPUsername;
        this.testLDAPPassword = testLDAPPassword;
    }

    public LDAPConfigModel getLDAPConfigModel() {
        return ldapConfigModel;
    }

    public String getTestLDAPUsername() {
        return testLDAPUsername;
    }

    public String getTestLDAPPassword() {
        return testLDAPPassword;
    }
}
