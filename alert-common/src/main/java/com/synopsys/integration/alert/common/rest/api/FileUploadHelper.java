package com.synopsys.integration.alert.common.rest.api;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

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
            logger.error(String.format("Error uploading file - file: %s, context: %s, descriptor: %s ", fileName, context, descriptorKey));
            logger.error(String.format("Error uploading file caused by: %s", e));
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading file to server.");
        }

        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    private void writeFile(String fileName, Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            filePersistenceUtil.writeFileToUploadsDirectory(fileName, inputStream);
        } catch (IOException ex) {
            logger.error(String.format("Error writing file to resource - file: %s", fileName));
            throw ex;
        }
    }
}
