/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ChunkedStringBuilder {
    public static final char DEFAULT_BREAK_AFTER_CHAR = ' ';

    private final int maxLength;
    private final char preferredBreakAfterChar;
    private final List<String> chunks;

    private StringBuilder currentChunkBuilder;

    /**
     * @param maxLength The maximum length of a chunk.
     */
    public ChunkedStringBuilder(int maxLength) {
        this(maxLength, DEFAULT_BREAK_AFTER_CHAR);
    }

    /**
     * @param maxLength               The maximum length of a chunk.
     * @param preferredBreakAfterChar If an appended String must be broken-up and this character is present in a substring after an initial split,
     *                                this character will be the last character of that substring and the next substring will start immediately after it.
     */
    public ChunkedStringBuilder(int maxLength, char preferredBreakAfterChar) {
        this.maxLength = maxLength;
        this.preferredBreakAfterChar = preferredBreakAfterChar;
        this.chunks = new LinkedList<>();
        this.currentChunkBuilder = new StringBuilder();
    }

    public int getMaxLength() {
        return maxLength;
    }

    public List<String> collectCurrentChunks() {
        List<String> copyOfChunks = new ArrayList<>(chunks);
        if (currentChunkBuilder.length() > 0) {
            copyOfChunks.add(currentChunkBuilder.toString());
        }
        return copyOfChunks;
    }

    public ChunkedStringBuilder append(String str) {
        int strLength = str.length();
        if (strLength > maxLength) {
            List<String> tokens = tokenize(str);
            for (String token : tokens) {
                append(token);
            }
        } else if ((currentChunkBuilder.length() + strLength) > maxLength) {
            chunks.add(currentChunkBuilder.toString());
            currentChunkBuilder = new StringBuilder();
            currentChunkBuilder.append(str);
        } else {
            currentChunkBuilder.append(str);
        }
        return this;
    }

    private List<String> tokenize(String str) {
        List<String> tokens = new LinkedList<>();
        String remainingStr = str;
        while (remainingStr.length() > maxLength) {
            String substring = StringUtils.substring(remainingStr, 0, maxLength);

            int preferredBreakIndex = StringUtils.lastIndexOf(substring, preferredBreakAfterChar);
            if (preferredBreakIndex > 0) {
                substring = StringUtils.substring(substring, 0, preferredBreakIndex + 1);
            } else if (preferredBreakAfterChar != DEFAULT_BREAK_AFTER_CHAR) {
                int defaultBreakIndex = StringUtils.lastIndexOf(substring, DEFAULT_BREAK_AFTER_CHAR);
                if (defaultBreakIndex > 0) {
                    substring = StringUtils.substring(substring, 0, defaultBreakIndex + 1);
                }
            }

            tokens.add(substring);
            remainingStr = StringUtils.substring(remainingStr, substring.length());
        }

        if (StringUtils.isNotEmpty(remainingStr)) {
            tokens.add(remainingStr);
        }
        return tokens;
    }

}
