/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.channel.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MessageSplitter {
    public static final String DEFAULT_LINE_SEPARATOR = "\n";
    public static final char BRACKET_CHARACTER = '[';
    private final int limit;
    private final String lineSeparator;
    private final char itemDelimiter;

    public MessageSplitter(int limit) {
        this(limit, DEFAULT_LINE_SEPARATOR, BRACKET_CHARACTER);
    }

    public MessageSplitter(int limit, String lineSeparator) {
        this(limit, lineSeparator, BRACKET_CHARACTER);
    }

    public MessageSplitter(int limit, String lineSeparator, char itemDelimiter) {
        this.limit = limit;
        this.lineSeparator = lineSeparator;
        this.itemDelimiter = itemDelimiter;
    }

    public final int getLimit() {
        return limit;
    }

    public final String getLineSeparator() {
        return lineSeparator;
    }

    public final List<String> splitMessages(Collection<String> messagePieces) {
        return splitMessages(messagePieces, false);
    }

    public final List<String> splitMessages(Collection<String> messagePieces, boolean separateChunksByLineSeparator) {
        List<String> messageChunks = new ArrayList<>();

        StringBuilder chunkBuilder = new StringBuilder();
        for (String messagePiece : messagePieces) {
            if (messagePiece.length() <= getLimit()) {
                if (messagePiece.length() + chunkBuilder.length() > getLimit()) {
                    chunkBuilder = flushChunks(messageChunks, chunkBuilder);
                }
                chunkBuilder.append(messagePiece);
                if (separateChunksByLineSeparator) {
                    chunkBuilder.append(lineSeparator);
                }
            } else {
                chunkBuilder = flushChunks(messageChunks, chunkBuilder);
                messageChunks.addAll(splitMessage(messagePiece));
            }
        }

        if (chunkBuilder.length() > 0) {
            flushChunks(messageChunks, chunkBuilder);
        }
        return messageChunks;
    }

    private StringBuilder flushChunks(List<String> messageChunks, StringBuilder chunkBuilder) {
        messageChunks.add(chunkBuilder.toString());
        return new StringBuilder();
    }

    private List<String> splitMessage(String message) {
        if (message.length() <= getLimit()) {
            return List.of(message);
        }

        int splitIndex = getSplitIndex(message);
        String preSplit = message.substring(0, splitIndex);
        String postSplit = message.substring(splitIndex);

        List<String> messages = new ArrayList<>();
        messages.add(preSplit);
        messages.addAll(splitMessage(postSplit));

        return messages;
    }

    private int getSplitIndex(String message) {
        int initialSplitIndex = getLimit() - 1;

        String preSplit = message.substring(0, initialSplitIndex);
        String postSplit = message.substring(initialSplitIndex);

        int bracketIndexBefore = preSplit.lastIndexOf(itemDelimiter);
        int newLineIndexBefore = preSplit.lastIndexOf(lineSeparator);
        int closestBeforeSplitIndex = Math.max(bracketIndexBefore, newLineIndexBefore);

        int bracketIndexAfter = postSplit.indexOf(itemDelimiter);
        int newLineIndexAfter = postSplit.indexOf(lineSeparator);
        int closestAfterSplitIndex = initialSplitIndex + Math.max(bracketIndexAfter, newLineIndexAfter);

        int beforeDistance = initialSplitIndex - Math.abs(closestBeforeSplitIndex);
        int afterDistance = Math.abs(closestAfterSplitIndex) - initialSplitIndex;

        int closestToSplitIndex;
        if (beforeDistance < afterDistance) {
            closestToSplitIndex = closestBeforeSplitIndex;
        } else {
            closestToSplitIndex = closestAfterSplitIndex;
        }

        if (closestToSplitIndex != -1) {
            return closestToSplitIndex;
        }

        return message.length() - 1;
    }
}
