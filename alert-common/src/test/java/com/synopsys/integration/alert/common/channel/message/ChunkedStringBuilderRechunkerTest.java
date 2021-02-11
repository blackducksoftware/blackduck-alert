package com.synopsys.integration.alert.common.channel.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class ChunkedStringBuilderRechunkerTest {
    @Test
    public void rechunkTest() {
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(10);

        String originalFirstChunk = "1-2-3-4-5,";
        chunkedStringBuilder.append(originalFirstChunk);

        String originalRemainingChunk = "6-7-8-9-10";
        chunkedStringBuilder.append(originalRemainingChunk);

        List<String> chunks = chunkedStringBuilder.collectCurrentChunks();
        assertEquals(2, chunks.size());

        int rechunkSize = 5;
        RechunkedModel rechunked = ChunkedStringBuilderRechunker.rechunk(chunkedStringBuilder, "Default", rechunkSize);
        assertEquals(originalFirstChunk, rechunked.getFirstChunk());

        List<String> remainingChunks = rechunked.getRemainingChunks();
        assertEquals(2, remainingChunks.size());

        for (String remainingChunk : remainingChunks) {
            assertEquals(rechunkSize, remainingChunk.length());
        }

        String joinedRemainingChunks = StringUtils.join(remainingChunks, "");
        assertEquals(originalRemainingChunk, joinedRemainingChunks);
    }

    @Test
    public void rechunkWithDefaultValueTest() {
        String defaultValue = "Default Chunk Value";
        RechunkedModel rechunked = ChunkedStringBuilderRechunker.rechunk(List.of(), defaultValue, 10, 10);
        assertEquals(defaultValue, rechunked.getFirstChunk());
    }

}
