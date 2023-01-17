package com.synopsys.integration.alert.common.rest.api;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.rest.model.ExistenceModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

public class FileUploadHelper {
    private final Logger logger = AlertLoggerFactory.getLogger(getClass());
    private final AuthorizationManager authorizationManager;
    private final ConfigContextEnum context;
    private final DescriptorKey descriptorKey;

    private static final String FILE_UPLOAD_ACTION_TEMPLATE = "File upload action {}: ";
    private static final String ACTION_CALLED_TEMPLATE = FILE_UPLOAD_ACTION_TEMPLATE + "{} called.";
    private static final String ACTION_SUCCESS_TEMPLATE = FILE_UPLOAD_ACTION_TEMPLATE + "{} completed successfully.";
    private static final String ACTION_NOT_FOUND_TEMPLATE = FILE_UPLOAD_ACTION_TEMPLATE + "{} not found.";
    private static final String ACTION_BAD_REQUEST_TEMPLATE = FILE_UPLOAD_ACTION_TEMPLATE + "{} bad request.";
    private static final String ACTION_MISSING_PERMISSIONS_TEMPLATE = FILE_UPLOAD_ACTION_TEMPLATE + "{} missing permissions.";

    public FileUploadHelper (AuthorizationManager authorizationManager, ConfigContextEnum context, DescriptorKey descriptorKey) {
        this.authorizationManager = authorizationManager;
        this.context = context;
        this.descriptorKey = descriptorKey;
    }

    public ActionResponse<ExistenceModel> fileExists(Supplier<ExistenceModel> modelSupplier) {
        String actionName = "fileExists";
        logger.trace(ACTION_CALLED_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        if (!authorizationManager.hasUploadReadPermission(context, descriptorKey)) {
            logger.trace(ACTION_MISSING_PERMISSIONS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return ActionResponse.createForbiddenResponse();
        }

        ExistenceModel existenceModel = modelSupplier.get();

        if (!existenceModel.getExists().booleanValue()) {
            logger.trace(ACTION_NOT_FOUND_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        logger.trace(ACTION_SUCCESS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }
}
