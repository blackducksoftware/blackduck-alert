/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.saml;

import java.util.Optional;

import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckSSOConfigView extends BlackDuckView {
    public static final String SSO_IDP_METADATA_LINK_KEY = "sso-idp-metadata";

    private Boolean ssoEnabled;
    private String spEntityId;
    private String idpMetadataUrl;
    private Boolean idpMetadataFileUploaded;
    private Boolean groupSynchronizationEnabled;
    private Boolean localLogoutEnabled;
    private String spExternalUrl;
    private Boolean userCreationEnabled;
    private String samlMetadataUrl;

    private BlackDuckSSOConfigView() {
        // For serialization
    }

    public BlackDuckSSOConfigView(Boolean ssoEnabled, String spEntityId, String idpMetadataUrl, Boolean idpMetadataFileUploaded, Boolean groupSynchronizationEnabled, Boolean localLogoutEnabled, String spExternalUrl,
        Boolean userCreationEnabled, String samlMetadataUrl) {
        this.ssoEnabled = ssoEnabled;
        this.spEntityId = spEntityId;
        this.idpMetadataUrl = idpMetadataUrl;
        this.idpMetadataFileUploaded = idpMetadataFileUploaded;
        this.groupSynchronizationEnabled = groupSynchronizationEnabled;
        this.localLogoutEnabled = localLogoutEnabled;
        this.spExternalUrl = spExternalUrl;
        this.userCreationEnabled = userCreationEnabled;
        this.samlMetadataUrl = samlMetadataUrl;
    }

    public Boolean getSsoEnabled() {
        return ssoEnabled;
    }

    public String getSpEntityId() {
        return spEntityId;
    }

    public String getIdpMetadataUrl() {
        return idpMetadataUrl;
    }

    public Boolean getIdpMetadataFileUploaded() {
        return idpMetadataFileUploaded;
    }

    public Boolean getGroupSynchronizationEnabled() {
        return groupSynchronizationEnabled;
    }

    public Boolean getLocalLogoutEnabled() {
        return localLogoutEnabled;
    }

    public String getSpExternalUrl() {
        return spExternalUrl;
    }

    public Boolean getUserCreationEnabled() {
        return userCreationEnabled;
    }

    public String getSamlMetadataUrl() {
        return samlMetadataUrl;
    }

    public HttpUrl metaSSOIdpMetadataLink() {
        return getFirstLink(SSO_IDP_METADATA_LINK_KEY);
    }

    public Optional<HttpUrl> metaSSOIdpMetadataSafeLink() {
        return getFirstLinkSafely(SSO_IDP_METADATA_LINK_KEY);
    }

}
