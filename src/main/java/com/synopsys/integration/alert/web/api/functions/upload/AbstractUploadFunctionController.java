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
package com.synopsys.integration.alert.web.api.functions.upload;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.synopsys.integration.alert.common.action.upload.AbstractUploadAction;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.ExistenceModel;

@RestController
@RequestMapping(AbstractUploadAction.API_FUNCTION_UPLOAD_URL)
public abstract class AbstractUploadFunctionController {
    private final AbstractUploadAction action;

    public AbstractUploadFunctionController(AbstractUploadAction action) {
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
