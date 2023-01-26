package com.synopsys.integration.alert.authentication.saml.security;

import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import org.opensaml.security.x509.X509Support;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Optional;

//@Component
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
                enableSAML();
            } else {
                disableSAML();
            }
        }
    }

    private void enableSAML() {
        try {
            RelyingPartyRegistration relyingPartyRegistration = createRegistration();
            alertRelyingPartyRegistrationRepository.registerRelyingPartyRegistration(relyingPartyRegistration);
        } catch(CertificateException | IOException ex) {
            logger.error("Error enabling saml due to certificate issue.", ex);
        }
    }

    private void disableSAML() {
    }

    private RelyingPartyRegistration createRegistration() throws CertificateException, IOException {
        ClassPathResource privateKeyClassPathResource = new ClassPathResource("okta-key.pem");
        try (InputStream privateKeyInputStream = privateKeyClassPathResource.getInputStream()) {
            RSAPrivateKey rsaPrivateKey = RsaKeyConverters.pkcs8().convert(privateKeyInputStream);
            ClassPathResource verificationClassPathResource = new ClassPathResource("okta-verification.pem");
            X509Certificate verificationCert = X509Support.decodeCertificate(verificationClassPathResource.getFile());
            ClassPathResource signingClassPathResource = new ClassPathResource("okta-signing.pem");
            X509Certificate signingCert = X509Support.decodeCertificate(signingClassPathResource.getFile());
            ClassPathResource encryptionClassPathResource = new ClassPathResource("okta-encryption.pem");
            X509Certificate encryptionCert = X509Support.decodeCertificate(encryptionClassPathResource.getFile());
            Saml2X509Credential verificationCredential = Saml2X509Credential.verification(verificationCert);
            Saml2X509Credential signingCredential = Saml2X509Credential.signing(rsaPrivateKey, signingCert);
            Saml2X509Credential encryptionCredential = Saml2X509Credential.encryption(encryptionCert);
            Saml2X509Credential decryptionCredential = Saml2X509Credential.decryption(rsaPrivateKey, encryptionCert);
            return RelyingPartyRegistrations.fromMetadataLocation("https://dev-94843441.okta.com/app/exk7fzsr4mPGWbwFW5d7/sso/saml/metadata")
                .registrationId("okta")
                .signingX509Credentials(credentials -> credentials.add(signingCredential))
                .decryptionX509Credentials(credentials -> credentials.add(decryptionCredential))
                .assertingPartyDetails(party -> party
                    .encryptionX509Credentials(credentials -> credentials.add(encryptionCredential))
                    .verificationX509Credentials(credentials -> credentials.add(verificationCredential))
                    .wantAuthnRequestsSigned(true))
                .build();
        }
    }
}
