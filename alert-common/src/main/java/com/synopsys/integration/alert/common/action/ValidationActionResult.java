package com.synopsys.integration.alert.common.action;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

public class ValidationActionResult extends ActionResult<Void> {
    private List<AlertFieldStatus> fieldStatusList;

    public ValidationActionResult(HttpStatus httpStatus, String message, List<AlertFieldStatus> fieldStatusList) {
        super(httpStatus, message);
        this.fieldStatusList = fieldStatusList;
    }

    public List<AlertFieldStatus> getFieldStatusList() {
        return fieldStatusList;
    }
}
