package com.blackduck.integration.alert.authentication.saml;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.blackduck.integration.alert.authentication.saml.database.mock.MockSAMLConfigurationRepository;
import com.blackduck.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.blackduck.integration.alert.authentication.saml.model.SAMLMetadataMode;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.common.util.DateUtils;

public class SAMLTestHelper {
    public static final String TEST_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
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

    public static final String TEST_X509_CERT = "-----BEGIN CERTIFICATE-----\n" +
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

    public static AuthorizationManager createAuthorizationManager() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), authenticationDescriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
            "admin",
            "admin",
            () -> new PermissionMatrixModel(permissions));
    }

    public static FilePersistenceUtil createFilePersistenceUtil() {
        AlertProperties alertProperties = new MockAlertProperties();
        return new FilePersistenceUtil(alertProperties, BlackDuckServicesFactory.createDefaultGson());
    }

    public static FilePersistenceUtil createTempDirFilePersistenceUtil(Path tempDir) {
        // Create a temp directory for mockAlertProperties for filePersistenceUtils to use
        MockAlertProperties alertProperties = new MockAlertProperties();
        alertProperties.setAlertConfigHome(tempDir.toAbsolutePath().toString());
        return new FilePersistenceUtil(alertProperties, BlackDuckServicesFactory.createDefaultGson());
    }

    public static SAMLConfigAccessor createTestSAMLConfigAccessor() {
        MockSAMLConfigurationRepository mockSAMLConfigurationRepository = new MockSAMLConfigurationRepository();
        return new SAMLConfigAccessor(mockSAMLConfigurationRepository);
    }

    public static class SAMLConfigModelBuilder {
        private final String id;
        private String createdAt;
        private String lastUpdated = "";
        private Boolean enabled = false;
        private Boolean forceAuth = false;
        private String metadataUrl = "";
        private String metadataFileName = "";
        private SAMLMetadataMode metadataMode = SAMLMetadataMode.URL;
        private String encryptionCertFileName = "";
        private String encryptionPrivateKeyFileName = "";
        private String signingCertFileName = "";
        private String signingPrivateKeyFileName = "";
        private String verificationCertFileName = "";

        public SAMLConfigModelBuilder() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        }

        public SAMLConfigModelBuilder setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SAMLConfigModelBuilder setLastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public SAMLConfigModelBuilder setEnabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public SAMLConfigModelBuilder setForceAuth(Boolean forceAuth) {
            this.forceAuth = forceAuth;
            return this;
        }

        public SAMLConfigModelBuilder setMetadataUrl(String metadataUrl) {
            this.metadataUrl = metadataUrl;
            return this;
        }

        public SAMLConfigModelBuilder setMetadataFileName(String metadataFileName) {
            this.metadataFileName = metadataFileName;
            return this;
        }

        public SAMLConfigModelBuilder setMetadataMode(SAMLMetadataMode metadataMode) {
            this.metadataMode = metadataMode;
            return this;
        }

        public SAMLConfigModelBuilder setEncryptionCertFileName(String encryptionCertFileName) {
            this.encryptionCertFileName = encryptionCertFileName;
            return this;
        }

        public SAMLConfigModelBuilder setEncryptionPrivateKeyFileName(String encryptionPrivateKeyFileName) {
            this.encryptionPrivateKeyFileName = encryptionPrivateKeyFileName;
            return this;
        }

        public SAMLConfigModelBuilder setSigningCertFileName(String signingCertFileName) {
            this.signingCertFileName = signingCertFileName;
            return this;
        }

        public SAMLConfigModelBuilder setSigningPrivateKeyFileName(String signingPrivateKeyFileName) {
            this.signingPrivateKeyFileName = signingPrivateKeyFileName;
            return this;
        }

        public SAMLConfigModelBuilder setVerificationCertFileName(String verificationCertFileName) {
            this.verificationCertFileName = verificationCertFileName;
            return this;
        }

        public SAMLConfigModel build() {
            return new SAMLConfigModel(
                    id,
                    this.createdAt,
                    this.lastUpdated,
                    this.enabled,
                    this.forceAuth,
                    this.metadataUrl,
                    this.metadataFileName,
                    this.metadataMode,
                    this.encryptionCertFileName,
                    this.encryptionPrivateKeyFileName,
                    this.signingCertFileName,
                    this.signingPrivateKeyFileName,
                    this.verificationCertFileName);
        }
    }
}
