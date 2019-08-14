package com.synopsys.integration.alert.workflow.startup.component;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.database.api.SystemStatusUtility;

@Component
@Order(2)
public class SystemStatusValidator extends StartupComponent {
    private final SystemStatusUtility systemStatusUtility;
    private final EncryptionUtility encryptionUtility;
    private final DefaultUserAccessor userAccessor;

    @Autowired
    public SystemStatusValidator(SystemStatusUtility systemStatusUtility, EncryptionUtility encryptionUtility, DefaultUserAccessor userAccessor) {
        this.systemStatusUtility = systemStatusUtility;
        this.encryptionUtility = encryptionUtility;
        this.userAccessor = userAccessor;
    }

    @Override
    protected void initialize() {
        boolean systemInitializationStatus = isSystemInitialized();
        systemStatusUtility.setSystemInitialized(systemInitializationStatus);
    }

    private boolean isSystemInitialized() {
        return areEncryptionSettingsConfigured() && isAdminUserConfigured();
    }

    private boolean areEncryptionSettingsConfigured() {
        return encryptionUtility.isInitialized();

    }

    private boolean isAdminUserConfigured() {
        return userAccessor
                   .getUser(DefaultUserAccessor.DEFAULT_ADMIN_USER)
                   .filter(this::isUserEmailSet)
                   .filter(this::isUserPasswordSet)
                   .isPresent();
    }

    private boolean isUserEmailSet(UserModel user) {
        return StringUtils.isNotBlank(user.getEmailAddress());
    }

    private boolean isUserPasswordSet(UserModel user) {
        return StringUtils.isNotBlank(user.getPassword());
    }

}
