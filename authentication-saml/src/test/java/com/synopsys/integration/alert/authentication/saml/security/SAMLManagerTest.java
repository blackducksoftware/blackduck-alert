package com.synopsys.integration.alert.authentication.saml.security;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.saml.SAMLTestHelper;
import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.model.SAMLMetadataMode;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

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

    private final String TEST_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCkMG1mghFYy1De\n" +
        "kOvTCTMYsYv9IuzbHAokzbeCPBvmakhBHtIp6rYW4lcAWkyskjfug2+EOLlt2kLK\n" +
        "QBl830qd+HV6ZbJfSDhXVFBZok6jws5o1RAeJeGJbafnECDeTZZSjI8lj5jjcNmC\n" +
        "stxVNeyndY/Yj4XOcvcqJ3MvCeT9Lrjdn21J9dDY6ONcNIwSxoUScrNhI9nLtA4P\n" +
        "q/OdCJ//j6+QYjhciItBfL1Bnoo62lvglEDDv2pRGsNI83TlH1rh+0QwvSkROte7\n" +
        "cKGrQI28ylCNq0FgdLQizgTqGBNY6WG+6TMUZJnWBhlcXlQPpYxPZS8peA8Up9fQ\n" +
        "2XlOwRc3AgMBAAECggEAY8h+pD7F9hXSNwESIPZFVGuKrTPNiLf4vjc7oG2Rcy4C\n" +
        "bXkitpDLSwsRXsiCMprGXTEJ0+x5XPa5gpsEImy9t1WpZ9JoXhnRC9nU3zSR4Fpj\n" +
        "ATkDi57v+4pl8TpPD2tNwStlT6l+fDM4LYMv/qVABuGeozlOU5Jw/fBtwxebT/Zc\n" +
        "8zjID/hcghDkMGPpLoZrmHBtH8sccTJh4EXTT77CWz1B4Ypj7qeMdH6ipVlykHiU\n" +
        "OtZ1mhZsUJvJvdBx+FhnzitjAGy4rnb01mQal0UMu3n3RYhQBwZFK1dF/gHFy6mO\n" +
        "Txa7shtU9q2Q6tY8OhLw6FQ49UhxhsriUemkp+1gWQKBgQDPTmBV9FTX+WnVlehY\n" +
        "qVovvqSyH3r1mN6NEGWPi7mAsGGl/sG+OZ4Op8msBIblJOxkuXFMmb84xkupZUWE\n" +
        "0L55l3YOUqLTKZ6OLSmo1FWUr/mBSBryivtqimDF6F6TOso8PGeJPDra3yItq8Bt\n" +
        "msUWPxqvFlh1Gh96TVwME0wjQwKBgQDKwVz7yA+WAl6Fz/bFARUCQnGmuwCoNyKW\n" +
        "ket2yyph1xJxhZEyKUUHpvPN0ihm3+fa1lAhMqiNYl3cDZxxj/7mCzITiiYhGg/l\n" +
        "e/ClCsbtc5YndBnvmIUlGkqELW0x2PQM66jl7fITvibNGVUQSn/WR/9Dy2jUuWnL\n" +
        "pLHM8K3q/QKBgQDGPyyPZn3woBre32H3z9RoTkdP9BzxW0SQ4DpPgQ5gC80GIk+D\n" +
        "K7SETV7mOtoJrjrGxDSeTXV5xOA8WZWWws00mGyUm+AMUgPH8VailpRVAch4Is1q\n" +
        "XxC5vSV4fZRb9d/KpPVaXxflkXcs98+owVZyxNwuzg6/xcCOhAZfoeCw4wKBgFOC\n" +
        "evrVNt4xUZ9Jvbj8rhuFJGxo32dpdhyQM2x1V5p+zADn45rGdsElGYvvgGaG2GdB\n" +
        "GDXkOJorJr+I96OqjHFRsSltIDFIG74IY2CG5NJOZk0Pu9L8ffJ/NqvkTfJXCoX7\n" +
        "1lAFxkwtaKvXQDr6hFbMxq20rRPY3mNbDUR86RU9AoGATspuUnP2fLNnZF+ve3q9\n" +
        "k+Q0PpW7/VO+EaR53qZHfp+FpAahY5SG+CwtESMF9gIKfPDv633H3APfXgUwRKuj\n" +
        "c+cC8DScuXTfJzA3XBhi+CCot4ja4z+XBEL49LxH9kitHzvWMKn8rIOAkg9p9/h/\n" +
        "QPOsrlKBEv70iIXy2RCBZtk=\n" +
        "-----END PRIVATE KEY-----\n";

    private final String TEST_X509_CERT = "-----BEGIN CERTIFICATE-----\n" +
        "MIIDcDCCAligAwIBAgIEYjpLADANBgkqhkiG9w0BAQsFADBRMQswCQYDVQQGEwJV\n" +
        "UzENMAsGA1UEAwwEVGVzdDEVMBMGA1UEBwwMRGVmYXVsdCBDaXR5MRwwGgYDVQQK\n" +
        "DBNEZWZhdWx0IENvbXBhbnkgTHRkMB4XDTIzMDMxNDIxMDU0MloXDTMzMDMxMTIx\n" +
        "MDU0MlowUTELMAkGA1UEBhMCVVMxDTALBgNVBAMMBFRlc3QxFTATBgNVBAcMDERl\n" +
        "ZmF1bHQgQ2l0eTEcMBoGA1UECgwTRGVmYXVsdCBDb21wYW55IEx0ZDCCASIwDQYJ\n" +
        "KoZIhvcNAQEBBQADggEPADCCAQoCggEBAKQwbWaCEVjLUN6Q69MJMxixi/0i7Nsc\n" +
        "CiTNt4I8G+ZqSEEe0inqthbiVwBaTKySN+6Db4Q4uW3aQspAGXzfSp34dXplsl9I\n" +
        "OFdUUFmiTqPCzmjVEB4l4Yltp+cQIN5NllKMjyWPmONw2YKy3FU17Kd1j9iPhc5y\n" +
        "9yoncy8J5P0uuN2fbUn10Njo41w0jBLGhRJys2Ej2cu0Dg+r850In/+Pr5BiOFyI\n" +
        "i0F8vUGeijraW+CUQMO/alEaw0jzdOUfWuH7RDC9KRE617twoatAjbzKUI2rQWB0\n" +
        "tCLOBOoYE1jpYb7pMxRkmdYGGVxeVA+ljE9lLyl4DxSn19DZeU7BFzcCAwEAAaNQ\n" +
        "ME4wHQYDVR0OBBYEFC+upLiXZBh8g0NhEVguGUt94qs7MB8GA1UdIwQYMBaAFC+u\n" +
        "pLiXZBh8g0NhEVguGUt94qs7MAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQAD\n" +
        "ggEBAFI4U9yjeQSZHYCw8aFSnOQiEXV1JCVQ3iYX5crJq5iTAN3K81qH9s2nuSfs\n" +
        "EqMPdPtDj4eXeEHysVmmriqB+FKERk+JWzzUJaFG7ZjkyjYef86H2Yad2g+vbzB8\n" +
        "GOwKf+LBsad90MCiXw1QIdW7HsfwJEY7RgmlqbKSKe0p8T9j/Bo1dSffMtjSxwh6\n" +
        "26jBAGie3ltxiP5wVG/AzSO5PCsRFGGn7dJt+EOfGOH8ArEMbr9zE2N5zRVfyHAH\n" +
        "8QrgHIG1/b4zfwuv09AJOSsgBCWK7QdFAFV4w7a9aShRT1ZbPCnDLP6TQhsxKWYy\n" +
        "sfDy/dAZ6ATjWvU8c4BrVLMggXc=\n" +
        "-----END CERTIFICATE-----\n";

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
        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE)).thenReturn(TEST_X509_CERT);
        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE)).thenReturn(TEST_PRIVATE_KEY);
        Mockito.when(filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE)).thenReturn(true);

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
            Mockito.verify(builder, times(1)).signingX509Credentials(any());
        }
    }

    @Test
    void reconfigureBuildsEncryptionCert() throws AlertConfigurationException, IOException {
        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE)).thenReturn(TEST_X509_CERT);
        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE)).thenReturn(TEST_PRIVATE_KEY);
        Mockito.when(filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE)).thenReturn(true);
        Mockito.when(builder.decryptionX509Credentials(any())).thenReturn(builder);

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
            Mockito.verify(builder, times(1)).decryptionX509Credentials(any());
        }
    }

    @Test
    void reconfigureBuildsVerificationCert() throws AlertConfigurationException, IOException {
        Mockito.when(filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE)).thenReturn(TEST_X509_CERT);
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
