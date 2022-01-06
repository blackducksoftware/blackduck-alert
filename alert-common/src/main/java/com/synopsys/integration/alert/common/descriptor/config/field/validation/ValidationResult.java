/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class ValidationResult {

    private final Collection<String> errors;
    private final Collection<String> warnings;

    public static ValidationResult success() {
        return new ValidationResult();
    }

    public static ValidationResult errors(Collection<String> errors) {
        return new ValidationResult(errors, List.of());
    }

    public static ValidationResult errors(String... errors) {
        return new ValidationResult(Arrays.asList(errors), List.of());
    }

    public static ValidationResult warnings(Collection<String> warnings) {
        return new ValidationResult(List.of(), warnings);
    }

    public static ValidationResult warnings(String... warnings) {
        return new ValidationResult(List.of(), Arrays.asList(warnings));
    }

    public static ValidationResult of(ValidationResult... validationResults) {
        Collection<String> validationErrors = getStatus(ValidationResult::getErrors, validationResults);
        Collection<String> validationWarnings = getStatus(ValidationResult::getWarnings, validationResults);

        return new ValidationResult(validationErrors, validationWarnings);
    }

    private ValidationResult() {
        this.errors = List.of();
        this.warnings = List.of();
    }

    private ValidationResult(Collection<String> errors, Collection<String> warnings) {
        this.errors = errors;
        this.warnings = warnings;
    }

    public Collection<String> getErrors() {
        return errors;
    }

    public Collection<String> getWarnings() {
        return warnings;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public String combineErrorMessages() {
        return combineMessages(errors);
    }

    public String combineWarningMessages() {
        return combineMessages(warnings);
    }

    private String combineMessages(Collection<String> messages) {
        return StringUtils.join(messages, ", ");
    }

    private static Collection<String> getStatus(Function<ValidationResult, Collection<String>> extractValidationResults, ValidationResult... validationResults) {
        return Arrays.stream(validationResults)
                   .map(extractValidationResults::apply)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("Validation Errors: %s, Warnings: %s", combineErrorMessages(), combineWarningMessages());
    }
}
