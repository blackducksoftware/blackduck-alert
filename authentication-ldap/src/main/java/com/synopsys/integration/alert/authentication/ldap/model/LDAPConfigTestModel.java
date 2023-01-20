package com.synopsys.integration.alert.authentication.ldap.model;

public class LDAPConfigTestModel {
    private final LDAPConfigModel ldapConfigModel;
    private final String testLDAPUsername;
    private final String testLDAPPassword;

    public LDAPConfigTestModel(LDAPConfigModel ldapConfigModel, String testLDAPUsername, String testLDAPPassword) {
        this.ldapConfigModel = ldapConfigModel;
        this.testLDAPUsername = testLDAPUsername;
        this.testLDAPPassword = testLDAPPassword;
    }

    public LDAPConfigModel getLdapConfigModel() {
        return ldapConfigModel;
    }

    public String getTestLDAPUsername() {
        return testLDAPUsername;
    }

    public String getTestLDAPPassword() {
        return testLDAPPassword;
    }
}
