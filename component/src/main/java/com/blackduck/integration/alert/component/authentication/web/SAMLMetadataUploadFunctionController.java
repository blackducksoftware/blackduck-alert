/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.alert.common.action.upload.AbstractUploadAction;
import com.blackduck.integration.alert.common.rest.api.AbstractUploadFunctionController;

@Controller
@RequestMapping(SAMLMetadataUploadFunctionController.SAML_UPLOAD_URL)
public class SAMLMetadataUploadFunctionController extends AbstractUploadFunctionController {
    public static final String SAML_UPLOAD_URL = AbstractUploadAction.API_FUNCTION_UPLOAD_URL + "/" + AuthenticationDescriptor.KEY_SAML_METADATA_FILE;

    @Autowired
    public SAMLMetadataUploadFunctionController(SamlMetaDataFileUpload action) {
        super(action);
    }
}
