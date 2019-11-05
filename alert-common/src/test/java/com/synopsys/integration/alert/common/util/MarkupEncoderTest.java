package com.synopsys.integration.alert.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MarkupEncoderTest {

    @Test
    public void testEncoder() {
        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();
        Map<Character, String> encoding = Map.of('*', "\\*", '~', "\\~", '#', "\\#", '-', "\\-", '_', "\\_");

        String testTxt = "***";
        String expectedTxt = "\\***";
        String encodedTxt = markupEncoderUtil.encodeMarkup(encoding, testTxt);

        assertEquals(expectedTxt, encodedTxt);

        String testMultiple = "**Hello**World";
        String expectedMultiple = "\\**Hello\\**World";
        String encodedMultiple = markupEncoderUtil.encodeMarkup(encoding, testMultiple);

        assertEquals(expectedMultiple, encodedMultiple);

        String testMixture = "*Hello*World~~wow~~\n#####New Line";
        String expectedMixture = "\\*Hello\\*World\\~~wow\\~~\n\\#####New Line";
        String encodedMixture = markupEncoderUtil.encodeMarkup(encoding, testMixture);

        assertEquals(expectedMixture, encodedMixture);
    }

    @Test
    public void testSlackEncoder() {
        MarkupEncoderUtil markupEncoderUtil = new MarkupEncoderUtil();
        LinkedHashMap<Character, String> encodingMap = new LinkedHashMap<>();
        encodingMap.put('&', "&amp;");
        encodingMap.put('<', "&lt;");
        encodingMap.put('>', "&gt;");

        String testTxt = "***";
        String expectedTxt = "***";
        String encodedTxt = markupEncoderUtil.encodeMarkup(encodingMap, testTxt);

        assertEquals(expectedTxt, encodedTxt);

        String testAmpersand = "Mom&Dad&Kid";
        String expectedAmpersand = "Mom&amp;Dad&amp;Kid";
        String encodedAmpersand = markupEncoderUtil.encodeMarkup(encodingMap, testAmpersand);

        assertEquals(expectedAmpersand, encodedAmpersand);

        String testMixed = "5 < 4 & 1 > 8";
        String expectedMixed = "5 &lt; 4 &amp; 1 &gt; 8";
        String encodedMixed = markupEncoderUtil.encodeMarkup(encodingMap, testMixed);

        assertEquals(expectedMixed, encodedMixed);
    }

}
