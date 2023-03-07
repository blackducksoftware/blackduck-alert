package com.synopsys.integration.alert.common.rest.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.descriptor.model.DescriptorKey;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;


public class FileUploadHelper {
    private final Logger logger = AlertLoggerFactory.getLogger(getClass());
    private final AuthorizationManager authorizationManager;
    private final ConfigContextEnum context;
    private final DescriptorKey descriptorKey;
    private final FilePersistenceUtil filePersistenceUtil;

    private static final String FILE_UPLOAD_ACTION_TEMPLATE = "File upload action {}: ";
    private static final String ACTION_CALLED_TEMPLATE = FILE_UPLOAD_ACTION_TEMPLATE + "{} called.";
    private static final String ACTION_SUCCESS_TEMPLATE = FILE_UPLOAD_ACTION_TEMPLATE + "{} completed successfully.";
    private static final String ACTION_NOT_FOUND_TEMPLATE = FILE_UPLOAD_ACTION_TEMPLATE + "{} not found.";
    private static final String ACTION_BAD_REQUEST_TEMPLATE = FILE_UPLOAD_ACTION_TEMPLATE + "{} bad request.";
    private static final String ACTION_MISSING_PERMISSIONS_TEMPLATE = FILE_UPLOAD_ACTION_TEMPLATE + "{} missing permissions.";

    private static final String ACTION_NAME_FILE_EXISTS = "fileExists";
    private static final String ACTION_NAME_FILE_UPLOAD = "fileUpload";
    private static final String ACTION_NAME_FILE_DELETE = "fileDelete";

    public FileUploadHelper (AuthorizationManager authorizationManager, ConfigContextEnum context, DescriptorKey descriptorKey, FilePersistenceUtil filePersistenceUtil) {
        this.authorizationManager = authorizationManager;
        this.context = context;
        this.descriptorKey = descriptorKey;
        this.filePersistenceUtil = filePersistenceUtil;
    }

    public ActionResponse<Boolean> fileExists(String fileName) {
        logger.trace(ACTION_CALLED_TEMPLATE, descriptorKey.getUniversalKey(), ACTION_NAME_FILE_EXISTS);
        if (!authorizationManager.hasUploadReadPermission(context, descriptorKey)) {
            logger.trace(ACTION_MISSING_PERMISSIONS_TEMPLATE, descriptorKey.getUniversalKey(), ACTION_NAME_FILE_EXISTS);
            return ActionResponse.createForbiddenResponse();
        }

        boolean exists = filePersistenceUtil.uploadFileExists(fileName);

        if (!exists) {
            logger.trace(ACTION_NOT_FOUND_TEMPLATE, descriptorKey.getUniversalKey(), ACTION_NAME_FILE_EXISTS);
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        logger.trace(ACTION_SUCCESS_TEMPLATE, descriptorKey.getUniversalKey(), ACTION_NAME_FILE_EXISTS);
        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    public ActionResponse<Void> fileUpload(String fileName, Resource resource, Supplier<ValidationResponseModel> validator) {
        logger.trace(ACTION_CALLED_TEMPLATE, descriptorKey.getUniversalKey(), ACTION_NAME_FILE_UPLOAD);
        if (!authorizationManager.hasUploadWritePermission(context, descriptorKey)) {
            logger.trace(ACTION_MISSING_PERMISSIONS_TEMPLATE, descriptorKey.getUniversalKey(), ACTION_NAME_FILE_UPLOAD);
            return ActionResponse.createForbiddenResponse();
        }

        ValidationResponseModel validationResponseModel = validator.get();
        if (validationResponseModel.hasErrors()) {
            logger.trace(ACTION_BAD_REQUEST_TEMPLATE, descriptorKey.getUniversalKey(), ACTION_NAME_FILE_UPLOAD);
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, validationResponseModel.getMessage());
        }

        try {
            writeFile(fileName, resource);
        } catch (IOException e) {
            logger.error("Error uploading file - file: {}, context: {}, descriptor: {} ", fileName, context, descriptorKey);
            logger.error("Error uploading file caused by: ", e);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading file to server.");
        }

        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    private void writeFile(String fileName, Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            filePersistenceUtil.writeFileToUploadsDirectory(fileName, inputStream);
        } catch (IOException ex) {
            logger.error("Error writing file to resource - file: {}", fileName);
            throw ex;
        }
    }

    public ActionResponse<Void> fileDelete(String fileName) {
        logger.trace(ACTION_CALLED_TEMPLATE, descriptorKey.getUniversalKey(), ACTION_NAME_FILE_DELETE);
        if (!authorizationManager.hasUploadDeletePermission(context, descriptorKey)) {
            logger.trace(ACTION_MISSING_PERMISSIONS_TEMPLATE, descriptorKey.getUniversalKey(), ACTION_NAME_FILE_DELETE);
            return ActionResponse.createForbiddenResponse();
        }

        try {
            if (filePersistenceUtil.uploadFileExists(fileName)) {
                File fileToValidate = filePersistenceUtil.createUploadsFile(fileName);
                filePersistenceUtil.delete(fileToValidate);
            }
        } catch (IOException ex) {
            logger.error("Error deleting file - file: {}, context: {}, descriptor: {} ", fileName, context, descriptorKey);
            logger.error("Error deleting file caused by:", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting uploaded file from server.");
        }

        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }
}
