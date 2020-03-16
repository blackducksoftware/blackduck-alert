/**
 * alert-common
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.UploadValidationFunction;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class UploadEndpointManager {
    public static final String UPLOAD_ENDPOINT_URL = "/api/uploads";
    public static final String NO_UPLOAD_FUNCTIONALITY_REGISTERED = "No upload functionality has been created for this endpoint.";
    public static final String CUSTOM_ENDPOINT_ALREADY_REGISTERED = "A custom endpoint is already registered for ";
    private static final Logger logger = LoggerFactory.getLogger(UploadEndpointManager.class);
    private Map<String, UploadTarget> uploadTargets = new HashMap<>();
    private FilePersistenceUtil filePersistenceUtil;
    private AuthorizationManager authorizationManager;
    private ResponseFactory responseFactory;
    private Gson gson;

    @Autowired
    public UploadEndpointManager(Gson gson, FilePersistenceUtil filePersistenceUtil, AuthorizationManager authorizationManager, ResponseFactory responseFactory) {
        this.filePersistenceUtil = filePersistenceUtil;
        this.authorizationManager = authorizationManager;
        this.responseFactory = responseFactory;
        this.gson = gson;
    }

    public boolean containsTarget(String targetKey) {
        return uploadTargets.containsKey(targetKey);
    }

    public void registerTarget(String targetKey, ConfigContextEnum context, DescriptorKey descriptorKey, String fileName) throws AlertException {
        registerTarget(targetKey, context, descriptorKey, fileName, null);
    }

    public void registerTarget(String targetKey, ConfigContextEnum context, DescriptorKey descriptorKey, String fileName, UploadValidationFunction validationFunction) throws AlertException {
        if (containsTarget(targetKey)) {
            throw new AlertException(CUSTOM_ENDPOINT_ALREADY_REGISTERED + targetKey);
        }
        uploadTargets.put(targetKey, new UploadTarget(context, descriptorKey, fileName, validationFunction));
    }

    public void unRegisterTarget(String targetKey) throws AlertException {
        if (!containsTarget(targetKey)) {
            throw new AlertException(CUSTOM_ENDPOINT_ALREADY_REGISTERED + targetKey);
        }
        uploadTargets.remove(targetKey);
    }

    public ResponseEntity<String> performUpload(String targetKey, Resource fileResource) {
        if (!containsTarget(targetKey)) {
            return new ResponseEntity(NO_UPLOAD_FUNCTIONALITY_REGISTERED, HttpStatus.NOT_IMPLEMENTED);
        }

        UploadTarget target = uploadTargets.get(targetKey);
        if (!authorizationManager.hasUploadWritePermission(target.getContext().name(), target.getDescriptorKey().getUniversalKey())) {
            return responseFactory.createForbiddenResponse();
        }

        return writeFile(target, fileResource);
    }

    public ResponseEntity<String> checkExists(String targetKey) {
        if (!containsTarget(targetKey)) {
            return new ResponseEntity(NO_UPLOAD_FUNCTIONALITY_REGISTERED, HttpStatus.NOT_IMPLEMENTED);
        }

        UploadTarget target = uploadTargets.get(targetKey);
        if (!authorizationManager.hasUploadReadPermission(target.getContext().name(), target.getDescriptorKey().getUniversalKey())) {
            return responseFactory.createForbiddenResponse();
        }
        String targetFilename = target.getFilename();
        Boolean exists = filePersistenceUtil.uploadFileExists(targetFilename);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("exists", exists);
        return responseFactory.createOkContentResponse(gson.toJson(jsonObject));
    }

    public ResponseEntity<String> deleteUploadedFile(String targetKey) {
        if (!containsTarget(targetKey)) {
            return new ResponseEntity(NO_UPLOAD_FUNCTIONALITY_REGISTERED, HttpStatus.NOT_IMPLEMENTED);
        }

        UploadTarget target = uploadTargets.get(targetKey);
        if (!authorizationManager.hasUploadDeletePermission(target.getContext().name(), target.getDescriptorKey().getUniversalKey())) {
            return responseFactory.createForbiddenResponse();
        }

        try {
            String targetFilename = target.getFilename();
            File fileToValidate = filePersistenceUtil.createUploadsFile(targetFilename);
            filePersistenceUtil.delete(fileToValidate);
            return responseFactory.createNoContentResponse();
        } catch (IOException ex) {
            logger.error("Error deleting file - file: {}, context: {}, descriptor: {} ", target.getFilename(), target.getContext(), target.getDescriptorKey().getUniversalKey());
            logger.error("Error deleting file caused by: ", ex);
            return responseFactory.createInternalServerErrorResponse("", "Error deleting uploaded file from server.");
        }
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
            logger.error("Error uploading file caused by: ", ex);
            return responseFactory.createInternalServerErrorResponse("", "Error uploading file to server.");
        }

    }

    private void writeFile(String fileName, Resource fileResource) throws IOException {
        try (InputStream inputStream = fileResource.getInputStream()) {
            filePersistenceUtil.writeFileToUploadsDirectory(fileName, inputStream);
        } catch (IOException ex) {
            logger.error("Error writing file to resource - file: {}", fileName);
            throw ex;
        }
    }

    private class UploadTarget {

        private ConfigContextEnum context;
        private DescriptorKey descriptorKey;
        private String filename;
        private UploadValidationFunction validationFunction;

        public UploadTarget(ConfigContextEnum context, DescriptorKey descriptorKey, String filename, UploadValidationFunction validationFunction) {
            this.context = context;
            this.descriptorKey = descriptorKey;
            this.filename = filename;
            this.validationFunction = validationFunction;
        }

        public ConfigContextEnum getContext() {
            return context;
        }

        public DescriptorKey getDescriptorKey() {
            return descriptorKey;
        }

        public String getFilename() {
            return filename;
        }

        public Optional<UploadValidationFunction> getValidationFunction() {
            return Optional.ofNullable(validationFunction);
        }
    }
}
