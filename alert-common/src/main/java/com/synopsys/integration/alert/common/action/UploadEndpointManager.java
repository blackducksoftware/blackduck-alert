/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.field.UploadValidationFunction;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class UploadEndpointManager {
    public static final String UPLOAD_ENDPOINT_URL = "/api/uploads";
    private static final Logger logger = LoggerFactory.getLogger(UploadEndpointManager.class);
    private Map<String, UploadTarget> uploadTargets = new HashMap<>();
    private FilePersistenceUtil filePersistenceUtil;
    private AuthorizationManager authorizationManager;
    private ResponseFactory responseFactory;

    @Autowired
    public UploadEndpointManager(FilePersistenceUtil filePersistenceUtil, AuthorizationManager authorizationManager, ResponseFactory responseFactory) {
        this.filePersistenceUtil = filePersistenceUtil;
        this.authorizationManager = authorizationManager;
        this.responseFactory = responseFactory;
    }

    public boolean containsTarget(String targetKey) {
        return uploadTargets.containsKey(targetKey);
    }

    public void registerTarget(String targetKey, ConfigContextEnum context, DescriptorKey descriptorKey, String fileName) throws AlertException {
        registerTarget(targetKey, context, descriptorKey, fileName, null);
    }

    public void registerTarget(String targetKey, ConfigContextEnum context, DescriptorKey descriptorKey, String fileName, UploadValidationFunction validationFunction) throws AlertException {
        if (containsTarget(targetKey)) {
            throw new AlertException("A custom endpoint is already registered for " + targetKey);
        }
        uploadTargets.put(targetKey, new UploadTarget(context, descriptorKey, targetKey, fileName, validationFunction));
    }

    public ResponseEntity<String> performUpload(String targetKey, Resource fileResource) {
        if (!containsTarget(targetKey)) {
            return new ResponseEntity("No upload functionality has been created for this endpoint.", HttpStatus.NOT_IMPLEMENTED);
        }

        UploadTarget target = uploadTargets.get(targetKey);
        if (!authorizationManager.hasWritePermission(target.getContext().name(), target.getDescriptorKey().getUniversalKey())) {
            return responseFactory.createForbiddenResponse();
        }

        return writeFile(target, fileResource);
    }

    private ResponseEntity<String> writeFile(UploadTarget target, Resource fileResource) {
        try {
            String targetFilename = target.getFilename();
            String tempFilename = "temp_" + targetFilename;

            Optional<UploadValidationFunction> validationFunction = target.getValidationFunction();
            if (validationFunction.isPresent()) {
                writeFile(tempFilename, fileResource);
                File fileToValidate = filePersistenceUtil.createUploadsFile(tempFilename);
                Collection<String> errors = validationFunction.get().apply(fileToValidate);
                filePersistenceUtil.delete(fileToValidate);
                // TODO Implement a ValidationResult object that UploadValidationFunction and ConfigValidationFunction can return.
                if (errors.isEmpty()) {
                    writeFile(targetFilename, fileResource);
                    return responseFactory.createCreatedResponse("", "File uploaded.");
                }
                return responseFactory.createBadRequestResponse("", StringUtils.join(errors, ","));
            } else {
                writeFile(targetFilename, fileResource);
                return responseFactory.createCreatedResponse("", "File uploaded.");
            }
        } catch (IOException ex) {
            logger.error("Error uploading file - file: {}, context: {}, descriptor: {} ", target.getFilename(), target.getContext(), target.getDescriptorKey().getUniversalKey());
            logger.error("Caused by: ", ex);
            return responseFactory.createInternalServerErrorResponse("", "Error uploading file to server.");
        }

    }

    private void writeFile(String fileName, Resource fileResource) throws IOException {
        try (InputStream inputStream = fileResource.getInputStream()) {
            filePersistenceUtil.writeFileToUploadsDirectory(fileName, inputStream);
        } catch (IOException ex) {
            logger.error("Error uploading file - file: {}, context: {}, descriptor: {} ", fileName);
            logger.error("Caused by: ", ex);
            throw ex;
        }
    }

    private class UploadTarget {

        private ConfigContextEnum context;
        private DescriptorKey descriptorKey;
        private String targetKey;
        private String filename;
        private UploadValidationFunction validationFunction;

        public UploadTarget(ConfigContextEnum context, DescriptorKey descriptorKey, String targetKey, String filename, UploadValidationFunction validationFunction) {
            this.context = context;
            this.descriptorKey = descriptorKey;
            this.targetKey = targetKey;
            this.filename = filename;
            this.validationFunction = validationFunction;
        }

        public ConfigContextEnum getContext() {
            return context;
        }

        public DescriptorKey getDescriptorKey() {
            return descriptorKey;
        }

        public String getTargetKey() {
            return targetKey;
        }

        public String getFilename() {
            return filename;
        }

        public Optional<UploadValidationFunction> getValidationFunction() {
            return Optional.ofNullable(validationFunction);
        }
    }
}
