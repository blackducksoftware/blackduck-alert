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

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.database.security.repository.SaltMappingRepository;

@Component
public class EncryptionUtility {
    private final String password;
    private final SaltMappingRepository saltMappingRepository;

    @Autowired
    public EncryptionUtility(final AlertProperties alertProperties, final SaltMappingRepository saltMappingRepository) {
        this.saltMappingRepository = saltMappingRepository;
        this.password = alertProperties.getAlertEncryptionPassword().orElse(null);
    }

    @Transactional
    public String encrypt(final String propertyKey, final String propertyValue) {
        Objects.requireNonNull(password);
        Objects.requireNonNull(propertyKey);
        Objects.requireNonNull(propertyValue);
        final String salt = generateSalt();
        final SaltMappingEntity saltMappingEntity = new SaltMappingEntity(propertyKey, salt);
        saltMappingRepository.save(saltMappingEntity);
        final TextEncryptor encryptor = Encryptors.delux(password, salt);
        return encryptor.encrypt(propertyValue);
    }

    @Transactional
    public Optional<String> decrypt(final String propertyKey, final String encryptedValue) {
        Objects.requireNonNull(password);
        Objects.requireNonNull(propertyKey);
        Objects.requireNonNull(encryptedValue);
        final Optional<String> salt = getSalt(propertyKey);
        final Optional<String> decryptedValue;
        if (salt.isPresent()) {
            final TextEncryptor encryptor = Encryptors.delux(password, salt.get());
            decryptedValue = Optional.of(encryptor.decrypt(encryptedValue));
        } else {
            decryptedValue = Optional.empty();
        }
        return decryptedValue;
    }

    private String generateSalt() {
        // creates an 8-byte key, if we want something more secure, then we need to mimic functionality of the HexEncodingStringKeyGenerator in Spring which is package protected
        // it also uses the Hex class which is meant for internal use in spring but Hex is a public class.
        final StringKeyGenerator keyGenerator = KeyGenerators.string();
        return keyGenerator.generateKey();
    }

    private Optional<String> getSalt(final String propertyKey) {
        final Optional<SaltMappingEntity> securityMapping = saltMappingRepository.findById(propertyKey);
        final Optional<String> salt;
        if (securityMapping.isPresent()) {
            salt = Optional.of(securityMapping.get().getSalt());
        } else {
            salt = Optional.empty();
        }
        return salt;
    }
}
