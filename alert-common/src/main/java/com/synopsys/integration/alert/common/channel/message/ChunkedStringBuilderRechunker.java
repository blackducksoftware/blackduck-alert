/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
