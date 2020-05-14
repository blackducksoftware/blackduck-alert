/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.database.settings;

import org.json.JSONException;

public interface BaseEntityTest<B> {
    void testEmptyEntity() throws JSONException;

    void testEntity() throws JSONException;

    Class<B> getEntityClass();

    void assertEntityFieldsNull(B entity);

    void assertEntityFieldsFull(B entity);

}
