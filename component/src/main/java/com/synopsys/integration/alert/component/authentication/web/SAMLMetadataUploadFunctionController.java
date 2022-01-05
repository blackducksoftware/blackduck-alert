/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.synopsys.integration.alert.common.action.upload.AbstractUploadAction;
import com.synopsys.integration.alert.common.rest.api.AbstractUploadFunctionController;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;

@Controller
@RequestMapping(SAMLMetadataUploadFunctionController.SAML_UPLOAD_URL)
public class SAMLMetadataUploadFunctionController extends AbstractUploadFunctionController {
    public static final String SAML_UPLOAD_URL = AbstractUploadAction.API_FUNCTION_UPLOAD_URL + "/" + AuthenticationDescriptor.KEY_SAML_METADATA_FILE;

    @Autowired
    public SAMLMetadataUploadFunctionController(SamlMetaDataFileUpload action) {
        super(action);
    }
}
