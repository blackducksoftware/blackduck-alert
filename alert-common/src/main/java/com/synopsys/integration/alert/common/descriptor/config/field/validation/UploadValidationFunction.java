/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.validation;

import java.io.File;
import java.util.function.Function;

@FunctionalInterface
public interface UploadValidationFunction extends Function<File, ValidationResult> {
}
