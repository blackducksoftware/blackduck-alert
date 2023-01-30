package com.synopsys.integration.alert.authentication.saml.security;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
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

    public void reconfigureSAMLFromDBConfigModel() {
        Optional<SAMLConfigModel> optionalSAMLConfigModel = configAccessor.getConfiguration();
        if(optionalSAMLConfigModel.isPresent()) {
            SAMLConfigModel samlConfigModel = optionalSAMLConfigModel.get();
            boolean samlEnabled = samlConfigModel.getEnabled().orElse(false);

            if(samlEnabled) {
                enableSAML(samlConfigModel);
            } else {
                disableSAML();
            }
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
        Optional<String> optionalMetadataUrl = configModel.getMetadataUrl();
        RelyingPartyRegistration.Builder builder;

        if (optionalMetadataUrl.isPresent()) {
            builder = RelyingPartyRegistrations.fromMetadataLocation(optionalMetadataUrl.get()).registrationId("default");
        } else {
            String metadataString = filePersistenceUtil.readFromFile(AuthenticationDescriptor.SAML_METADATA_FILE);
            try (InputStream metadataInputStream = new ByteArrayInputStream(metadataString.getBytes())) {
                builder = RelyingPartyRegistrations.fromMetadata(metadataInputStream);
            }
        }

        if (filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE)) {
            signingCredentialBuilder(builder);
        }
        if (filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE)) {
            encryptionCredentialBuilder(builder);
        }
        if (filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE)) {
            verificationCredentialBuilder(builder);
        }

        builder.assertingPartyDetails(party -> party.wantAuthnRequestsSigned(configModel.getWantAssertionsSigned().orElse(false)));

        return builder.build();
    }

    private void signingCredentialBuilder(RelyingPartyRegistration.Builder builder) throws CertificateException, IOException {
        String signingCertString = filePersistenceUtil.readFromFile(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE);
        // Get signing private key
        String signingPrivateKeyString = filePersistenceUtil.readFromFile(AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE);
        try (InputStream signingPrivateKeyInputStream = new ByteArrayInputStream(signingPrivateKeyString.getBytes())) {
            RSAPrivateKey signingRSAPrivateKey = RsaKeyConverters.pkcs8().convert(signingPrivateKeyInputStream);
            X509Certificate signingCert = X509Support.decodeCertificate(signingCertString);
            Saml2X509Credential signingCredential = Saml2X509Credential.signing(signingRSAPrivateKey, signingCert);
            builder.signingX509Credentials(credentials -> credentials.add(signingCredential));
        }
    }

    private void encryptionCredentialBuilder(RelyingPartyRegistration.Builder builder) throws CertificateException, IOException {
        String encryptionCertString = filePersistenceUtil.readFromFile(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE);
        // Get encryption private key
        String encryptionPrivateKeyString = filePersistenceUtil.readFromFile(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE);
        try (InputStream encryptionPrivateKeyInputStream = new ByteArrayInputStream(encryptionPrivateKeyString.getBytes())) {
            RSAPrivateKey encryptionRSAPrivateKey = RsaKeyConverters.pkcs8().convert(encryptionPrivateKeyInputStream);
            X509Certificate encryptionCert = X509Support.decodeCertificate(encryptionCertString);
            Saml2X509Credential encryptionCredential = Saml2X509Credential.encryption(encryptionCert);
            Saml2X509Credential decryptionCredential = Saml2X509Credential.decryption(encryptionRSAPrivateKey, encryptionCert);
            builder
                .decryptionX509Credentials(credentials -> credentials.add(decryptionCredential))
                .assertingPartyDetails(party -> party.encryptionX509Credentials(credentials -> credentials.add(encryptionCredential)));
        }
    }

    private void verificationCredentialBuilder(RelyingPartyRegistration.Builder builder) throws CertificateException, IOException {
        String verificationCertString = filePersistenceUtil.readFromFile(AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE);
        X509Certificate verificationCert = X509Support.decodeCertificate(verificationCertString);
        Saml2X509Credential verificationCredential = Saml2X509Credential.verification(verificationCert);
        builder.assertingPartyDetails(party -> party.verificationX509Credentials(credentials -> credentials.add(verificationCredential)));
    }

//    private RelyingPartyRegistration createRegistration() throws CertificateException, IOException {
//        ClassPathResource privateKeyClassPathResource = new ClassPathResource("okta-key.pem");
//        try (InputStream privateKeyInputStream = privateKeyClassPathResource.getInputStream()) {
//            RSAPrivateKey rsaPrivateKey = RsaKeyConverters.pkcs8().convert(privateKeyInputStream);
//            ClassPathResource verificationClassPathResource = new ClassPathResource("okta-verification.pem");
//            X509Certificate verificationCert = X509Support.decodeCertificate(verificationClassPathResource.getFile());
//            ClassPathResource signingClassPathResource = new ClassPathResource("okta-signing.pem");
//            X509Certificate signingCert = X509Support.decodeCertificate(signingClassPathResource.getFile());
//            ClassPathResource encryptionClassPathResource = new ClassPathResource("okta-encryption.pem");
//            X509Certificate encryptionCert = X509Support.decodeCertificate(encryptionClassPathResource.getFile());
//            Saml2X509Credential verificationCredential = Saml2X509Credential.verification(verificationCert);
//            Saml2X509Credential signingCredential = Saml2X509Credential.signing(rsaPrivateKey, signingCert);
//            Saml2X509Credential encryptionCredential = Saml2X509Credential.encryption(encryptionCert);
//            Saml2X509Credential decryptionCredential = Saml2X509Credential.decryption(rsaPrivateKey, encryptionCert);
//            return RelyingPartyRegistrations.fromMetadataLocation("https://dev-94843441.okta.com/app/exk7fzsr4mPGWbwFW5d7/sso/saml/metadata")
//                .registrationId("okta")
//                .signingX509Credentials(credentials -> credentials.add(signingCredential))
//                .decryptionX509Credentials(credentials -> credentials.add(decryptionCredential))
//                .assertingPartyDetails(party -> party
//                    .encryptionX509Credentials(credentials -> credentials.add(encryptionCredential))
//                    .verificationX509Credentials(credentials -> credentials.add(verificationCredential))
//                    .wantAuthnRequestsSigned(true))
//                .build();
//        }
//    }
}
