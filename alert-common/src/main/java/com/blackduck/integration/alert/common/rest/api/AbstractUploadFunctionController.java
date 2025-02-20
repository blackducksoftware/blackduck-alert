/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.blackduck.integration.alert.common.action.upload.AbstractUploadAction;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.model.ExistenceModel;

/**
 * @deprecated Deprecated in 8.x as a result of deprecation of SAMLMetadataUploadFunctionController, planned for removed in 9.0.0.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping(AbstractUploadAction.API_FUNCTION_UPLOAD_URL)
public abstract class AbstractUploadFunctionController {
    private final AbstractUploadAction action;

    protected AbstractUploadFunctionController(AbstractUploadAction action) {
        this.action = action;
    }

    @GetMapping("/exists")
    public ExistenceModel checkUploadedFileExists() {
        return ResponseFactory.createContentResponseFromAction(action.uploadFileExists());
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void postFileUpload(@RequestParam("file") MultipartFile file) {
        ResponseFactory.createResponseFromAction(action.uploadFile(file.getResource()));
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUploadedFile() {
        ResponseFactory.createResponseFromAction(action.deleteFile());
    }
}
