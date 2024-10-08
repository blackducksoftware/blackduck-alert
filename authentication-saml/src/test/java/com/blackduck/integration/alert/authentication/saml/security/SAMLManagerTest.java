package com.blackduck.integration.alert.authentication.saml.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.authentication.saml.SAMLTestHelper;
import com.blackduck.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.blackduck.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.blackduck.integration.alert.authentication.saml.model.SAMLMetadataMode;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;

@ExtendWith(SpringExtension.class)
class SAMLManagerTest {
    private SAMLManager samlManager;
    private SAMLConfigAccessor samlConfigAccessor;
    private AlertRelyingPartyRegistrationRepository alertRelyingPartyRegistrationRepository;
    @Mock
    private FilePersistenceUtil filePersistenceUtil;
    @Mock
    private OpenSaml4AuthenticationRequestResolver saml2AuthenticationRequestResolver;
    @Mock
    private RelyingPartyRegistration relyingPartyRegistration;
    @Mock
    private RelyingPartyRegistration.Builder builder;

    @BeforeEach
    void init() throws IOException {
        samlConfigAccessor = SAMLTestHelper.createTestSAMLConfigAccessor();
        alertRelyingPartyRegistrationRepository = new AlertRelyingPartyRegistrationRepository();
        samlManager = new SAMLManager(samlConfigAccessor, alertRelyingPartyRegistrationRepository, filePersistenceUtil, saml2AuthenticationRequestResolver);

        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_METADATA_FILE)).thenReturn("dummy data");
        Mockito.when(builder.build()).thenReturn(relyingPartyRegistration);
        Mockito.when(relyingPartyRegistration.getRegistrationId()).thenReturn("default");
    }

    @Test
    void reconfigureEnablesSAMLFromUrl() throws AlertConfigurationException {
        try (MockedStatic<RelyingPartyRegistrations> relyingPartyRegistrationsMockedStatic =  Mockito.mockStatic(RelyingPartyRegistrations.class)) {
            relyingPartyRegistrationsMockedStatic.when(() -> RelyingPartyRegistrations.fromMetadataLocation(anyString()))
                .thenReturn(builder);
            SAMLConfigModel enabledSAML = new SAMLTestHelper.SAMLConfigModelBuilder()
                .setMetadataMode(SAMLMetadataMode.URL)
                .setMetadataUrl("https://www.metadataurl.com")
                .setEnabled(true)
                .build();
            samlConfigAccessor.createConfiguration(enabledSAML);

            samlManager.reconfigureSAML();
            assertTrue(alertRelyingPartyRegistrationRepository.iterator().hasNext());
            Mockito.verify(saml2AuthenticationRequestResolver, times(1)).setAuthnRequestCustomizer(any());
        }
    }

    @Test
    void reconfigureEnablesSAMLFromFile() throws AlertConfigurationException {
        try (MockedStatic<RelyingPartyRegistrations> relyingPartyRegistrationsMockedStatic =  Mockito.mockStatic(RelyingPartyRegistrations.class)) {
            relyingPartyRegistrationsMockedStatic.when(() -> RelyingPartyRegistrations.fromMetadata(any()))
                .thenReturn(builder);
            SAMLConfigModel enabledSAML = new SAMLTestHelper.SAMLConfigModelBuilder()
                .setMetadataMode(SAMLMetadataMode.FILE)
                .setEnabled(true)
                .build();
            samlConfigAccessor.createConfiguration(enabledSAML);

            samlManager.reconfigureSAML();
            assertTrue(alertRelyingPartyRegistrationRepository.iterator().hasNext());
            Mockito.verify(saml2AuthenticationRequestResolver, times(1)).setAuthnRequestCustomizer(any());
        }
    }

    @Test
    void reconfigureDisablesSAML() throws AlertConfigurationException {
        try (MockedStatic<RelyingPartyRegistrations> relyingPartyRegistrationsMockedStatic =  Mockito.mockStatic(RelyingPartyRegistrations.class)) {
            relyingPartyRegistrationsMockedStatic.when(() -> RelyingPartyRegistrations.fromMetadata(any()))
                .thenReturn(builder);
            SAMLConfigModel enabledSAML = new SAMLTestHelper.SAMLConfigModelBuilder()
                .setMetadataMode(SAMLMetadataMode.FILE)
                .setEnabled(true)
                .build();
            samlConfigAccessor.createConfiguration(enabledSAML);

            samlManager.reconfigureSAML();
            assertTrue(alertRelyingPartyRegistrationRepository.iterator().hasNext());
        }

        SAMLConfigModel disabledSAML = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setEnabled(false)
            .build();
        samlConfigAccessor.updateConfiguration(disabledSAML);

        samlManager.reconfigureSAML();
        assertFalse(alertRelyingPartyRegistrationRepository.iterator().hasNext());
    }

    @Test
    void reconfigureBuildsSigningCert() throws AlertConfigurationException, IOException {
        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE))
            .thenReturn(SAMLTestHelper.TEST_X509_CERT);
        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE)).thenReturn(SAMLTestHelper.TEST_PRIVATE_KEY);
        Mockito.when(filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE)).thenReturn(true);

        try (MockedStatic<RelyingPartyRegistrations> relyingPartyRegistrationsMockedStatic = Mockito.mockStatic(RelyingPartyRegistrations.class)) {
            relyingPartyRegistrationsMockedStatic.when(() -> RelyingPartyRegistrations.fromMetadataLocation(anyString()))
                .thenReturn(builder);
            SAMLConfigModel enabledSAML = new SAMLTestHelper.SAMLConfigModelBuilder()
                .setMetadataMode(SAMLMetadataMode.URL)
                .setMetadataUrl("https://www.metadataurl.com")
                .setEnabled(true)
                .build();
            samlConfigAccessor.createConfiguration(enabledSAML);

            samlManager.reconfigureSAML();
            assertTrue(alertRelyingPartyRegistrationRepository.iterator().hasNext());
            Mockito.verify(builder, times(1)).signingX509Credentials(any());
        }
    }

    @Test
    void reconfigureBuildsEncryptionCert() throws AlertConfigurationException, IOException {
        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE)).thenReturn(SAMLTestHelper.TEST_X509_CERT);
        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE)).thenReturn(SAMLTestHelper.TEST_PRIVATE_KEY);
        Mockito.when(filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE)).thenReturn(true);
        Mockito.when(builder.decryptionX509Credentials(any())).thenReturn(builder);

        try (MockedStatic<RelyingPartyRegistrations> relyingPartyRegistrationsMockedStatic = Mockito.mockStatic(RelyingPartyRegistrations.class)) {
            relyingPartyRegistrationsMockedStatic.when(() -> RelyingPartyRegistrations.fromMetadataLocation(anyString()))
                .thenReturn(builder);
            SAMLConfigModel enabledSAML = new SAMLTestHelper.SAMLConfigModelBuilder()
                .setMetadataMode(SAMLMetadataMode.URL)
                .setMetadataUrl("https://www.metadataurl.com")
                .setEnabled(true)
                .build();
            samlConfigAccessor.createConfiguration(enabledSAML);

            samlManager.reconfigureSAML();
            assertTrue(alertRelyingPartyRegistrationRepository.iterator().hasNext());
            Mockito.verify(builder, times(1)).decryptionX509Credentials(any());
        }
    }

    @Test
    void reconfigureBuildsVerificationCert() throws AlertConfigurationException, IOException {
        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE)).thenReturn(SAMLTestHelper.TEST_X509_CERT);
        Mockito.when(filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE)).thenReturn(true);

        try (MockedStatic<RelyingPartyRegistrations> relyingPartyRegistrationsMockedStatic =  Mockito.mockStatic(RelyingPartyRegistrations.class)) {
            relyingPartyRegistrationsMockedStatic.when(() -> RelyingPartyRegistrations.fromMetadataLocation(anyString()))
                .thenReturn(builder);
            SAMLConfigModel enabledSAML = new SAMLTestHelper.SAMLConfigModelBuilder()
                .setMetadataMode(SAMLMetadataMode.URL)
                .setMetadataUrl("https://www.metadataurl.com")
                .setEnabled(true)
                .build();
            samlConfigAccessor.createConfiguration(enabledSAML);

            samlManager.reconfigureSAML();
            assertTrue(alertRelyingPartyRegistrationRepository.iterator().hasNext());
            Mockito.verify(builder, times(1)).assertingPartyDetails(any());
        }
    }

    @Test
    void isSAMLEnabledReturnsConfigSAMLEnabled() throws AlertConfigurationException {
        SAMLConfigModel disabledSAML = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setEnabled(false)
            .build();
        samlConfigAccessor.createConfiguration(disabledSAML);
        assertFalse(samlManager.isSAMLEnabled());

        SAMLConfigModel enabledSAML = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setEnabled(true)
            .build();
        samlConfigAccessor.updateConfiguration(enabledSAML);
        assertTrue(samlManager.isSAMLEnabled());
        samlConfigAccessor.deleteConfiguration();
        assertFalse(samlManager.isSAMLEnabled());
    }
}
