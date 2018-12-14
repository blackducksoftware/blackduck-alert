package com.synopsys.integration.alert.workflow.startup.install;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;

public class SystemInitializerTest {
    private SystemStatusUtility systemStatusUtility;
    private AlertProperties alertProperties;
    private EncryptionUtility encryptionUtility;
    private SystemValidator systemValidator;
    private UserAccessor userAccessor;
    private BaseConfigurationAccessor baseConfigurationAccessor;

    @Before
    public void initialize() {
        systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        alertProperties = Mockito.mock(AlertProperties.class);
        encryptionUtility = Mockito.mock(EncryptionUtility.class);
        systemValidator = Mockito.mock(SystemValidator.class);
        userAccessor = Mockito.mock(UserAccessor.class);
        baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);

        try {
            Mockito.when(baseConfigurationAccessor.updateConfiguration(Mockito.anyLong(), Mockito.anyCollection())).thenReturn(null);
            Mockito.when(baseConfigurationAccessor.createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(null);
        } catch (final AlertDatabaseConstraintException e) {
            // This won't happen
        }
    }

    @Test
    public void testIsInitiailzed() {
        final SystemInitializer systemInitializer = new SystemInitializer(systemStatusUtility, alertProperties, encryptionUtility, systemValidator, userAccessor, baseConfigurationAccessor);

        assertFalse(systemInitializer.isSystemInitialized());
        Mockito.when(systemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        assertTrue(systemInitializer.isSystemInitialized());
    }

    @Test
    public void testGetCurrentSystemSetup() {
        final SystemInitializer systemInitializer = new SystemInitializer(systemStatusUtility, alertProperties, encryptionUtility, systemValidator, userAccessor, baseConfigurationAccessor);
        systemInitializer.getCurrentSystemSetup();

        Mockito.verify(alertProperties).getAlertProxyHost();
        Mockito.verify(alertProperties).getAlertProxyPort();
        Mockito.verify(alertProperties).getAlertProxyPassword();
        Mockito.verify(alertProperties).getAlertProxyUsername();
        Mockito.verify(encryptionUtility).isPasswordSet();
        Mockito.verify(encryptionUtility).isGlobalSaltSet();
    }

    @Test
    public void testSaveEncryptionException() throws Exception {
        final String defaultAdminPassword = "defaultPassword";
        final boolean defaultAdminPasswordSet = true;
        final String blackDuckProviderUrl = "url";
        final Integer blackDuckConnectionTimeout = 100;
        final String blackDuckApiToken = "token";
        final String globalEncryptionPassword = "password";
        final boolean isGlobalEncryptionPasswordSet = true;
        final String globalEncryptionSalt = "salt";
        final boolean isGlobalEncryptionSaltSet = true;
        final String proxyHost = "host";
        final String proxyPort = "port";
        final String proxyUsername = "username";
        final String proxyPassword = "password";

        final RequiredSystemConfiguration configuration = new RequiredSystemConfiguration(defaultAdminPassword, defaultAdminPasswordSet, blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken,
                globalEncryptionPassword, isGlobalEncryptionPasswordSet, globalEncryptionSalt, isGlobalEncryptionSaltSet,
                proxyHost, proxyPort, proxyUsername, proxyPassword);

        Mockito.doThrow(new IllegalArgumentException("bad credentials")).when(encryptionUtility).updateEncryptionFields(Mockito.anyString(), Mockito.anyString());

        final SystemInitializer systemInitializer = new SystemInitializer(systemStatusUtility, alertProperties, encryptionUtility, systemValidator, userAccessor, baseConfigurationAccessor);
        systemInitializer.updateRequiredConfiguration(configuration, new HashMap<>());
        Mockito.verify(encryptionUtility).updateEncryptionFields(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testSaveWithExistingData() throws Exception {
        final String defaultAdminPassword = "defaultPassword";
        final boolean defaultAdminPasswordSet = true;
        final String blackDuckProviderUrl = "url";
        final Integer blackDuckConnectionTimeout = 100;
        final String blackDuckApiToken = "token";
        final String globalEncryptionPassword = "password";
        final boolean isGlobalEncryptionPasswordSet = true;
        final String globalEncryptionSalt = "salt";
        final boolean isGlobalEncryptionSaltSet = true;
        final String proxyHost = "host";
        final String proxyPort = "port";
        final String proxyUsername = "username";
        final String proxyPassword = "password";

        final RequiredSystemConfiguration configuration = new RequiredSystemConfiguration(defaultAdminPassword, defaultAdminPasswordSet, blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken,
                globalEncryptionPassword, isGlobalEncryptionPasswordSet, globalEncryptionSalt, isGlobalEncryptionSaltSet,
                proxyHost, proxyPort, proxyUsername, proxyPassword);

        final SystemInitializer systemInitializer = new SystemInitializer(systemStatusUtility, alertProperties, encryptionUtility, systemValidator, userAccessor, baseConfigurationAccessor);
        systemInitializer.updateRequiredConfiguration(configuration, new HashMap<>());

        Mockito.verify(userAccessor).changeUserPassword(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(alertProperties).setAlertProxyHost(Mockito.anyString());
        Mockito.verify(alertProperties).setAlertProxyPort(Mockito.anyString());
        Mockito.verify(alertProperties).setAlertProxyPassword(Mockito.anyString());
        Mockito.verify(alertProperties).setAlertProxyUsername(Mockito.anyString());
        Mockito.verify(encryptionUtility).updateEncryptionFields(Mockito.anyString(), Mockito.anyString());
    }
}
