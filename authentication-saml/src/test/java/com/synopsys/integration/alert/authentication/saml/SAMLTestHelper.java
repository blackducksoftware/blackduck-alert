package com.synopsys.integration.alert.authentication.saml;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.database.mock.MockSAMLConfigurationRepository;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.model.SAMLMetadataMode;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

import java.util.Map;
import java.util.UUID;

public class SAMLTestHelper {
    public static AuthorizationManager createAuthorizationManager() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), authenticationDescriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
            "admin",
            "admin",
            () -> new PermissionMatrixModel(permissions)
        );
    }

    public static FilePersistenceUtil createFilePersistenceUtil() {
        AlertProperties alertProperties = new MockAlertProperties();
        return new FilePersistenceUtil(alertProperties, new Gson());
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
        private Boolean wantAssertionsSigned = false;
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

        public SAMLConfigModelBuilder setWantAssertionsSigned(Boolean wantAssertionsSigned) {
            this.wantAssertionsSigned = wantAssertionsSigned;
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
                this.wantAssertionsSigned,
                this.encryptionCertFileName,
                this.encryptionPrivateKeyFileName,
                this.signingCertFileName,
                this.signingPrivateKeyFileName,
                this.verificationCertFileName
            );
        }
    }
}
