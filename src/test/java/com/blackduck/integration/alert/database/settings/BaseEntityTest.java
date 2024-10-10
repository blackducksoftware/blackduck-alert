/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.settings;

import org.json.JSONException;

public interface BaseEntityTest<B> {
    void testEmptyEntity() throws JSONException;

    void testEntity() throws JSONException;

    Class<B> getEntityClass();

    void assertEntityFieldsNull(B entity);

    void assertEntityFieldsFull(B entity);

}
