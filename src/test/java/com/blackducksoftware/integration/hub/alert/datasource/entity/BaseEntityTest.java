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
package com.blackducksoftware.integration.hub.alert.datasource.entity;

import org.json.JSONException;

public interface BaseEntityTest<B extends BaseEntity> {
    public void testEmptyEntity() throws JSONException;
    public void testEntity() throws JSONException;
    public Class<B> getEntityClass();
    public void assertEntityFieldsNull(final B entity);
    public long entitySerialId();
    public int emptyEntityHashCode();
    public void assertEntityFieldsFull(final B entity);
    public int entityHashCode();

}
