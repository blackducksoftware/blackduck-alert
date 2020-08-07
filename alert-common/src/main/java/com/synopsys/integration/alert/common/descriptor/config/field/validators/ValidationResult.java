/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.validators;

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

    private static Collection<String> getStatus(Function<ValidationResult, Collection<String>> mapResults, ValidationResult... validationResults) {
        return Arrays.stream(validationResults)
                   .map(mapResults::apply)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
    }
}