package com.synopsys.integration.alert.common.action;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;

@Component
public class UploadEndpointManager {
    public static final String UPLOAD_ENDPOINT_URL = "/api/uploads";
    private Map<String, UploadTarget> uploadTargets = new HashMap<>();
    private FilePersistenceUtil filePersistenceUtil;

    @Autowired
    public UploadEndpointManager(FilePersistenceUtil filePersistenceUtil) {
        this.filePersistenceUtil = filePersistenceUtil;
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

    public ResponseEntity<String> performFunction(String targetKey, Resource fileResource) {
        // check permissions
        if (!containsTarget(targetKey)) {
            return new ResponseEntity("No functionality has been created for this endpoint.", HttpStatus.NOT_IMPLEMENTED);
        }

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    public class UploadTarget {

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
