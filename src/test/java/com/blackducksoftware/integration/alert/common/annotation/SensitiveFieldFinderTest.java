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
package com.blackducksoftware.integration.alert.common.annotation;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Test;

import com.blackducksoftware.integration.alert.database.annotation.EncryptedStringField;

public class SensitiveFieldFinderTest {

    @Test
    public void findSensitiveFieldsTest() {
        final Set<Field> sensitiveFields = SensitiveFieldFinder.findSensitiveFields(TestSensitiveFields.class);
        assertEquals(3, sensitiveFields.size());
    }

    private class TestSensitiveFields {

        @SensitiveField
        private String mySensitiveField;

        @SensitiveField
        @EncryptedStringField
        private String sensitiveAndEncryted;

        @EncryptedStringField
        private String encrypted;

        @SuppressWarnings("unused")
        private String myInsensitiveField;
    }

}
