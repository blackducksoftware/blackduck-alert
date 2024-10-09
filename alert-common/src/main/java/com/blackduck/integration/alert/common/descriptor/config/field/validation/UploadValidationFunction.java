package com.blackduck.integration.alert.common.descriptor.config.field.validation;

import java.io.File;
import java.util.function.Function;

@FunctionalInterface
public interface UploadValidationFunction extends Function<File, ValidationResult> {
}
