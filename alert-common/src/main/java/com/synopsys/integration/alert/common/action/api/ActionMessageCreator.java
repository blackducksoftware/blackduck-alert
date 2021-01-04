/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.action.api;

public class ActionMessageCreator {
    public static final String CREATE_START_MESSAGE = "Creating %s: %s";
    public static final String CREATE_SUCCESS_MESSAGE = "%s %s created successfully.";
    public static final String CREATE_ERROR_MESSAGE = "An error occurred while creating %s: %s";

    public static final String UPDATE_START_MESSAGE_ = "Updating %s: %s";
    public static final String UPDATE_SUCCESS_MESSAGE = "%s %s updated successfully.";
    public static final String UPDATE_ERROR_MESSAGE = "An error occurred while updating %s: %s";
    public static final String UPDATE_NOT_FOUND_MESSAGE = "%s with id %s not found";

    public static final String DELETE_START_MESSAGE = "Deleting %s: %s";
    public static final String DELETE_SUCCESS_MESSAGE = "%s %s deleted successfully.";
    public static final String DELETE_ERROR_MESSAGE = "An error occurred while deleting %s: %s";
    public static final String DELETE_NOT_FOUND_MESSAGE = "%s with id %s not found";

    public String createStartMessage(String objectType, String objectName) {
        return String.format(CREATE_START_MESSAGE, objectType, objectName);
    }

    public String createSuccessMessage(String objectType, String objectName) {
        return String.format(CREATE_SUCCESS_MESSAGE, objectType, objectName);
    }

    public String createErrorMessage(String objectType, String objectName) {
        return String.format(CREATE_ERROR_MESSAGE, objectType, objectName);
    }

    public String updateStartMessage(String objectType, String objectName) {
        return String.format(UPDATE_START_MESSAGE_, objectType, objectName);
    }

    public String updateSuccessMessage(String objectType, String objectName) {
        return String.format(UPDATE_SUCCESS_MESSAGE, objectType, objectName);
    }

    public String updateErrorMessage(String objectType, String objectName) {
        return String.format(UPDATE_ERROR_MESSAGE, objectType, objectName);
    }

    public String updateNotFoundMessage(String objectType, Long id) {
        return String.format(UPDATE_NOT_FOUND_MESSAGE, objectType, id);
    }

    public String deleteStartMessage(String objectType, String objectName) {
        return String.format(DELETE_START_MESSAGE, objectType, objectName);
    }

    public String deleteSuccessMessage(String objectType, String objectName) {
        return String.format(DELETE_SUCCESS_MESSAGE, objectType, objectName);
    }

    public String deleteErrorMessage(String objectType, String objectName) {
        return String.format(DELETE_ERROR_MESSAGE, objectType, objectName);
    }

    public String deleteNotFoundMessage(String objectType, Long id) {
        return String.format(DELETE_NOT_FOUND_MESSAGE, objectType, id);
    }
}
