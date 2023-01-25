package com.synopsys.integration.alert.authentication.ldap.database.configuration;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "configuration_ldap")
public class LDAPConfigurationEntity extends BaseEntity {
    @Id
    @Column(name = "configuration_id")
    private UUID configurationId;
    @Column(name = "name")
    private String name;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;
    @Column(name = "enabled")
    private Boolean enabled;
    @Column(name = "server_name")
    private String serverName;
    @Column(name = "manager_dn")
    private String managerDn;
    @Column(name = "manager_password")
    private String managerPassword;
    @Column(name = "authentication_type")
    private String authenticationType;
    @Column(name = "referral")
    private String referral;
    @Column(name = "user_search_base")
    private String userSearchBase;
    @Column(name = "user_search_filter")
    private String userSearchFilter;
    @Column(name = "user_dn_patterns")
    private String userDnPatterns;
    @Column(name = "user_attributes")
    private String userAttributes;
    @Column(name = "group_search_base")
    private String groupSearchBase;
    @Column(name = "group_search_filter")
    private String groupSearchFilter;
    @Column(name = "group_role_attribute")
    private String groupRoleAttribute;

    public LDAPConfigurationEntity() {
    }

    public LDAPConfigurationEntity(
        UUID configurationId,
        OffsetDateTime createdAt,
        OffsetDateTime lastUpdated,
        Boolean enabled,
        String serverName,
        String managerDn,
        String managerPassword,
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
        this.configurationId = configurationId;
        this.name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.enabled = enabled;
        this.serverName = serverName;
        this.managerDn = managerDn;
        this.managerPassword = managerPassword;
        this.authenticationType = authenticationType;
        this.referral = referral;
        this.userSearchBase = userSearchBase;
        this.userSearchFilter = userSearchFilter;
        this.userDnPatterns = userDnPatterns;
        this.userAttributes = userAttributes;
        this.groupSearchBase = groupSearchBase;
        this.groupSearchFilter = groupSearchFilter;
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
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

    public String getManagerPassword() {
        return managerPassword;
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
