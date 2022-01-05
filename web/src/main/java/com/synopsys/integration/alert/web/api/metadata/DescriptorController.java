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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.api.metadata.model.DescriptorsResponseModel;

@RestController
@RequestMapping(DescriptorController.BASE_PATH)
public class DescriptorController {
    public static final String BASE_PATH = MetadataControllerConstants.METADATA_BASE_PATH + "/descriptors";

    private final DescriptorMetadataActions descriptorMetadataActions;

    @Autowired
    public DescriptorController(DescriptorMetadataActions descriptorMetadataActions) {
        this.descriptorMetadataActions = descriptorMetadataActions;
    }

    @GetMapping
    public DescriptorsResponseModel getDescriptors(@RequestParam(required = false) String name, @RequestParam(required = false) String type, @RequestParam(required = false) String context) {
        return ResponseFactory.createContentResponseFromAction(descriptorMetadataActions.getDescriptorsByPermissions(name, type, context));
    }

}
