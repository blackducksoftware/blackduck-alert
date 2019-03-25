package com.synopsys.integration.alert.web.security.authentication.saml;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.SAMLAuthenticationProvider;

@Configuration
public class SamlManager {

    public static final String SAML_SP_ENTITYID = "alert.saml.sp.entity.id";

    public static final String SAML_SSO_ENABLED = "alert.saml.sso.enabled";

    public static final String SAML_GROUP_SYNCH_ENABLED = "alert.saml.group.synch.enabled";

    public static final String SAML_LOCAL_LOGOUT_ENABLED = "alert.saml.local.logout.enabled";

    public static final String SAML_IDP_METADATA = "alert.saml.idp.metadata";

    public static final String SAML_IDP_METADATAURL = "alert.saml.idp.metadata.url";

    public static final String SAML_IDP_METADATA_FILE = "alert.saml.idp.metadataFile";

    public static final String SAML_KEYSTORE = "alert.saml.ssl.keyStore";

    public static final String SAML_KEYSTORE_PASSWORD = "alert.saml.ssl.keyStorePassword";

    public static final String SAML_PRIVATEKEY_ALIAS = "alert.saml.ssl.privateKeyAlias";

    public static final String SAML_PRIVATEKEY_PASSWORD = "alert.saml.ssl.privateKeyPassword";

    public static final String SAML_TRUSTED_CERT_ALIAS = "alert.saml.ssl.trustedCertAlias";

    public static final String SAML_SP_EXTERNAL_URL = "alert.saml.sp.externalUrl";

    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        final SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }

}
