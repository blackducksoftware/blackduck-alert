package com.synopsys.integration.alert.authentication.saml.security;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.model.SAMLMetadataMode;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.security.x509.X509Support;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Optional;

@Component
public class SAMLManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SAMLConfigAccessor configAccessor;

    private final AlertRelyingPartyRegistrationRepository alertRelyingPartyRegistrationRepository;
    private final FilePersistenceUtil filePersistenceUtil;

    @Autowired
    public SAMLManager(
        SAMLConfigAccessor configAccessor,
        AlertRelyingPartyRegistrationRepository alertRelyingPartyRegistrationRepository,
        FilePersistenceUtil filePersistenceUtil
    ) {
        this.configAccessor = configAccessor;
        this.alertRelyingPartyRegistrationRepository = alertRelyingPartyRegistrationRepository;
        this.filePersistenceUtil = filePersistenceUtil;
    }

    public void reconfigureSAML() {
        Optional<SAMLConfigModel> optionalSAMLConfigModel = configAccessor.getConfiguration();
        if (optionalSAMLConfigModel.isPresent()) {
            SAMLConfigModel samlConfigModel = optionalSAMLConfigModel.get();
            boolean samlEnabled = samlConfigModel.getEnabled();

            if(samlEnabled) {
                enableSAML(samlConfigModel);
            } else {
                disableSAML();
            }
        } else {
            disableSAML();
        }
    }

    private void enableSAML(SAMLConfigModel configModel) {
        try {
            RelyingPartyRegistration relyingPartyRegistration = createRegistration(configModel);
            alertRelyingPartyRegistrationRepository.registerRelyingPartyRegistration(relyingPartyRegistration);
        } catch(CertificateException | IOException ex) {
            logger.error("Error enabling saml due to certificate issue.", ex);
        }
    }

    private void disableSAML() {
        alertRelyingPartyRegistrationRepository.unregisterRelyingPartyRegistration();
    }

    private RelyingPartyRegistration createRegistration(SAMLConfigModel configModel) throws CertificateException, IOException {
        SAMLMetadataMode metadataMode = configModel.getMetadataMode().orElse(SAMLMetadataMode.URL); // Default to url
        Optional<String> optionalMetadataUrl = configModel.getMetadataUrl().filter(StringUtils::isNotBlank);
        RelyingPartyRegistration.Builder builder;

        // Make sure the mode for SAML metadata is url before using it
        if (metadataMode == SAMLMetadataMode.URL && optionalMetadataUrl.isPresent()) {
            builder = RelyingPartyRegistrations.fromMetadataLocation(optionalMetadataUrl.get());
        } else {
            String metadataString = filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_METADATA_FILE);
            try (InputStream metadataInputStream = new ByteArrayInputStream(metadataString.getBytes())) {
                builder = RelyingPartyRegistrations.fromMetadata(metadataInputStream);
            }
        }
        builder.registrationId("default");

        if (filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE)) {
            signingCredentialBuilder(builder);
        }
        if (filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE)) {
            encryptionCredentialBuilder(builder);
        }
        if (filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE)) {
            verificationCredentialBuilder(builder);
        }

        builder.assertingPartyDetails(party -> party.wantAuthnRequestsSigned(configModel.getWantAssertionsSigned()));

        return builder.build();
    }

    private void signingCredentialBuilder(RelyingPartyRegistration.Builder builder) throws CertificateException, IOException {
        String signingCertString = filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE);
        // Get signing private key
        String signingPrivateKeyString = filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE);
        try (InputStream signingPrivateKeyInputStream = new ByteArrayInputStream(signingPrivateKeyString.getBytes())) {
            RSAPrivateKey signingRSAPrivateKey = RsaKeyConverters.pkcs8().convert(signingPrivateKeyInputStream);
            X509Certificate signingCert = X509Support.decodeCertificate(signingCertString.getBytes());
            Saml2X509Credential signingCredential = Saml2X509Credential.signing(signingRSAPrivateKey, signingCert);
            builder.signingX509Credentials(credentials -> credentials.add(signingCredential));
        }
    }

    private void encryptionCredentialBuilder(RelyingPartyRegistration.Builder builder) throws CertificateException, IOException {
        String encryptionCertString = filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE);
        // Get encryption private key
        String encryptionPrivateKeyString = filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE);
        try (InputStream encryptionPrivateKeyInputStream = new ByteArrayInputStream(encryptionPrivateKeyString.getBytes())) {
            RSAPrivateKey encryptionRSAPrivateKey = RsaKeyConverters.pkcs8().convert(encryptionPrivateKeyInputStream);
            X509Certificate encryptionCert = X509Support.decodeCertificate(encryptionCertString.getBytes());
            Saml2X509Credential encryptionCredential = Saml2X509Credential.encryption(encryptionCert);
            Saml2X509Credential decryptionCredential = Saml2X509Credential.decryption(encryptionRSAPrivateKey, encryptionCert);
            builder
                .decryptionX509Credentials(credentials -> credentials.add(decryptionCredential))
                .assertingPartyDetails(party -> party.encryptionX509Credentials(credentials -> credentials.add(encryptionCredential)));
        }
    }

    private void verificationCredentialBuilder(RelyingPartyRegistration.Builder builder) throws CertificateException, IOException {
        String verificationCertString = filePersistenceUtil.readFromUploadsFile(AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE);
        X509Certificate verificationCert = X509Support.decodeCertificate(verificationCertString.getBytes());
        Saml2X509Credential verificationCredential = Saml2X509Credential.verification(verificationCert);
        builder.assertingPartyDetails(party -> party.verificationX509Credentials(credentials -> credentials.add(verificationCredential)));
    }
}
