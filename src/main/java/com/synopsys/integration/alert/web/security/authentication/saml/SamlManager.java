/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.security.authentication.saml;

//@Configuration
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

    //    public SAMLAuthenticationProvider samlAuthenticationProvider() {
    //        final SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
    //        samlAuthenticationProvider.setForcePrincipalAsString(false);
    //        return samlAuthenticationProvider;
    //    }
    //
    //    @Bean
    //    public EmptyKeyManager keyManager() {
    //        return new EmptyKeyManager();
    //    }
    //
    //    @Bean
    //    public MetadataGenerator metadataGenerator() {
    //        final MetadataGenerator metadataGenerator = new MetadataGenerator();
    //        metadataGenerator.setEntityId("getSpEntityId");
    //        metadataGenerator.setEntityBaseURL("getSpExternalUrl");
    //        metadataGenerator.setRequestSigned(false);
    //        metadataGenerator.setWantAssertionSigned(false);
    //        metadataGenerator.setExtendedMetadata(extendedMetadata());
    //        metadataGenerator.setIncludeDiscoveryExtension(false);
    //        metadataGenerator.setKeyManager(keyManager());
    //        metadataGenerator.setBindingsSLO(Collections.emptyList());
    //        metadataGenerator.setBindingsSSO(Arrays.asList("post"));
    //        metadataGenerator.setNameID(Arrays.asList(NameIDType.UNSPECIFIED));
    //        return metadataGenerator;
    //    }
    //
    //    @Bean
    //    public ExtendedMetadata extendedMetadata() {
    //        final ExtendedMetadata extendedMetadata = new ExtendedMetadata();
    //        extendedMetadata.setIdpDiscoveryEnabled(false);
    //        extendedMetadata.setSignMetadata(false);
    //        extendedMetadata.setEcpEnabled(true);
    //        extendedMetadata.setRequireLogoutRequestSigned(false);
    //        return extendedMetadata;
    //    }

}
