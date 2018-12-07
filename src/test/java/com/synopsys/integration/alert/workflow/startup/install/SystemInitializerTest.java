package com.synopsys.integration.alert.workflow.startup.install;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;

public class SystemInitializerTest {
    private SystemStatusUtility systemStatusUtility;
    private AlertProperties alertProperties;
    private GlobalBlackDuckRepository globalBlackDuckRepository;
    private EncryptionUtility encryptionUtility;
    private SystemValidator systemValidator;
    private UserAccessor userAccessor;

    @Before
    public void initialize() {
        systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        alertProperties = Mockito.mock(AlertProperties.class);
        globalBlackDuckRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        encryptionUtility = Mockito.mock(EncryptionUtility.class);
        systemValidator = Mockito.mock(SystemValidator.class);
        userAccessor = Mockito.mock(UserAccessor.class);

    }

    @Test
    public void testIsInitiailzed() {
        final SystemInitializer systemInitializer = new SystemInitializer(systemStatusUtility, alertProperties, globalBlackDuckRepository, encryptionUtility, systemValidator, userAccessor);

        assertFalse(systemInitializer.isSystemInitialized());
        Mockito.when(systemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        assertTrue(systemInitializer.isSystemInitialized());
    }

    @Test
    public void testGetCurrentSystemSetup() {
        final Integer timeout = 300;
        final String apiToken = "token";
        final String url = "url";
        final GlobalBlackDuckConfigEntity entity = new GlobalBlackDuckConfigEntity(timeout, apiToken, url);
        Mockito.when(globalBlackDuckRepository.findAll()).thenReturn(Collections.singletonList(entity));

        final SystemInitializer systemInitializer = new SystemInitializer(systemStatusUtility, alertProperties, globalBlackDuckRepository, encryptionUtility, systemValidator, userAccessor);

        systemInitializer.getCurrentSystemSetup();

        Mockito.verify(alertProperties).getAlertProxyHost();
        Mockito.verify(alertProperties).getAlertProxyPort();
        Mockito.verify(alertProperties).getAlertProxyPassword();
        Mockito.verify(alertProperties).getAlertProxyUsername();
        Mockito.verify(globalBlackDuckRepository).findAll();
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

        final SystemInitializer systemInitializer = new SystemInitializer(systemStatusUtility, alertProperties, globalBlackDuckRepository, encryptionUtility, systemValidator, userAccessor);
        systemInitializer.updateRequiredConfiguration(configuration, new HashMap<>());
        Mockito.verify(encryptionUtility).updateEncryptionFields(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testSaveWithExistingData() throws Exception {
        final Integer timeout = 300;
        final String apiToken = "token";
        final String url = "url";
        final GlobalBlackDuckConfigEntity entity = new GlobalBlackDuckConfigEntity(timeout, apiToken, url);
        entity.setId(999L);
        Mockito.when(globalBlackDuckRepository.findAll()).thenReturn(Collections.singletonList(entity));
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

        final SystemInitializer systemInitializer = new SystemInitializer(systemStatusUtility, alertProperties, globalBlackDuckRepository, encryptionUtility, systemValidator, userAccessor);
        systemInitializer.updateRequiredConfiguration(configuration, new HashMap<>());

        Mockito.verify(userAccessor).changeUserPassword(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(alertProperties).setAlertProxyHost(Mockito.anyString());
        Mockito.verify(alertProperties).setAlertProxyPort(Mockito.anyString());
        Mockito.verify(alertProperties).setAlertProxyPassword(Mockito.anyString());
        Mockito.verify(alertProperties).setAlertProxyUsername(Mockito.anyString());
        Mockito.verify(globalBlackDuckRepository).findAll();
        Mockito.verify(globalBlackDuckRepository).save(Mockito.any());
        Mockito.verify(encryptionUtility).updateEncryptionFields(Mockito.anyString(), Mockito.anyString());
    }
}
