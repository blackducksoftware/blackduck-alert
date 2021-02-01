/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.api.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.web.api.metadata.model.DescriptorsResponseModel;

@Component
public class DescriptorMetadataActions {
    private final Collection<Descriptor> descriptors;
    private final AuthorizationManager authorizationManager;

    @Autowired
    public DescriptorMetadataActions(Collection<Descriptor> descriptors, AuthorizationManager authorizationManager) {
        this.descriptors = descriptors;
        this.authorizationManager = authorizationManager;
    }

    public ActionResponse<DescriptorsResponseModel> getDescriptorsByType(String type) {
        return getDescriptors(null, type, null, this::generateUIComponents);
    }

    public ActionResponse<DescriptorsResponseModel> getDescriptorsByPermissions(@Nullable String name, @Nullable String type, @Nullable String context) {
        return getDescriptors(name, type, context, this::generateUIComponentsByPermissions);
    }

    private ActionResponse<DescriptorsResponseModel> getDescriptors(@Nullable String name, @Nullable String type, @Nullable String context,
        BiFunction<Set<Descriptor>, ConfigContextEnum, Set<DescriptorMetadata>> generatorFunction) {
        Predicate<Descriptor> filter = Descriptor::hasUIConfigs;
        if (name != null) {
            filter = filter.and(descriptor -> name.equalsIgnoreCase(descriptor.getDescriptorKey().getUniversalKey()));
        }

        DescriptorType typeEnum = EnumUtils.getEnumIgnoreCase(DescriptorType.class, type);
        if (typeEnum != null) {
            filter = filter.and(descriptor -> typeEnum.equals(descriptor.getType()));
        } else if (type != null) {
            return new ActionResponse<>(HttpStatus.OK, new DescriptorsResponseModel());
        }

        ConfigContextEnum contextEnum = EnumUtils.getEnumIgnoreCase(ConfigContextEnum.class, context);
        if (contextEnum != null) {
            filter = filter.and(descriptor -> descriptor.hasUIConfigForType(contextEnum));
        } else if (context != null) {
            return new ActionResponse<>(HttpStatus.OK, new DescriptorsResponseModel());
        }

        Set<Descriptor> filteredDescriptors = filter(descriptors, filter);
        DescriptorsResponseModel content = new DescriptorsResponseModel(generatorFunction.apply(filteredDescriptors, contextEnum));
        return new ActionResponse<>(HttpStatus.OK, content);
    }

    private Set<Descriptor> filter(Collection<Descriptor> descriptors, Predicate<Descriptor> predicate) {
        return descriptors
                   .stream()
                   .filter(predicate)
                   .collect(Collectors.toSet());
    }

    private Set<DescriptorMetadata> generateUIComponents(Set<Descriptor> filteredDescriptors, ConfigContextEnum context) {
        ConfigContextEnum[] applicableContexts = (null != context) ? new ConfigContextEnum[] { context } : ConfigContextEnum.values();
        Set<DescriptorMetadata> descriptorMetadata = new HashSet<>();
        for (ConfigContextEnum applicableContext : applicableContexts) {
            for (Descriptor descriptor : filteredDescriptors) {
                descriptor.createMetaData(applicableContext)
                    .ifPresent(descriptorMetadata::add);
            }
        }
        return descriptorMetadata;
    }

    private Set<DescriptorMetadata> generateUIComponentsByPermissions(Set<Descriptor> filteredDescriptors, ConfigContextEnum context) {
        ConfigContextEnum[] applicableContexts = (null != context) ? new ConfigContextEnum[] { context } : ConfigContextEnum.values();
        Set<DescriptorMetadata> descriptorMetadata = new HashSet<>();
        for (ConfigContextEnum applicableContext : applicableContexts) {
            for (Descriptor descriptor : filteredDescriptors) {
                DescriptorKey descriptorKey = descriptor.getDescriptorKey();
                if (authorizationManager.hasPermissions(applicableContext, descriptorKey)) {
                    Optional<DescriptorMetadata> optionalMetaData = descriptor.createMetaData(applicableContext);
                    optionalMetaData
                        .flatMap((metadata) -> restrictMetaData(metadata, applicableContext, descriptorKey))
                        .ifPresent(descriptorMetadata::add);
                }
            }
        }
        return descriptorMetadata;
    }

    private Optional<DescriptorMetadata> restrictMetaData(DescriptorMetadata descriptorMetadata, ConfigContextEnum context, DescriptorKey descriptorKey) {
        boolean hasReadPermission = authorizationManager.hasReadPermission(context, descriptorKey);
        if (!hasReadPermission) {
            return Optional.empty();
        }

        Set<AccessOperation> operationSet = new HashSet<>();
        for (int operations : authorizationManager.getOperations(context, descriptorKey)) {
            operationSet.addAll(AccessOperation.getAllAccessOperations(operations));
        }

        boolean isReadOnly = authorizationManager.isReadOnly(context, descriptorKey);
        descriptorMetadata.setOperations(operationSet);
        descriptorMetadata.setReadOnly(isReadOnly);

        if (isReadOnly) {
            descriptorMetadata.getFields().forEach(field -> field.applyReadOnly(true));
        }
        return Optional.of(descriptorMetadata);
    }

}
