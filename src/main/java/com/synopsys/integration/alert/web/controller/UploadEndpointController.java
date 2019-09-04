package com.synopsys.integration.alert.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.synopsys.integration.alert.common.action.UploadEndpointManager;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;

@RestController
@RequestMapping(UploadEndpointManager.UPLOAD_ENDPOINT_URL)
public class UploadEndpointController {

        private final UploadEndpointManager uploadEndpointManager;
        private final ResponseFactory responseFactory;
        private final AuthorizationManager authorizationManager;
        private final FilePersistenceUtil filePersistenceUtil;

        @Autowired
        public UploadEndpointController(UploadEndpointManager uploadEndpointManager, ResponseFactory responseFactory, AuthorizationManager authorizationManager, FilePersistenceUtil filePersistenceUtil) {
            this.uploadEndpointManager = uploadEndpointManager;
            this.responseFactory = responseFactory;
            this.authorizationManager = authorizationManager;
            this.filePersistenceUtil = filePersistenceUtil;
        }

        @PostMapping("/{key}")
        public ResponseEntity<String> postFileUpload(@PathVariable final String key, @RequestParam("file") MultipartFile file) {
            if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL.name(), SettingsDescriptor.SETTINGS_COMPONENT) ||
                !authorizationManager.hasWritePermission(ConfigContextEnum.GLOBAL.name(), SettingsDescriptor.SETTINGS_COMPONENT)) {
                return responseFactory.createForbiddenResponse();
            }

            if (StringUtils.isBlank(key)) {
                return responseFactory.createBadRequestResponse("", "Must be given the key associated with the custom functionality.");
            }

            return uploadEndpointManager.performFunction(key, file.getResource());
        }
}
