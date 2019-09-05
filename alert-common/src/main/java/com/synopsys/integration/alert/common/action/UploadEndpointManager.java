package com.synopsys.integration.alert.common.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class UploadEndpointManager {
    public static final String UPLOAD_ENDPOINT_URL = "/api/uploads";
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
        if (containsTarget(targetKey)) {
            throw new AlertException("A custom endpoint is already registered for " + targetKey);
        }
        uploadTargets.put(targetKey, new UploadTarget(context, descriptorKey, targetKey, fileName));
    }

    public ResponseEntity<String> performUpload(String targetKey, Resource fileResource) {
        if (!containsTarget(targetKey)) {
            return new ResponseEntity("No upload functionality has been created for this endpoint.", HttpStatus.NOT_IMPLEMENTED);
        }

        UploadTarget target = uploadTargets.get(targetKey);
        // check permissions
        if(!authorizationManager.hasAllPermissions(target.getContext().name(), target.getDescriptorKey().getUniversalKey(), AccessOperation.EXECUTE, AccessOperation.WRITE)) {
            return responseFactory.createForbiddenResponse();
        }

        return writeFile(target, fileResource);
    }

    private ResponseEntity<String> writeFile(UploadTarget target, Resource fileResource) {
        try {
            if (fileResource.isFile()) {
                filePersistenceUtil.writeToFile(target.getFilename(), fileResource.getFile());
                return responseFactory.createCreatedResponse("", "File uploaded.");
            }
        } catch(IOException ex) {
            // add logger to log details.  Don't want to send internal path details back to the client in the response.
            return responseFactory.createInternalServerErrorResponse("", "Error uploading file to server.");
        }
        return responseFactory.createBadRequestResponse("", "The file could not be uploaded.");
    }

    private class UploadTarget {

        private ConfigContextEnum context;
        private DescriptorKey descriptorKey;
        private String targetKey;
        private String filename;

        public UploadTarget(final ConfigContextEnum context, final DescriptorKey descriptorKey, final String targetKey, final String filename) {
            this.context = context;
            this.descriptorKey = descriptorKey;
            this.targetKey = targetKey;
            this.filename = filename;
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
    }
}
