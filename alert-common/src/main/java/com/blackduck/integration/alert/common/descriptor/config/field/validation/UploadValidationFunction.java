/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.descriptor.config.field.validation;

import java.io.File;
import java.util.function.Function;

@FunctionalInterface
public interface UploadValidationFunction extends Function<File, ValidationResult> {
}
