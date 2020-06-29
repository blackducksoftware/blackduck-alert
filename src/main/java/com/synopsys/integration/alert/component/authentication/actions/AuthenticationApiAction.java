/**
 * blackduck-alert
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
package com.synopsys.integration.alert.component.authentication.actions;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLManager;

@Component
public class AuthenticationApiAction extends ApiAction {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationApiAction.class);
    private final SAMLManager samlManager;

    @Autowired
    public AuthenticationApiAction(SAMLManager samlManager) {
        this.samlManager = samlManager;
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) {
        return handleNewAndUpdatedConfig(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) {
        return handleNewAndUpdatedConfig(currentFieldModel);
    }

    private FieldModel handleNewAndUpdatedConfig(FieldModel fieldModel) {
        addSAMLMetadata(fieldModel);
        return fieldModel;
    }

    private void addSAMLMetadata(FieldModel fieldModel) {
        try {
            boolean samlEnabled = fieldModel.getFieldValueModel(AuthenticationDescriptor.KEY_SAML_ENABLED)
                                      .flatMap(FieldValueModel::getValue)
                                      .map(BooleanUtils::toBoolean)
                                      .orElse(false);
            Optional<FieldValueModel> metadataURLFieldValueOptional = fieldModel.getFieldValueModel(AuthenticationDescriptor.KEY_SAML_METADATA_URL);
            Optional<FieldValueModel> metadataEntityFieldValueOptional = fieldModel.getFieldValueModel(AuthenticationDescriptor.KEY_SAML_ENTITY_ID);
            Optional<FieldValueModel> metadataBaseURLFieldValueOptional = fieldModel.getFieldValueModel(AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL);
            if (metadataEntityFieldValueOptional.isPresent() && metadataBaseURLFieldValueOptional.isPresent()) {
                FieldValueModel metadataEntityFieldValue = metadataEntityFieldValueOptional.get();
                FieldValueModel metadataBaseUrValueModel = metadataBaseURLFieldValueOptional.get();
                String metadataURL = metadataURLFieldValueOptional.flatMap(FieldValueModel::getValue).orElse("");
                String entityId = metadataEntityFieldValue.getValue().orElse("");
                String baseUrl = metadataBaseUrValueModel.getValue().orElse("");
                samlManager.updateSAMLConfiguration(samlEnabled, metadataURL, entityId, baseUrl);
            }
        } catch (Exception ex) {
            logger.error("Error adding SAML settings", ex);
        }
    }
}
