package com.synopsys.integration.alert.authentication.ldap.environment;

import com.blackduck.integration.alert.api.common.model.AlertConstants;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.api.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.api.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.synopsys.integration.alert.common.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LDAPEnvironmentVariableHandler extends EnvironmentVariableHandler<LDAPConfigModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String HANDLER_NAME = "LDAP Settings";
    public static final String ENVIRONMENT_VARIABLE_PREFIX = "ALERT_COMPONENT_AUTHENTICATION_SETTINGS_LDAP_";

    public static final String LDAP_AUTHENTICATION_TYPE_KEY = ENVIRONMENT_VARIABLE_PREFIX + "AUTHENTICATION_TYPE";
    public static final String LDAP_ENABLED_KEY = ENVIRONMENT_VARIABLE_PREFIX + "ENABLED";
    public static final String LDAP_GROUP_ROLE_ATTRIBUTE_KEY = ENVIRONMENT_VARIABLE_PREFIX + "GROUP_ROLE_ATTRIBUTE";
    public static final String LDAP_GROUP_SEARCH_BASE_KEY = ENVIRONMENT_VARIABLE_PREFIX + "GROUP_SEARCH_BASE";
    public static final String LDAP_GROUP_SEARCH_FILTER_KEY = ENVIRONMENT_VARIABLE_PREFIX + "GROUP_SEARCH_FILTER";
    public static final String LDAP_MANAGER_DN_KEY = ENVIRONMENT_VARIABLE_PREFIX + "MANAGER_DN";
    public static final String LDAP_MANAGER_PASSWORD_KEY = ENVIRONMENT_VARIABLE_PREFIX + "MANAGER_PASSWORD";
    public static final String LDAP_REFERRAL_KEY = ENVIRONMENT_VARIABLE_PREFIX + "REFERRAL";
    public static final String LDAP_SERVER_KEY = ENVIRONMENT_VARIABLE_PREFIX + "SERVER";
    public static final String LDAP_USER_ATTRIBUTES_KEY = ENVIRONMENT_VARIABLE_PREFIX + "USER_ATTRIBUTES";
    public static final String LDAP_USER_DN_PATTERNS_KEY = ENVIRONMENT_VARIABLE_PREFIX + "USER_DN_PATTERNS";
    public static final String LDAP_USER_SEARCH_BASE_KEY = ENVIRONMENT_VARIABLE_PREFIX + "USER_SEARCH_BASE";
    public static final String LDAP_USER_SEARCH_FILTER_KEY = ENVIRONMENT_VARIABLE_PREFIX + "USER_SEARCH_FILTER";

    public static final Set<String> LDAP_CONFIGURATION_KEY_SET = Set.of(
        LDAP_AUTHENTICATION_TYPE_KEY,
        LDAP_ENABLED_KEY,
        LDAP_GROUP_ROLE_ATTRIBUTE_KEY,
        LDAP_GROUP_SEARCH_BASE_KEY,
        LDAP_GROUP_SEARCH_FILTER_KEY,
        LDAP_MANAGER_DN_KEY,
        LDAP_MANAGER_PASSWORD_KEY,
        LDAP_REFERRAL_KEY,
        LDAP_SERVER_KEY,
        LDAP_USER_ATTRIBUTES_KEY,
        LDAP_USER_DN_PATTERNS_KEY,
        LDAP_USER_SEARCH_BASE_KEY,
        LDAP_USER_SEARCH_FILTER_KEY
    );

    private final LDAPConfigAccessor ldapConfigAccessor;
    private final LDAPConfigurationValidator ldapConfigurationValidator;
    private final EnvironmentVariableUtility environmentVariableUtility;

    @Autowired
    protected LDAPEnvironmentVariableHandler(
        LDAPConfigAccessor ldapConfigAccessor,
        LDAPConfigurationValidator ldapConfigurationValidator,
        EnvironmentVariableUtility environmentVariableUtility
    ) {
        super(HANDLER_NAME, LDAP_CONFIGURATION_KEY_SET, environmentVariableUtility);
        this.ldapConfigAccessor = ldapConfigAccessor;
        this.ldapConfigurationValidator = ldapConfigurationValidator;
        this.environmentVariableUtility = environmentVariableUtility;
    }

    @Override
    protected Boolean configurationMissingCheck() {
        return !ldapConfigAccessor.doesConfigurationExist();
    }

    @Override
    protected LDAPConfigModel configureModel() {
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        LDAPConfigModel ldapConfigModel = new LDAPConfigModel();

        configureLDAPConfigFromEnv(ldapConfigModel);

        ldapConfigModel.setCreatedAt(createdAt);
        ldapConfigModel.setLastUpdated(createdAt);

        return ldapConfigModel;
    }

    @Override
    protected ValidationResponseModel validateConfiguration(LDAPConfigModel ldapConfigModel) {
        return ldapConfigurationValidator.validate(ldapConfigModel);
    }

    @Override
    protected EnvironmentProcessingResult buildProcessingResult(LDAPConfigModel obfuscatedConfigModel) {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(LDAP_CONFIGURATION_KEY_SET);

        builder.addVariableValue(LDAP_ENABLED_KEY, String.valueOf(obfuscatedConfigModel.getEnabled()));

        if (StringUtils.isNotBlank(obfuscatedConfigModel.getServerName())) {
            builder.addVariableValue(LDAP_SERVER_KEY, obfuscatedConfigModel.getServerName());
        }
        if (StringUtils.isNotBlank(obfuscatedConfigModel.getManagerDn())) {
            builder.addVariableValue(LDAP_MANAGER_DN_KEY, obfuscatedConfigModel.getManagerDn());
        }

        if (Boolean.TRUE.equals(obfuscatedConfigModel.getIsManagerPasswordSet())) {
            builder.addVariableValue(LDAP_MANAGER_PASSWORD_KEY, AlertConstants.MASKED_VALUE);
        }

        obfuscatedConfigModel.getAuthenticationType()
            .ifPresent(value -> builder.addVariableValue(LDAP_AUTHENTICATION_TYPE_KEY, value));
        obfuscatedConfigModel.getGroupRoleAttribute()
            .ifPresent(value -> builder.addVariableValue(LDAP_GROUP_ROLE_ATTRIBUTE_KEY, value));
        obfuscatedConfigModel.getGroupSearchBase()
            .ifPresent(value -> builder.addVariableValue(LDAP_GROUP_SEARCH_BASE_KEY, value));
        obfuscatedConfigModel.getGroupSearchFilter()
            .ifPresent(value -> builder.addVariableValue(LDAP_GROUP_SEARCH_FILTER_KEY, value));
        obfuscatedConfigModel.getReferral()
            .ifPresent(value -> builder.addVariableValue(LDAP_REFERRAL_KEY, value));
        obfuscatedConfigModel.getUserAttributes()
            .ifPresent(value -> builder.addVariableValue(LDAP_USER_ATTRIBUTES_KEY, value));
        obfuscatedConfigModel.getUserDnPatterns()
            .ifPresent(value -> builder.addVariableValue(LDAP_USER_DN_PATTERNS_KEY, value));
        obfuscatedConfigModel.getUserSearchBase()
            .ifPresent(value -> builder.addVariableValue(LDAP_USER_SEARCH_BASE_KEY, value));
        obfuscatedConfigModel.getUserSearchFilter()
            .ifPresent(value -> builder.addVariableValue(LDAP_USER_SEARCH_FILTER_KEY, value));

        return builder.build();
    }

    @Override
    protected void saveConfiguration(LDAPConfigModel ldapConfigModel, EnvironmentProcessingResult processingResult) {
        try {
            ldapConfigAccessor.createConfiguration(ldapConfigModel);
        } catch (AlertConfigurationException ex) {
            logger.error("Error creating the configuration: {}", ex.getMessage());
        }
    }

    private void configureLDAPConfigFromEnv(LDAPConfigModel ldapConfigModel) {
        environmentVariableUtility.getEnvironmentValue(LDAP_AUTHENTICATION_TYPE_KEY)
            .ifPresent(ldapConfigModel::setAuthenticationType);
        environmentVariableUtility.getEnvironmentValue(LDAP_ENABLED_KEY)
            .map(Boolean::valueOf)
            .ifPresent(ldapConfigModel::setEnabled);
        environmentVariableUtility.getEnvironmentValue(LDAP_GROUP_ROLE_ATTRIBUTE_KEY)
            .ifPresent(ldapConfigModel::setGroupRoleAttribute);
        environmentVariableUtility.getEnvironmentValue(LDAP_GROUP_SEARCH_BASE_KEY)
            .ifPresent(ldapConfigModel::setGroupSearchBase);
        environmentVariableUtility.getEnvironmentValue(LDAP_GROUP_SEARCH_FILTER_KEY)
            .ifPresent(ldapConfigModel::setGroupSearchFilter);
        environmentVariableUtility.getEnvironmentValue(LDAP_MANAGER_DN_KEY)
            .ifPresent(ldapConfigModel::setManagerDn);
        environmentVariableUtility.getEnvironmentValue(LDAP_MANAGER_PASSWORD_KEY)
            .ifPresent(ldapConfigModel::setManagerPassword);
        environmentVariableUtility.getEnvironmentValue(LDAP_MANAGER_PASSWORD_KEY)
            .map(StringUtils::isNotBlank)
            .ifPresent(ldapConfigModel::setIsManagerPasswordSet);
        environmentVariableUtility.getEnvironmentValue(LDAP_REFERRAL_KEY)
            .ifPresent(ldapConfigModel::setReferral);
        environmentVariableUtility.getEnvironmentValue(LDAP_SERVER_KEY)
            .ifPresent(ldapConfigModel::setServerName);
        environmentVariableUtility.getEnvironmentValue(LDAP_USER_ATTRIBUTES_KEY)
            .ifPresent(ldapConfigModel::setUserAttributes);
        environmentVariableUtility.getEnvironmentValue(LDAP_USER_DN_PATTERNS_KEY)
            .ifPresent(ldapConfigModel::setUserDnPatterns);
        environmentVariableUtility.getEnvironmentValue(LDAP_USER_SEARCH_BASE_KEY)
            .ifPresent(ldapConfigModel::setUserSearchBase);
        environmentVariableUtility.getEnvironmentValue(LDAP_USER_SEARCH_FILTER_KEY)
            .ifPresent(ldapConfigModel::setUserSearchFilter);
    }
}
