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
import com.synopsys.integration.alert.common.descriptor.config.filter.FieldsFilter;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.web.model.RestrictedDescriptorMetadata;

@RestController
public class DescriptorController extends MetadataController {
    public static final String DESCRIPTORS_PATH = "/descriptors";

    private final Collection<Descriptor> descriptors;
    private final Collection<FieldsFilter> fieldsFilters;
    private final AuthorizationManager authorizationManager;

    @Autowired
    public DescriptorController(final Collection<Descriptor> descriptors, final Collection<FieldsFilter> fieldsFilters, final AuthorizationManager authorizationManager) {
        this.descriptors = descriptors;
        this.fieldsFilters = fieldsFilters;
        this.authorizationManager = authorizationManager;
    }

    @GetMapping(DESCRIPTORS_PATH)
    public Set<RestrictedDescriptorMetadata> getDescriptors(@RequestParam(required = false) final String name, @RequestParam(required = false) final String type, @RequestParam(required = false) final String context) {
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

    private Set<RestrictedDescriptorMetadata> generateUIComponents(final Set<Descriptor> filteredDescriptors, final ConfigContextEnum context) {
        final ConfigContextEnum[] applicableContexts;
        if (context != null) {
            applicableContexts = new ConfigContextEnum[] { context };
        } else {
            applicableContexts = ConfigContextEnum.values();
        }

        final Set<RestrictedDescriptorMetadata> descriptorMetadata = new HashSet<>();
        for (final ConfigContextEnum applicableContext : applicableContexts) {
            for (final Descriptor descriptor : filteredDescriptors) {
                final Optional<DescriptorMetadata> optionalMetaData = descriptor.getMetaData(applicableContext);
                optionalMetaData.flatMap(this::filterFieldsByPermissions).ifPresent(descriptorMetadata::add);
            }
        }
        return descriptorMetadata;
    }

    private Optional<RestrictedDescriptorMetadata> filterFieldsByPermissions(final DescriptorMetadata descriptorMetadata) {
        final String descriptorName = descriptorMetadata.getName();
        final ConfigContextEnum context = descriptorMetadata.getContext();
        final Optional<FieldsFilter> matchingFieldsFilter = fieldsFilters
                                                                .stream()
                                                                .filter(fieldsFilter -> fieldsFilter.getContext() == context && fieldsFilter.getDescriptorName().equals(descriptorName))
                                                                .findFirst();
        if (matchingFieldsFilter.isPresent()) {
            final List<ConfigField> fields = descriptorMetadata.getFields();
            final List<ConfigField> filteredFields = matchingFieldsFilter.get().filter(fields);
            descriptorMetadata.setFields(filteredFields);
        }

        final String permissionKey = AuthorizationManager.generateConfigPermissionKey(context.name(), descriptorName);
        return restrictMetaData(descriptorMetadata, permissionKey);
    }

    private Optional<RestrictedDescriptorMetadata> restrictMetaData(final DescriptorMetadata descriptorMetadata, final String permissionKey) {
        final boolean hasReadPermission = authorizationManager.hasReadPermission(permissionKey);
        if (!hasReadPermission) {
            return Optional.empty();
        }

        final boolean hasExecutePermission = authorizationManager.hasExecutePermission(permissionKey);
        final boolean hasCreatePermission = authorizationManager.hasCreatePermission(permissionKey);
        final boolean hasWritePermission = authorizationManager.hasWritePermission(permissionKey);
        final boolean hasDeletePermission = authorizationManager.hasDeletePermission(permissionKey);
        final boolean isReadOnly = authorizationManager.isReadOnly(permissionKey);

        final RestrictedDescriptorMetadata restrictedDescriptorMetadata = new RestrictedDescriptorMetadata(descriptorMetadata, hasCreatePermission && hasWritePermission, hasExecutePermission, hasDeletePermission, isReadOnly);
        return Optional.of(restrictedDescriptorMetadata);
    }

}
