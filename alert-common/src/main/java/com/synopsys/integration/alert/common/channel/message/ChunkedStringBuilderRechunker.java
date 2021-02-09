/*
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

import java.util.List;

public final class ChunkedStringBuilderRechunker {
    public static RechunkedModel rechunk(ChunkedStringBuilder originalChunkBuilder, String firstChunkDefaultValue, int remainingChunksMaxLength) {
        return rechunk(originalChunkBuilder.collectCurrentChunks(), firstChunkDefaultValue, originalChunkBuilder.getMaxLength(), remainingChunksMaxLength);
    }

    public static RechunkedModel rechunk(List<String> chunks, String firstChunkDefaultValue, int originalChunkLength, int remainingChunksMaxLength) {
        String firstChunk;
        int chunksSize = chunks.size();
        if (chunksSize > 0) {
            firstChunk = chunks.get(0);
        } else {
            firstChunk = firstChunkDefaultValue;
        }

        List<String> initialRemainingChunks;
        if (chunksSize > 1) {
            initialRemainingChunks = chunks.subList(1, chunksSize);
        } else {
            initialRemainingChunks = List.of();
        }

        List<String> resizedRemainingChunks;
        if (remainingChunksMaxLength >= originalChunkLength) {
            resizedRemainingChunks = initialRemainingChunks;
        } else {
            ChunkedStringBuilder remainingChunksBuilder = new ChunkedStringBuilder(remainingChunksMaxLength);
            initialRemainingChunks.forEach(remainingChunksBuilder::append);
            resizedRemainingChunks = remainingChunksBuilder.collectCurrentChunks();
        }
        return new RechunkedModel(firstChunk, resizedRemainingChunks);
    }

    private ChunkedStringBuilderRechunker() {
    }

}
