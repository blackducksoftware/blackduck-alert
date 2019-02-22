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

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.database.DatabaseRelation;

public abstract class RelationTest<R extends DatabaseRelation> implements BaseEntityTest<R> {
    @Override
    @Test
    public void testEmptyEntity() throws JSONException {
        final R configEntity = createMockEmptyRelation();
        final R configEntityNew = createMockEmptyRelation();
        assertEquals(configEntity, configEntityNew);
    }

    @Override
    @Test
    public void testEntity() throws JSONException {
        final Long firstId = 13L;
        final Long secondId = 17L;

        final R configEntity = createMockRelation(firstId, secondId);
        final R configEntityNew = createMockRelation(firstId, secondId);
        assertEquals(configEntity, configEntityNew);
    }

    public abstract R createMockRelation(final Long firstId, final Long secondId);

    public abstract R createMockEmptyRelation();

}
