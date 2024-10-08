package com.synopsys.integration.alert.authentication.ldap.model;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

public class LDAPConfigModel extends ConfigWithMetadata implements Obfuscated<LDAPConfigModel> {
    private static final long serialVersionUID = -3340739050525348445L;

    private Boolean enabled;
    private String serverName;
    private String managerDn;
    private String managerPassword;
    private Boolean isManagerPasswordSet;
    private String authenticationType;
    private String referral;
    private String userSearchBase;
    private String userSearchFilter;
    private String userDnPatterns;
    private String userAttributes;
    private String groupSearchBase;
    private String groupSearchFilter;
    private String groupRoleAttribute;

    public LDAPConfigModel() {
        // For serialization
    }

    public LDAPConfigModel(String id, String serverName, String managerDn, String managerPassword) {
        super(id, AlertRestConstants.DEFAULT_CONFIGURATION_NAME);

        this.serverName = serverName;
        this.managerDn = managerDn;
        this.managerPassword = managerPassword;
    }

    public LDAPConfigModel(
        String id,
        String createdAt,
        String lastUpdated,
        Boolean enabled,
        String serverName,
        String managerDn,
        String managerPassword,
        Boolean isManagerPasswordSet,
        String authenticationType,
        String referral,
        String userSearchBase,
        String userSearchFilter,
        String userDnPatterns,
        String userAttributes,
        String groupSearchBase,
        String groupSearchFilter,
        String groupRoleAttribute
    ) {
        this(id, serverName, managerDn, managerPassword);
        this.enabled = enabled;
        this.isManagerPasswordSet = isManagerPasswordSet;
        this.authenticationType = LDAPAuthenticationType.fromString(authenticationType);
        this.referral = referral;
        this.userSearchBase = userSearchBase;
        this.userSearchFilter = userSearchFilter;
        this.userDnPatterns = userDnPatterns;
        this.userAttributes = userAttributes;
        this.groupSearchBase = groupSearchBase;
        this.groupSearchFilter = groupSearchFilter;
        this.groupRoleAttribute = groupRoleAttribute;

        setCreatedAt(createdAt);
        setLastUpdated(lastUpdated);
    }

    @Override
    public LDAPConfigModel obfuscate() {
        return new LDAPConfigModel(
            getId(),
            getCreatedAt(),
            getLastUpdated(),
            enabled,
            serverName,
            managerDn,
            null,
            StringUtils.isNotBlank(managerPassword),
            authenticationType,
            referral,
            userSearchBase,
            userSearchFilter,
            userDnPatterns,
            userAttributes,
            groupSearchBase,
            groupSearchFilter,
            groupRoleAttribute
        );
    }

    public Boolean getEnabled() {
        return BooleanUtils.toBoolean(enabled);
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getManagerDn() {
        return managerDn;
    }

    public void setManagerDn(String managerDn) {
        this.managerDn = managerDn;
    }

    public Optional<String> getManagerPassword() {
        return Optional.ofNullable(managerPassword);
    }

    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public Boolean getIsManagerPasswordSet() {
        return BooleanUtils.toBoolean(isManagerPasswordSet);
    }

    public void setIsManagerPasswordSet(Boolean managerPasswordSet) {
        isManagerPasswordSet = managerPasswordSet;
    }

    public Optional<String> getAuthenticationType() {
        return Optional.ofNullable(authenticationType);
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = LDAPAuthenticationType.fromString(authenticationType);
    }

    public Optional<String> getReferral() {
        return Optional.ofNullable(referral);
    }

    public void setReferral(String referral) {
        this.referral = referral;
    }

    public Optional<String> getUserSearchBase() {
        return Optional.ofNullable(userSearchBase);
    }

    public void setUserSearchBase(String userSearchBase) {
        this.userSearchBase = userSearchBase;
    }

    public Optional<String> getUserSearchFilter() {
        return Optional.ofNullable(userSearchFilter);
    }

    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public Optional<String> getUserDnPatterns() {
        return Optional.ofNullable(userDnPatterns);
    }

    public void setUserDnPatterns(String userDnPatterns) {
        this.userDnPatterns = userDnPatterns;
    }

    public Optional<String> getUserAttributes() {
        return Optional.ofNullable(userAttributes);
    }

    public void setUserAttributes(String userAttributes) {
        this.userAttributes = userAttributes;
    }

    public Optional<String> getGroupSearchBase() {
        return Optional.ofNullable(groupSearchBase);
    }

    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public Optional<String> getGroupSearchFilter() {
        return Optional.ofNullable(groupSearchFilter);
    }

    public void setGroupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public Optional<String> getGroupRoleAttribute() {
        return Optional.ofNullable(groupRoleAttribute);
    }

    public void setGroupRoleAttribute(String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
    }

}
