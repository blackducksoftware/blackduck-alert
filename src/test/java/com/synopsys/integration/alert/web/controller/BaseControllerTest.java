package com.synopsys.integration.alert.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.rest.api.BaseController;

public class BaseControllerTest {

    @Test
    public void testValidLoggableString() {
        String validString = "avalidstring";
        String actual = BaseController.createSaferLoggableString(validString);
        assertEquals(validString, actual);
    }

    @Test
    public void testInvalidLoggableStrings() {
        String testWithNewline = "a\nnewline\n\nstring";
        String testWithTab = "a\t\ttabbed\tstring\t\t";
        String testWithCarriageReturn = "\r\r \ra\rcarriage\r\r\rreturn\r \r\r";
        String complexTest = "\t a\n\r \tcomplex \r\n \t test \n\r\tstring\r";

        String actual = BaseController.createSaferLoggableString(testWithNewline);
        assertEquals("a_newline__string", actual);
        actual = BaseController.createSaferLoggableString(testWithTab);
        assertEquals("a__tabbed_string__", actual);
        actual = BaseController.createSaferLoggableString(testWithCarriageReturn);
        assertEquals("__ _a_carriage___return_ __", actual);
        actual = BaseController.createSaferLoggableString(complexTest);
        assertEquals("_ a__ _complex __ _ test ___string_", actual);
    }
}
