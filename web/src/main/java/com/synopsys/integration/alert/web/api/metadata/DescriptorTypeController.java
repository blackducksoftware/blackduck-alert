/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.api.metadata.model.DescriptorTypesResponseModel;

@RestController
@RequestMapping(DescriptorTypeController.TYPES_PATH)
public class DescriptorTypeController {
    public static final String TYPES_PATH = MetadataControllerConstants.METADATA_BASE_PATH + "/descriptorTypes";
    private DescriptorTypeActions actions;

    @Autowired
    public DescriptorTypeController(DescriptorTypeActions actions) {
        this.actions = actions;
    }

    @GetMapping
    public DescriptorTypesResponseModel getTypes() {
        return ResponseFactory.createContentResponseFromAction(actions.getAll());
    }

}
