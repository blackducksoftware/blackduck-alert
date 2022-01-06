/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action.api;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.JobPagedModel;
import com.synopsys.integration.alert.common.rest.model.JobProviderProjectFieldModel;
import com.synopsys.integration.alert.common.rest.model.MultiJobFieldModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.PagingParamValidationUtils;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public abstract class AbstractJobResourceActions {
    private static final EnumSet<DescriptorType> ALLOWED_JOB_DESCRIPTOR_TYPES = EnumSet.of(DescriptorType.PROVIDER, DescriptorType.CHANNEL);
    protected final AuthorizationManager authorizationManager;
    protected final DescriptorAccessor descriptorAccessor;
    protected final DescriptorMap descriptorMap;

    public AbstractJobResourceActions(AuthorizationManager authorizationManager, DescriptorAccessor descriptorAccessor, DescriptorMap descriptorMap) {
        this.authorizationManager = authorizationManager;
        this.descriptorAccessor = descriptorAccessor;
        this.descriptorMap = descriptorMap;
    }

    protected abstract Optional<JobFieldModel> findJobFieldModel(UUID id);

    protected abstract ActionResponse<JobFieldModel> createWithoutChecks(JobFieldModel resource);

    protected abstract ActionResponse<JobFieldModel> deleteWithoutChecks(UUID id);

    protected abstract ActionResponse<JobPagedModel> readPageWithoutChecks(Integer pageNumber, Integer pageSize, String searchTerm, Collection<String> permittedDescriptorsForSession);

    protected abstract ValidationActionResponse testWithoutChecks(JobFieldModel resource);

    protected abstract ActionResponse<JobFieldModel> updateWithoutChecks(UUID id, JobFieldModel resource);

    protected abstract ValidationActionResponse validateWithoutChecks(JobFieldModel resource);

    private Set<String> getDescriptorNames() {
        return descriptorAccessor.getRegisteredDescriptors()
                   .stream()
                   .filter(descriptor -> ALLOWED_JOB_DESCRIPTOR_TYPES.contains(descriptor.getType()))
                   .map(RegisteredDescriptorModel::getName)
                   .collect(Collectors.toSet());
    }

    public final ActionResponse<JobFieldModel> create(JobFieldModel resource) {
        boolean hasPermissions = hasRequiredPermissions(resource.getFieldModels(), authorizationManager::hasCreatePermission);
        if (!hasPermissions) {
            return ActionResponse.createForbiddenResponse();
        }

        // Clean input
        resource.setJobId(null);
        correctProjectsField(resource);

        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return createWithoutChecks(resource);
    }

    public final ActionResponse<JobPagedModel> getPage(Integer pageNumber, Integer pageSize, String searchTerm) {
        Optional<ActionResponse<JobPagedModel>> pagingErrorResponse = PagingParamValidationUtils.createErrorActionResponseIfInvalid(pageNumber, pageSize);
        if (pagingErrorResponse.isPresent()) {
            return pagingErrorResponse.get();
        }

        String relevantContextName = ConfigContextEnum.DISTRIBUTION.name();
        Set<String> descriptorNames = getDescriptorNames();
        Set<String> permittedDescriptorsForSession = new HashSet<>();
        for (String descriptorName : descriptorNames) {
            if (authorizationManager.hasReadPermission(relevantContextName, descriptorName)) {
                permittedDescriptorsForSession.add(descriptorName);
            }
        }

        if (permittedDescriptorsForSession.isEmpty()) {
            return ActionResponse.createForbiddenResponse();
        }
        return readPageWithoutChecks(pageNumber, pageSize, searchTerm, permittedDescriptorsForSession);
    }

    @Deprecated
    public final ActionResponse<MultiJobFieldModel> getAll() {
        return new ActionResponse<>(HttpStatus.GONE);
    }

    public final ActionResponse<JobFieldModel> getOne(UUID id) {
        Set<String> descriptorNames = getDescriptorNames();
        if (!authorizationManager.anyReadPermission(List.of(ConfigContextEnum.DISTRIBUTION), descriptorNames)) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<JobFieldModel> optionalModel = findJobFieldModel(id);
        if (optionalModel.isPresent()) {
            JobFieldModel fieldModel = optionalModel.get();

            boolean hasPermissions = hasRequiredPermissions(fieldModel.getFieldModels(), authorizationManager::hasReadPermission);
            if (!hasPermissions) {
                return ActionResponse.createForbiddenResponse();
            }
            return new ActionResponse<>(HttpStatus.OK, fieldModel);
        }
        return new ActionResponse<>(HttpStatus.NOT_FOUND);
    }

    public final ActionResponse<JobFieldModel> update(UUID id, JobFieldModel resource) {
        boolean hasPermissions = hasRequiredPermissions(resource.getFieldModels(), authorizationManager::hasWritePermission);
        if (!hasPermissions) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<JobFieldModel> existingJob = findJobFieldModel(id);
        if (existingJob.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        // Clean input
        correctProjectsField(resource);

        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return updateWithoutChecks(id, resource);
    }

    public final ActionResponse<JobFieldModel> delete(UUID id) {
        Optional<JobFieldModel> optionalModel = findJobFieldModel(id);

        if (optionalModel.isPresent()) {
            JobFieldModel jobFieldModel = optionalModel.get();
            boolean hasPermissions = hasRequiredPermissions(jobFieldModel.getFieldModels(), authorizationManager::hasDeletePermission);
            if (!hasPermissions) {
                return ActionResponse.createForbiddenResponse();
            }
        } else {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }
        return deleteWithoutChecks(id);
    }

    public final ValidationActionResponse test(JobFieldModel resource) {
        boolean hasPermissions = hasRequiredPermissions(resource.getFieldModels(), authorizationManager::hasExecutePermission);
        if (!hasPermissions) {
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }

        // Clean input
        correctProjectsField(resource);

        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return ValidationActionResponse.createOKResponseWithContent(validationResponse);
        }
        return testWithoutChecks(resource);
    }

    public final ValidationActionResponse validate(JobFieldModel resource) {
        boolean hasPermissions = resource.getFieldModels()
                                     .stream()
                                     .allMatch(model ->
                                                   authorizationManager.hasCreatePermission(model.getContext(), model.getDescriptorName())
                                                       || authorizationManager.hasWritePermission(model.getContext(), model.getDescriptorName())
                                                       || authorizationManager.hasExecutePermission(model.getContext(), model.getDescriptorName()));
        if (!hasPermissions) {
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }

        // Clean input
        correctProjectsField(resource);

        ValidationActionResponse response = validateWithoutChecks(resource);
        return ValidationActionResponse.createOKResponseWithContent(response);
    }

    public AuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    public DescriptorAccessor getDescriptorAccessor() {
        return descriptorAccessor;
    }

    public DescriptorMap getDescriptorMap() {
        return descriptorMap;
    }

    private boolean hasRequiredPermissions(Collection<FieldModel> fieldModels, BiFunction<String, String, Boolean> permissionChecker) {
        return fieldModels
                   .stream()
                   .allMatch(model -> permissionChecker.apply(model.getContext(), model.getDescriptorName()));
        //TODO Once the FieldModel is updated to handle ConfigContextEnum and DescriptorKey, the following code should be used
        /*
        return fieldModels
                   .stream()
                   .allMatch(model -> checkContextAndDescriptorKey(model, permissionChecker));
         */
    }

    private boolean checkContextAndDescriptorKey(FieldModel fieldModel, BiFunction<ConfigContextEnum, DescriptorKey, Boolean> permissionChecker) {
        ConfigContextEnum configContextEnum = ConfigContextEnum.valueOf(fieldModel.getContext());
        DescriptorKey descriptorKey = descriptorMap.getDescriptorKey(fieldModel.getDescriptorName()).orElseThrow(() -> new AlertRuntimeException("Could not find DescriptorKey for: " + fieldModel.getDescriptorName()));
        return permissionChecker.apply(configContextEnum, descriptorKey);
    }

    // FIXME More tech debt until we fix the Jobs API
    private void correctProjectsField(JobFieldModel jobFieldModel) {
        List<JobProviderProjectFieldModel> projects = jobFieldModel.getConfiguredProviderProjects();
        if (null == projects) {
            projects = List.of();
        }

        String projectFieldKey = "channel.common.configured.project";
        for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            Map<String, FieldValueModel> keyToValues = fieldModel.getKeyToValues();
            if (keyToValues.containsKey(projectFieldKey)) {
                FieldValueModel projectFieldValues = createProjectFieldValues(projects);
                keyToValues.put(projectFieldKey, projectFieldValues);
                return;
            }
        }
    }

    private FieldValueModel createProjectFieldValues(List<JobProviderProjectFieldModel> projects) {
        Set<String> projectNames = projects.stream().map(JobProviderProjectFieldModel::getName).collect(Collectors.toSet());
        return new FieldValueModel(projectNames, false);
    }

}
