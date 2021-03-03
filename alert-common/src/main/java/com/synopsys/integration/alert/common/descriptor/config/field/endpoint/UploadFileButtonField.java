/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import java.util.List;

import com.synopsys.integration.alert.common.action.upload.AbstractUploadAction;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class UploadFileButtonField extends EndpointField {
    private final List<String> accept;
    private final String capture;
    private final boolean multiple;

    public UploadFileButtonField(String key, String label, String description, String buttonLabel, List<String> accept, String capture, boolean multiple) {
        super(key, label, description, FieldType.UPLOAD_FILE_BUTTON, buttonLabel, AbstractUploadAction.API_FUNCTION_UPLOAD_URL);
        this.accept = accept;
        this.capture = capture;
        this.multiple = multiple;
    }

    public List<String> getAccept() {
        return accept;
    }

    public String getCapture() {
        return capture;
    }

    public Boolean getMultiple() {
        return multiple;
    }

}
