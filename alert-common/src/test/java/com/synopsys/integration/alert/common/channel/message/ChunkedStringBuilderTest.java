package com.synopsys.integration.alert.common.channel.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ChunkedStringBuilderTest {
    @Test
    public void appendLessThanMaxLengthTest() {
        String testInput = "abcdefghij";
        testOneString(testInput, testInput.length() + 1);
    }

    @Test
    public void appendEqualToMaxLengthTest() {
        String testInput = "abcdefghij";
        testOneString(testInput, testInput.length());
    }

    @Test
    public void appendGreaterThanMaxLengthTest() {
        String testInput = "abcdefghij";
        int maxLength = testInput.length() - 1;
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(maxLength);
        chunkedStringBuilder.append(testInput);

        List<String> chunks = chunkedStringBuilder.collectCurrentChunks();
        assertEquals(2, chunks.size());

        String firstChunk = chunks.get(0);
        assertEquals(maxLength, firstChunk.length());

        assertJoinedChunks(testInput, chunks);
    }

    @Test
    public void preferredBreakAfterCharTest() {
        char inputChar = '$';
        testBreakAfterChar("12345$67890", inputChar, inputChar);
    }

    @Test
    public void defaultBreakAfterCharTest() {
        testBreakAfterChar("12345" + ChunkedStringBuilder.DEFAULT_BREAK_AFTER_CHAR + "67890", '$', ChunkedStringBuilder.DEFAULT_BREAK_AFTER_CHAR);
    }

    @Test
    public void noBreakAfterCharTest() {
        String testInput = "123456789";
        int maxLength = testInput.length() - 1;
        char preferredBreakAfterChar = '$';
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(maxLength, preferredBreakAfterChar);
        chunkedStringBuilder.append(testInput);

        List<String> chunks = chunkedStringBuilder.collectCurrentChunks();
        assertEquals(2, chunks.size());

        String firstChunk = chunks.get(0);
        assertEquals(maxLength, firstChunk.length());

        assertJoinedChunks(testInput, chunks);
    }

    @Test
    public void inputPlusChunkLengthGreaterThanMaxLengthTest() {
        String testInput = "abcdefghij";
        int maxLength = testInput.length() + 1;
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(maxLength);
        chunkedStringBuilder.append(testInput);
        chunkedStringBuilder.append(testInput);

        List<String> chunks = chunkedStringBuilder.collectCurrentChunks();
        assertEquals(2, chunks.size());

        String firstChunk = chunks.get(0);
        assertTrue(firstChunk.length() < maxLength);

        assertJoinedChunks(testInput + testInput, chunks);
    }

    private void testOneString(String input, int maxLength) {
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(maxLength);
        chunkedStringBuilder.append(input);

        List<String> chunks = chunkedStringBuilder.collectCurrentChunks();
        assertEquals(1, chunks.size());

        String firstChunk = chunks.get(0);
        assertEquals(input, firstChunk);
    }

    private void testBreakAfterChar(String testInput, char preferredBreakAfterChar, char expectedFirstChunkEndCharacter) {
        int maxLength = testInput.length() - 1;
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(maxLength, preferredBreakAfterChar);
        chunkedStringBuilder.append(testInput);

        List<String> chunks = chunkedStringBuilder.collectCurrentChunks();
        assertEquals(2, chunks.size());

        String firstChunk = chunks.get(0);
        assertTrue(firstChunk.endsWith("" + expectedFirstChunkEndCharacter), "Expected first chunk to end with preferred break-after character");

        String secondChunk = chunks.get(1);
        assertTrue(secondChunk.startsWith("6"), "Expected second chunk to start with a different character");

        assertJoinedChunks(testInput, chunks);
    }

    private void assertJoinedChunks(String expected, List<String> chunks) {
        String joinedChunks = String.join("", chunks);
        assertEquals(expected, joinedChunks);
    }

}
