package com.synopsys.integration.alert.channel.msteams;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MsTeamsMessageParserTest {

    @Test
    public void testEncoder() {
        TestMsTeamsMessageParser testMsTeamsMessageParser = new TestMsTeamsMessageParser();

        String testTxt = "***";
        String expectedTxt = "\\***";
        String encodedTxt = testMsTeamsMessageParser.testEncode(testTxt);

        assertEquals(expectedTxt, encodedTxt);

        String testMultiple = "**Hello**World";
        String expectedMultiple = "\\**Hello\\**World";
        String encodedMultiple = testMsTeamsMessageParser.testEncode(testMultiple);

        assertEquals(expectedMultiple, encodedMultiple);

        String testMixture = "*Hello*World~~wow~~\n#####New Line";
        String expectedMixture = "\\*Hello\\*World\\~~wow\\~~\n\\#####New Line";
        String encodedMixture = testMsTeamsMessageParser.testEncode(testMixture);

        assertEquals(expectedMixture, encodedMixture);
    }

    class TestMsTeamsMessageParser extends MsTeamsMessageParser {
        public String testEncode(String txt) {
            return encodeString(txt);
        }
    }

}
