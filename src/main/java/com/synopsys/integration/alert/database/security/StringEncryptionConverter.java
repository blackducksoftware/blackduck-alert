/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.database.security;

import javax.persistence.AttributeConverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.security.EncryptionUtility;

@Component
public class StringEncryptionConverter implements AttributeConverter<String, String> {
    private final Logger logger = LoggerFactory.getLogger(StringEncryptionConverter.class);
    private static EncryptionUtility encryptionUtility;

    public boolean isInitialized() {
        return StringEncryptionConverter.encryptionUtility != null;
    }

    @Autowired
    public void setEncryptionUtility(final EncryptionUtility encryptionUtility) {
        StringEncryptionConverter.encryptionUtility = encryptionUtility;
    }

    @Override
    public String convertToDatabaseColumn(final String attribute) {
        String encryptedAttribute = "";
        if (StringUtils.isNotBlank(attribute)) {
            try {
                encryptedAttribute = StringEncryptionConverter.encryptionUtility.encrypt(attribute);
            } catch (final Exception e) {
                logger.error("Could not encrypt the attribute. " + e.getMessage());
                logger.debug(e.getMessage(), e);
            }
        }
        return encryptedAttribute;
    }

    @Override
    public String convertToEntityAttribute(final String dbData) {
        String decryptedColumm = "";
        if (StringUtils.isNotBlank(dbData)) {
            try {
                decryptedColumm = StringEncryptionConverter.encryptionUtility.decrypt(dbData);
            } catch (final Exception e) {
                logger.error("Could not decrypt the attribute. " + e.getMessage());
                logger.debug(e.getMessage(), e);
            }
        }
        return decryptedColumm;
    }

}
