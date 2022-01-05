/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users.web.role.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.util.BitwiseUtil;
import com.synopsys.integration.alert.component.users.web.role.PermissionModel;

public final class PermissionModelUtil {
    public static PermissionMatrixModel convertToPermissionMatrixModel(Set<PermissionModel> permissionModels) {
        Map<PermissionKey, Integer> permissionMatrix = new HashMap<>();
        for (PermissionModel permissionModel : permissionModels) {
            String descriptorKey = permissionModel.getDescriptorName();
            String context = permissionModel.getContext();
            PermissionKey permissionKey = new PermissionKey(context, descriptorKey);

            int accessOperationsBits = 0;
            if (permissionModel.isCreate()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.CREATE.getBit());
            }
            if (permissionModel.isRead()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.READ.getBit());
            }
            if (permissionModel.isDelete()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.DELETE.getBit());
            }
            if (permissionModel.isExecute()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.EXECUTE.getBit());
            }
            if (permissionModel.isWrite()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.WRITE.getBit());
            }
            if (permissionModel.isUploadDelete()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.UPLOAD_FILE_DELETE.getBit());
            }
            if (permissionModel.isUploadRead()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.UPLOAD_FILE_READ.getBit());
            }
            if (permissionModel.isUploadWrite()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.UPLOAD_FILE_WRITE.getBit());
            }

            permissionMatrix.put(permissionKey, accessOperationsBits);
        }
        return new PermissionMatrixModel(permissionMatrix);
    }
}
