/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.api.upload;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.synopsys.integration.alert.common.action.UploadEndpointManager;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.ExistenceModel;

@RestController
@RequestMapping(UploadEndpointManager.UPLOAD_ENDPOINT_URL)
public class UploadEndpointController {
    public static final String TARGET_KEY_MISSING = "Must be given the key associated with the custom functionality.";

    private final UploadEndpointManager uploadEndpointManager;

    @Autowired
    public UploadEndpointController(UploadEndpointManager uploadEndpointManager) {
        this.uploadEndpointManager = uploadEndpointManager;
    }

    @GetMapping("/{key}/exists")
    public ExistenceModel checkUploadedFileExists(@PathVariable String key) {
        throwBadRequestExceptionIfBlank(key);
        return uploadEndpointManager.checkExists(key);
    }

    @PostMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void postFileUpload(@PathVariable String key, @RequestParam("file") MultipartFile file) {
        throwBadRequestExceptionIfBlank(key);
        uploadEndpointManager.performUpload(key, file.getResource());
    }

    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUploadedFile(@PathVariable String key) {
        throwBadRequestExceptionIfBlank(key);
        uploadEndpointManager.deleteUploadedFile(key);
    }

    private void throwBadRequestExceptionIfBlank(String key) {
        if (StringUtils.isBlank(key)) {
            throw ResponseFactory.createBadRequestException(TARGET_KEY_MISSING);
        }
    }

}
