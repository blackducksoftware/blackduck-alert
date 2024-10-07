package com.blackduck.integration.alert.authentication.ldap.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

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
