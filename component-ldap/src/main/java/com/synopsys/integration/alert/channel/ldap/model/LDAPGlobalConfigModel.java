package com.synopsys.integration.alert.channel.ldap.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

public class LDAPGlobalConfigModel extends ConfigWithMetadata implements Obfuscated<LDAPGlobalConfigModel> {
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

    public LDAPGlobalConfigModel() {
        // For serialization
    }

    // Required
    public LDAPGlobalConfigModel(String id, String name) {
        super(id, name);
    }

    public LDAPGlobalConfigModel(
        String id,
        String name,
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
        this(id, name);
        this.enabled = enabled;
        this.serverName = serverName;
        this.managerDn = managerDn;
        this.managerPassword = managerPassword;
        this.isManagerPasswordSet = isManagerPasswordSet;
        this.authenticationType = authenticationType;
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
    public LDAPGlobalConfigModel obfuscate() {
        return new LDAPGlobalConfigModel(
            getId(),
            getName(),
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
        return enabled;
    }

    public String getServerName() {
        return serverName;
    }

    public String getManagerDn() {
        return managerDn;
    }

    public Optional<String> getManagerPassword() {
        return Optional.ofNullable(managerPassword);
    }

    public Optional<Boolean> getIsManagerPasswordSet() {
        return Optional.ofNullable(isManagerPasswordSet);
    }

    public void setIsManagerPasswordSet(final Boolean managerPasswordSet) {
        isManagerPasswordSet = managerPasswordSet;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public String getReferral() {
        return referral;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public String getUserDnPatterns() {
        return userDnPatterns;
    }

    public String getUserAttributes() {
        return userAttributes;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }
}
