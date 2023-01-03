package com.synopsys.integration.alert.authentication.ldap.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
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

    public LDAPGlobalConfigModel(String id, String serverName, String managerDn, String managerPassword) {
        super(id, AlertRestConstants.DEFAULT_CONFIGURATION_NAME);

        this.serverName = serverName;
        this.managerDn = managerDn;
        this.managerPassword = managerPassword;
    }

    public LDAPGlobalConfigModel(
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

    public void setIsManagerPasswordSet(Boolean managerPasswordSet) {
        isManagerPasswordSet = managerPasswordSet;
    }

    public Optional<String> getAuthenticationType() {
        return Optional.ofNullable(authenticationType);
    }

    public Optional<String> getReferral() {
        return Optional.ofNullable(referral);
    }

    public Optional<String> getUserSearchBase() {
        return Optional.ofNullable(userSearchBase);
    }

    public Optional<String> getUserSearchFilter() {
        return Optional.ofNullable(userSearchFilter);
    }

    public Optional<String> getUserDnPatterns() {
        return Optional.ofNullable(userDnPatterns);
    }

    public Optional<String> getUserAttributes() {
        return Optional.ofNullable(userAttributes);
    }

    public Optional<String> getGroupSearchBase() {
        return Optional.ofNullable(groupSearchBase);
    }

    public Optional<String> getGroupSearchFilter() {
        return Optional.ofNullable(groupSearchFilter);
    }

    public Optional<String> getGroupRoleAttribute() {
        return Optional.ofNullable(groupRoleAttribute);
    }
}
