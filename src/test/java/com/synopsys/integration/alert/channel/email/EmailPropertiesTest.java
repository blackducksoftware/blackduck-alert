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
package com.synopsys.integration.alert.channel.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;

public class EmailPropertiesTest {
    @Test
    public void updateFromConfigTest() {
        final Map<String, ConfigurationFieldModel> fieldMap = new LinkedHashMap<>();
        for (final EmailPropertyKeys emailKey : EmailPropertyKeys.values()) {
            final String key = emailKey.getPropertyKey();
            final ConfigurationFieldModel model = ConfigurationFieldModel.create(key);
            fieldMap.put(key, model);
            model.setFieldValue(key + "_value");
        }

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
        final EmailProperties emailProperties = new EmailProperties(fieldAccessor);

        for (final String key : fieldMap.keySet()) {
            final String expected = key + "_value";
            final String actual = emailProperties.getJavamailOption(key);
            assertEquals(expected, actual);
        }

        IllegalArgumentException caughtException = null;
        try {
            new EmailProperties(null);
        } catch (final IllegalArgumentException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);
    }
}
