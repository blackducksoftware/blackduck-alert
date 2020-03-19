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
package com.synopsys.integration.alert.web.controller.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@RestController
public class DescriptorController extends MetadataController {
    public static final String DESCRIPTORS_PATH = "/descriptors";

    private final Collection<Descriptor> descriptors;
    private final AuthorizationManager authorizationManager;

    @Autowired
    public DescriptorController(final Collection<Descriptor> descriptors, final AuthorizationManager authorizationManager) {
        this.descriptors = descriptors;
        this.authorizationManager = authorizationManager;
    }

    @GetMapping(DESCRIPTORS_PATH)
    public Set<DescriptorMetadata> getDescriptors(@RequestParam(required = false) final String name, @RequestParam(required = false) final String type, @RequestParam(required = false) final String context) {
        Predicate<Descriptor> filter = Descriptor::hasUIConfigs;
        if (name != null) {
            filter = filter.and(descriptor -> name.equalsIgnoreCase(descriptor.getName()));
        }

        final DescriptorType typeEnum = EnumUtils.getEnumIgnoreCase(DescriptorType.class, type);
        if (typeEnum != null) {
            filter = filter.and(descriptor -> typeEnum.equals(descriptor.getType()));
        } else if (type != null) {
            return Set.of();
        }

        final ConfigContextEnum contextEnum = EnumUtils.getEnumIgnoreCase(ConfigContextEnum.class, context);
        if (contextEnum != null) {
            filter = filter.and(descriptor -> descriptor.hasUIConfigForType(contextEnum));
        } else if (context != null) {
            return Set.of();
        }

        final Set<Descriptor> filteredDescriptors = filter(descriptors, filter);
        return generateUIComponents(filteredDescriptors, contextEnum);
    }

    private Set<Descriptor> filter(final Collection<Descriptor> descriptors, final Predicate<Descriptor> predicate) {
        return descriptors
                   .stream()
                   .filter(predicate)
                   .collect(Collectors.toSet());
    }

    private Set<DescriptorMetadata> generateUIComponents(final Set<Descriptor> filteredDescriptors, final ConfigContextEnum context) {
        final ConfigContextEnum[] applicableContexts = (null != context) ? new ConfigContextEnum[] { context } : ConfigContextEnum.values();
        final Set<DescriptorMetadata> descriptorMetadata = new HashSet<>();
        for (final ConfigContextEnum applicableContext : applicableContexts) {
            for (final Descriptor descriptor : filteredDescriptors) {
                final Optional<DescriptorMetadata> optionalMetaData = descriptor.createMetaData(applicableContext);
                optionalMetaData.flatMap(this::filterFieldsByPermissions).ifPresent(descriptorMetadata::add);
            }
        }
        return descriptorMetadata;
    }

    private Optional<DescriptorMetadata> filterFieldsByPermissions(final DescriptorMetadata descriptorMetadata) {
        final String descriptorName = descriptorMetadata.getName();
        final ConfigContextEnum context = descriptorMetadata.getContext();
        final String permissionKey = AuthorizationManager.generatePermissionKey(context.name(), descriptorName);

        List<ConfigField> filteredFields = descriptorMetadata.getFields();
        if (!authorizationManager.hasPermissions(permissionKey)) {
            filteredFields = List.of();
        }

        descriptorMetadata.setFields(filteredFields);
        return restrictMetaData(descriptorMetadata, permissionKey);
    }

    private Optional<DescriptorMetadata> restrictMetaData(final DescriptorMetadata descriptorMetadata, final String permissionKey) {
        final boolean hasReadPermission = authorizationManager.hasReadPermission(permissionKey);
        if (!hasReadPermission) {
            return Optional.empty();
        }

        Set<AccessOperation> operationSet = authorizationManager.getOperations(permissionKey);
        final boolean isReadOnly = authorizationManager.isReadOnly(permissionKey);
        descriptorMetadata.setOperations(operationSet);
        descriptorMetadata.setReadOnly(isReadOnly);

        if (authorizationManager.isReadOnly(permissionKey)) {
            descriptorMetadata.getFields().stream().forEach(field -> field.setReadOnly(isReadOnly));
        }

        return Optional.of(descriptorMetadata);
    }

}
