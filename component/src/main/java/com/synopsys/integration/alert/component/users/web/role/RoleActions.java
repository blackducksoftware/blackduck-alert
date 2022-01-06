/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users.web.role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.action.api.AbstractResourceActions;
import com.synopsys.integration.alert.common.action.api.ActionMessageCreator;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.BitwiseUtil;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.component.users.web.role.util.PermissionModelUtil;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class RoleActions extends AbstractResourceActions<RolePermissionModel, UserRoleModel, MultiRolePermissionModel> {
    private static final String FIELD_KEY_ROLE_NAME = "roleName";
    private final RoleAccessor roleAccessor;
    private final AuthorizationManager authorizationManager;
    private final DescriptorMap descriptorMap;

    private final Logger logger = AlertLoggerFactory.getLogger(RoleActions.class);
    private final ActionMessageCreator actionMessageCreator = new ActionMessageCreator();

    @Autowired
    public RoleActions(UserManagementDescriptorKey userManagementDescriptorKey, RoleAccessor roleAccessor, AuthorizationManager authorizationManager, DescriptorMap descriptorMap) {
        super(userManagementDescriptorKey, ConfigContextEnum.GLOBAL, authorizationManager);
        this.roleAccessor = roleAccessor;
        this.authorizationManager = authorizationManager;
        this.descriptorMap = descriptorMap;
    }

    @Override
    protected ActionResponse<RolePermissionModel> createWithoutChecks(RolePermissionModel resource) {
        String roleName = resource.getRoleName();
        Set<PermissionModel> permissions = resource.getPermissions();
        PermissionMatrixModel permissionMatrixModel = PermissionModelUtil.convertToPermissionMatrixModel(permissions);
        logger.debug(actionMessageCreator.createStartMessage("role", roleName));
        UserRoleModel userRoleModel = authorizationManager.createRoleWithPermissions(roleName, permissionMatrixModel);
        logger.debug(actionMessageCreator.createSuccessMessage("Role", roleName));
        return new ActionResponse<>(HttpStatus.OK, convertDatabaseModelToRestModel(userRoleModel));
    }

    @Override
    protected ActionResponse<RolePermissionModel> deleteWithoutChecks(Long id) {
        Optional<UserRoleModel> existingRole = roleAccessor.getRoles(List.of(id))
                                                   .stream()
                                                   .findFirst();
        if (existingRole.isPresent()) {
            String roleName = existingRole.get().getName();
            try {
                logger.debug(actionMessageCreator.deleteStartMessage("role", roleName));
                authorizationManager.deleteRole(id);
            } catch (AlertException ex) {
                logger.error(actionMessageCreator.deleteErrorMessage("role", existingRole.get().getName()));
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error deleting role: %s", ex.getMessage()));
            }
            logger.debug(actionMessageCreator.deleteSuccessMessage("Role", roleName));
            return new ActionResponse<>(HttpStatus.NO_CONTENT);
        }
        logger.warn(actionMessageCreator.deleteNotFoundMessage("Role", id));
        return new ActionResponse<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected List<UserRoleModel> retrieveDatabaseModels() {
        return new ArrayList<>(roleAccessor.getRoles());
    }

    @Override
    protected MultiRolePermissionModel createMultiResponseModel(List<RolePermissionModel> roles) {
        return new MultiRolePermissionModel(roles);
    }

    @Override
    protected RolePermissionModel convertDatabaseModelToRestModel(UserRoleModel userRoleModel) {
        String roleName = userRoleModel.getName();
        PermissionMatrixModel permissionModel = userRoleModel.getPermissions();
        Set<PermissionModel> permissionKeyToAccess = convertPermissionMatrixModel(permissionModel);
        return new RolePermissionModel(String.valueOf(userRoleModel.getId()), roleName, permissionKeyToAccess);
    }

    @Override
    protected ValidationActionResponse testWithoutChecks(RolePermissionModel resource) {
        return validateWithoutChecks(resource);
    }

    @Override
    protected ActionResponse<RolePermissionModel> updateWithoutChecks(Long id, RolePermissionModel resource) {
        try {
            String roleName = resource.getRoleName();
            Optional<UserRoleModel> existingRole = roleAccessor.getRoles(List.of(id))
                                                       .stream()
                                                       .findFirst();
            if (existingRole.isPresent()) {
                logger.debug(actionMessageCreator.updateStartMessage("role", existingRole.get().getName()));
                if (!existingRole.get().getName().equals(roleName)) {
                    authorizationManager.updateRoleName(id, roleName);
                }
                Set<PermissionModel> permissions = resource.getPermissions();
                PermissionMatrixModel permissionMatrixModel = PermissionModelUtil.convertToPermissionMatrixModel(permissions);
                authorizationManager.updatePermissionsForRole(roleName, permissionMatrixModel);
                logger.debug(actionMessageCreator.updateSuccessMessage("Role", roleName));
                return new ActionResponse<>(HttpStatus.NO_CONTENT);
            }
            logger.warn(actionMessageCreator.updateNotFoundMessage("Role", id));
            return new ActionResponse<>(HttpStatus.NOT_FOUND, "Role not found.");
        } catch (AlertException ex) {
            logger.error(actionMessageCreator.updateErrorMessage("role", resource.getRoleName()));
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected ValidationActionResponse validateWithoutChecks(RolePermissionModel resource) {
        ValidationResponseModel responseModel;
        List<AlertFieldStatus> alertFieldStatus = fullyValidateRoleNameField(resource);
        if (alertFieldStatus.isEmpty()) {
            responseModel = ValidationResponseModel.success("The role name is valid");
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        }
        responseModel = ValidationResponseModel.fromStatusCollection("There were problems with the role configuration", alertFieldStatus);
        return new ValidationActionResponse(HttpStatus.BAD_REQUEST, responseModel);
    }

    @Override
    protected Optional<RolePermissionModel> findExisting(Long id) {
        return roleAccessor.getRoles(List.of(id))
                   .stream()
                   .findFirst()
                   .map(this::convertDatabaseModelToRestModel);
    }

    private Set<PermissionModel> convertPermissionMatrixModel(PermissionMatrixModel permissionMatrixModel) {
        Set<PermissionModel> permissionMatrix = new HashSet<>();
        for (Map.Entry<PermissionKey, Integer> matrixRow : permissionMatrixModel.getPermissions().entrySet()) {
            Integer accessOperations = matrixRow.getValue();
            PermissionKey permissionKey = matrixRow.getKey();
            String descriptorDisplayName = descriptorMap.getDescriptorKey(permissionKey.getDescriptorName()).map(DescriptorKey::getDisplayName).orElse(permissionKey.getDescriptorName());

            PermissionModel permissionModel = new PermissionModel(
                descriptorDisplayName,
                permissionKey.getContext(),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.CREATE.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.READ.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.WRITE.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.DELETE.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.EXECUTE.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.UPLOAD_FILE_READ.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.UPLOAD_FILE_WRITE.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.UPLOAD_FILE_DELETE.getBit())
            );
            permissionMatrix.add(permissionModel);
        }
        return permissionMatrix;
    }

    private List<AlertFieldStatus> fullyValidateRoleNameField(RolePermissionModel rolePermissionModel) {
        List<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        String roleName = rolePermissionModel.getRoleName();

        validateRoleNameFieldRequired(roleName).ifPresent(fieldStatuses::add);

        Set<PermissionModel> permissions = rolePermissionModel.getPermissions();
        validatePermissions(permissions).ifPresent(fieldStatuses::add);

        if (rolePermissionModel.getId() == null) {
            boolean exists = roleAccessor.doesRoleNameExist(roleName);
            if (exists) {
                fieldStatuses.add(AlertFieldStatus.error(FIELD_KEY_ROLE_NAME, "A role with that name already exists."));
            }
        }
        return fieldStatuses;
    }

    private Optional<AlertFieldStatus> validateRoleNameFieldRequired(String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            return Optional.of(AlertFieldStatus.error(RoleActions.FIELD_KEY_ROLE_NAME, "This field is required."));
        }
        return Optional.empty();
    }

    private Optional<AlertFieldStatus> validatePermissions(Set<PermissionModel> permissionModels) {
        Set<PermissionKey> descriptorContexts = new HashSet<>();
        for (PermissionModel permissionModel : permissionModels) {
            PermissionKey pair = new PermissionKey(permissionModel.getContext(), permissionModel.getDescriptorName());
            if (descriptorContexts.contains(pair)) {
                return Optional.of(AlertFieldStatus.error(RoleActions.FIELD_KEY_ROLE_NAME,
                    String.format("Can't save duplicate permissions for a role. Duplicate permission for '%s' with context '%s' found.", pair.getDescriptorName(), pair.getContext())));
            } else {
                descriptorContexts.add(pair);
            }
        }
        return Optional.empty();
    }

}
