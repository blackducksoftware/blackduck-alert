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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.UploadValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.ValidationResult;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.ExistenceModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class UploadEndpointManager {
    public static final String UPLOAD_ENDPOINT_URL = "/api/uploads";
    public static final String NO_UPLOAD_FUNCTIONALITY_REGISTERED = "No upload functionality has been created for this endpoint.";
    public static final String CUSTOM_ENDPOINT_ALREADY_REGISTERED = "A custom endpoint is already registered for ";
    private final Logger logger = LoggerFactory.getLogger(UploadEndpointManager.class);
    private final Map<String, UploadTarget> uploadTargets = new HashMap<>();
    private final FilePersistenceUtil filePersistenceUtil;
    private final AuthorizationManager authorizationManager;

    @Autowired
    public UploadEndpointManager(FilePersistenceUtil filePersistenceUtil, AuthorizationManager authorizationManager) {
        this.filePersistenceUtil = filePersistenceUtil;
        this.authorizationManager = authorizationManager;
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

    public void performUpload(String targetKey, Resource fileResource) {
        throwNotImplementedExceptionIfMissing(targetKey);

        UploadTarget target = uploadTargets.get(targetKey);
        throwForbiddenExceptionIfPermissionMissing(authorizationManager::hasUploadWritePermission, target);
        writeFile(target, fileResource);
    }

    public ExistenceModel checkExists(String targetKey) {
        throwNotImplementedExceptionIfMissing(targetKey);

        UploadTarget target = uploadTargets.get(targetKey);

        throwForbiddenExceptionIfPermissionMissing(authorizationManager::hasUploadReadPermission, target);
        String targetFilename = target.getFilename();
        Boolean exists = filePersistenceUtil.uploadFileExists(targetFilename);
        return new ExistenceModel(exists);
    }

    public void deleteUploadedFile(String targetKey) {
        throwNotImplementedExceptionIfMissing(targetKey);

        UploadTarget target = uploadTargets.get(targetKey);
        throwForbiddenExceptionIfPermissionMissing(authorizationManager::hasUploadDeletePermission, target);

        try {
            String targetFilename = target.getFilename();
            File fileToValidate = filePersistenceUtil.createUploadsFile(targetFilename);
            filePersistenceUtil.delete(fileToValidate);
        } catch (IOException ex) {
            logger.error("Error deleting file - file: {}, context: {}, descriptor: {} ", target.getFilename(), target.getContext(), target.getDescriptorKey().getUniversalKey());
            logger.error("Error deleting file caused by: ", ex);
            throw ResponseFactory.createInternalServerErrorException("Error deleting uploaded file from server.");
        }
    }

    private void writeFile(UploadTarget target, Resource fileResource) {
        try {
            String targetFilename = target.getFilename();
            String tempFilename = "temp_" + targetFilename;

            Optional<UploadValidationFunction> validationFunction = target.getValidationFunction();
            if (validationFunction.isPresent()) {
                writeFile(tempFilename, fileResource);
                File tempFileToValidate = filePersistenceUtil.createUploadsFile(tempFilename);
                ValidationResult validationResult = validationFunction.get().apply(tempFileToValidate);
                filePersistenceUtil.delete(tempFileToValidate);
                if (validationResult.hasErrors()) {
                    throw ResponseFactory.createBadRequestException(validationResult.combineErrorMessages());
                }
            }
            writeFile(targetFilename, fileResource);
        } catch (IOException ex) {
            logger.error("Error uploading file - file: {}, context: {}, descriptor: {} ", target.getFilename(), target.getContext(), target.getDescriptorKey().getUniversalKey());
            logger.error("Error uploading file caused by: ", ex);
            throw ResponseFactory.createInternalServerErrorException("Error uploading file to server.");
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

    private void throwNotImplementedExceptionIfMissing(String targetKey) {
        if (!containsTarget(targetKey)) {
            throw ResponseFactory.createNotImplementedException(NO_UPLOAD_FUNCTIONALITY_REGISTERED);
        }
    }

    private void throwForbiddenExceptionIfPermissionMissing(BiFunction<String, String, Boolean> permissionChecker, UploadTarget target) {
        if (!permissionChecker.apply(target.getContext().name(), target.getDescriptorKey().getUniversalKey())) {
            throw ResponseFactory.createForbiddenException();
        }
    }

    private class UploadTarget {
        private final ConfigContextEnum context;
        private final DescriptorKey descriptorKey;
        private final String filename;
        private final UploadValidationFunction validationFunction;

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
