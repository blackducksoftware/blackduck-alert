/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.util;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MarkupEncoderUtil {

    public String encodeMarkup(Map<Character, String> itemsToReplace, String txt) {
        StringBuilder newTxt = new StringBuilder(txt);
        int cursor = 0;
        char characterMemory = Character.MIN_VALUE;
        while (cursor < newTxt.length()) {
            char c = newTxt.charAt(cursor);
            if (characterMemory == c) {
                cursor++;
                continue;
            }

            characterMemory = c;
            String newString = itemsToReplace.get(c);
            if (null != newString) {
                newTxt.insert(cursor, newString);
                cursor += newString.length();
                newTxt.deleteCharAt(cursor);
            } else {
                cursor++;
            }
        }

        return newTxt.toString();
    }

}
