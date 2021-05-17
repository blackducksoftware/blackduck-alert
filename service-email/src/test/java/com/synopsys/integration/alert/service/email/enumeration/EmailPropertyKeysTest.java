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
package com.synopsys.integration.alert.service.email.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.email.EmailProperties;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;

public class EmailPropertyKeysTest {
    @Test
    public void updateFromConfigTest() {
        Map<String, ConfigurationFieldModel> fieldMap = new LinkedHashMap<>();
        for (com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys emailKey : EmailPropertyKeys.values()) {
            if (!emailKey.name().startsWith("EMAIL") && !emailKey.name().startsWith("TEMPLATE")) {
                String key = emailKey.getPropertyKey();
                ConfigurationFieldModel model = ConfigurationFieldModel.create(key);
                fieldMap.put(key, model);
                model.setFieldValue(key + "_value");
            }
        }

        FieldUtility fieldUtility = new FieldUtility(fieldMap);
        EmailProperties emailProperties = new EmailProperties(fieldUtility);

        for (String key : fieldMap.keySet()) {
            String expected = key + "_value";
            String actual = emailProperties.getJavamailOption(key);
            assertEquals(expected, actual);
        }

        IllegalArgumentException caughtException = null;
        try {
            new EmailProperties((FieldUtility) null);
        } catch (IllegalArgumentException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);
    }

}
