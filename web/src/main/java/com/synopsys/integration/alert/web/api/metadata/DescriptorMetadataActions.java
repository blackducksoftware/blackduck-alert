/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
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
        Predicate<Descriptor> descriptorFilter = descriptor -> descriptor.getType().name().equals(type);
        return createDescriptorResponse(descriptorFilter, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION));
    }

    public ActionResponse<DescriptorsResponseModel> getDescriptorsByPermissions(@Nullable String name, @Nullable String type, @Nullable String context) {
        Predicate<Descriptor> descriptorFilter = ignored -> true;
        Set<ConfigContextEnum> requestedContexts = Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION);

        if (StringUtils.isNotBlank(name)) {
            descriptorFilter = descriptorFilter.and(descriptor -> name.equals(descriptor.getDescriptorKey().getUniversalKey()));
        }

        if (StringUtils.isNotBlank(type)) {
            descriptorFilter = descriptorFilter.and(descriptor -> type.equals(descriptor.getType().name()));
        }

        if (StringUtils.isNotBlank(context)) {
            ConfigContextEnum requestedContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
            if (null != requestedContext) {
                requestedContexts = Set.of(requestedContext);
            } else {
                requestedContexts = Set.of();
            }
        }

        return createDescriptorResponse(descriptorFilter, requestedContexts);
    }

    private ActionResponse<DescriptorsResponseModel> createDescriptorResponse(Predicate<Descriptor> descriptorFilter, Set<ConfigContextEnum> requestedContexts) {
        Set<DescriptorMetadata> descriptorMetadata = descriptors
            .stream()
            .filter(descriptorFilter)
            .map(descriptor -> createDescriptorMetadata(descriptor, requestedContexts))
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
        DescriptorsResponseModel responseModel = new DescriptorsResponseModel(descriptorMetadata);
        return new ActionResponse<>(HttpStatus.OK, responseModel);
    }

    private Set<DescriptorMetadata> createDescriptorMetadata(Descriptor requestedDescriptor, Set<ConfigContextEnum> requestedContexts) {
        // Permissions can exist for contexts that do not have configuration (e.g. empty Global Channel configs)
        return requestedContexts.stream()
            .filter(requestedContexts::contains)
            .map(configContextEnum -> createDescriptorMetadata(requestedDescriptor.getDescriptorKey(), configContextEnum, requestedDescriptor.getType()))
            .flatMap(Optional::stream)
            .collect(Collectors.toSet());
    }

    private Optional<DescriptorMetadata> createDescriptorMetadata(DescriptorKey descriptorKey, ConfigContextEnum context, DescriptorType descriptorType) {
        if (authorizationManager.hasPermissions(context, descriptorKey)) {
            Set<AccessOperation> operations = gatherDescriptorContextOperations(context, descriptorKey);
            boolean readOnly = authorizationManager.isReadOnly(context, descriptorKey);
            DescriptorMetadata descriptorMetadata = new DescriptorMetadata(descriptorKey, descriptorType, context, operations, readOnly);
            return Optional.of(descriptorMetadata);
        }
        return Optional.empty();
    }

    private Set<AccessOperation> gatherDescriptorContextOperations(ConfigContextEnum context, DescriptorKey descriptorKey) {
        Set<AccessOperation> operations = new HashSet<>();
        for (int operationBits : authorizationManager.getOperations(context, descriptorKey)) {
            operations.addAll(AccessOperation.getAllAccessOperations(operationBits));
        }
        return operations;
    }

}
