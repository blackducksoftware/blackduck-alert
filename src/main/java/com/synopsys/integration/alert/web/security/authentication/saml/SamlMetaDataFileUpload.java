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
package com.synopsys.integration.alert.web.security.authentication.saml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.synopsys.integration.alert.common.action.upload.AbstractUploadAction;
import com.synopsys.integration.alert.common.action.upload.UploadTarget;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;

@Component
public class SamlMetaDataFileUpload extends AbstractUploadAction {
    private final Logger logger = LoggerFactory.getLogger(SamlMetaDataFileUpload.class);

    @Autowired
    public SamlMetaDataFileUpload(AuthenticationDescriptorKey descriptorKey, AuthorizationManager authorizationManager, FilePersistenceUtil filePersistenceUtil) {
        super(authorizationManager, filePersistenceUtil);
        setTarget(new UploadTarget(AuthenticationDescriptor.KEY_SAML_METADATA_FILE, ConfigContextEnum.GLOBAL, descriptorKey, AuthenticationDescriptor.SAML_METADATA_FILE, this::validateXMLFile));
    }

    private ValidationResult validateXMLFile(File file) {
        try (InputStream fileInputStream = new FileInputStream(file)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            XMLErrorHandler errorHandler = new XMLErrorHandler();
            builder.setErrorHandler(errorHandler);
            builder.parse(new InputSource(fileInputStream));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return ValidationResult.errors(String.format("XML file error: %s", ex.getMessage()));
        }
        return ValidationResult.success();
    }

    private class XMLErrorHandler implements ErrorHandler {

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            logger.warn("File upload exception warning:", exception);
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            logger.error("File upload exception error:", exception);
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            logger.error("File upload exception fatal error:", exception);
        }
    }
}
