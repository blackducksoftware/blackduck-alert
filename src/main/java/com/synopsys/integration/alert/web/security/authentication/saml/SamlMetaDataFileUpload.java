/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.security.authentication.saml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.UploadEndpointManager;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;

@Component
public class SamlMetaDataFileUpload {
    public final static String SAML_METADATA_FILE = "uploads/saml_metadata.xml";

    @Autowired
    public SamlMetaDataFileUpload(UploadEndpointManager uploadEndpointManager, SettingsDescriptorKey descriptorKey) throws AlertException {
        uploadEndpointManager.registerTarget(SettingsDescriptor.KEY_SAML_METADATA_FILE, ConfigContextEnum.GLOBAL, descriptorKey, SAML_METADATA_FILE);
    }
}
