/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.saml;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class BlackDuckSSOConfigResponseModel extends AlertSerializableModel {
    private Boolean ssoEnabled;
    private String spEntityId;
    private String idpMetadataUrl;
    private Boolean idpMetadataFileUploaded;
    private Boolean groupSynchronizationEnabled;
    private Boolean localLogoutEnabled;
    private String spExternalUrl;
    private Boolean userCreationEnabled;
    private String samlMetadataUrl;

    private BlackDuckSSOConfigResponseModel() {
        // For serialization
    }

    public BlackDuckSSOConfigResponseModel(
        Boolean ssoEnabled,
        String spEntityId,
        String idpMetadataUrl,
        Boolean idpMetadataFileUploaded,
        Boolean groupSynchronizationEnabled,
        Boolean localLogoutEnabled,
        String spExternalUrl,
        Boolean userCreationEnabled,
        String samlMetadataUrl
    ) {
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

}
