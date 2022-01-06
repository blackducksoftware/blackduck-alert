/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users.web.role;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class PermissionModel extends AlertSerializableModel {
    private String descriptorName;
    private String context;
    private boolean create;
    private boolean read;
    private boolean write;
    private boolean delete;
    private boolean execute;
    private boolean uploadRead;
    private boolean uploadWrite;
    private boolean uploadDelete;

    public PermissionModel() {
    }

    public PermissionModel(String descriptorName, String context, boolean create, boolean read, boolean write, boolean delete, boolean execute, boolean uploadRead, boolean uploadWrite,
        boolean uploadDelete) {
        this.descriptorName = descriptorName;
        this.context = context;
        this.create = create;
        this.read = read;
        this.write = write;
        this.delete = delete;
        this.execute = execute;
        this.uploadRead = uploadRead;
        this.uploadWrite = uploadWrite;
        this.uploadDelete = uploadDelete;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public String getContext() {
        return context;
    }

    public boolean isCreate() {
        return create;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isWrite() {
        return write;
    }

    public boolean isDelete() {
        return delete;
    }

    public boolean isExecute() {
        return execute;
    }

    public boolean isUploadRead() {
        return uploadRead;
    }

    public boolean isUploadWrite() {
        return uploadWrite;
    }

    public boolean isUploadDelete() {
        return uploadDelete;
    }

    public void setDescriptorName(String descriptorName) {
        this.descriptorName = descriptorName;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public void setExecute(boolean execute) {
        this.execute = execute;
    }

    public void setUploadRead(boolean uploadRead) {
        this.uploadRead = uploadRead;
    }

    public void setUploadWrite(boolean uploadWrite) {
        this.uploadWrite = uploadWrite;
    }

    public void setUploadDelete(boolean uploadDelete) {
        this.uploadDelete = uploadDelete;
    }

}
