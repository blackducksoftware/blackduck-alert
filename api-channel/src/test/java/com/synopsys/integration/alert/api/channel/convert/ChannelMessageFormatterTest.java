package com.synopsys.integration.alert.api.channel.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ChannelMessageFormatterTest {
    @Test
    public void shortConstructorTest() {
        int maxMessageLength = 10;
        String lineSeparator = "Reality can be whatever I want it to be...";

        ChannelMessageFormatterImpl formatter = new ChannelMessageFormatterImpl(maxMessageLength, lineSeparator);
        runAssertions(
            formatter,
            maxMessageLength,
            lineSeparator,
            ChannelMessageFormatter.DEFAULT_SECTION_SEPARATOR,
            ChannelMessageFormatter.DEFAULT_NON_BREAKING_SPACE
        );
    }

    @Test
    public void fullConstructorTest() {
        int maxMessageLength = 25;
        String lineSeparator = "<br />, \n, \n\r";
        String nonBreakingSpace = "\t";
        String sectionSeparator = "[NEW SECTION]";

        ChannelMessageFormatterImpl formatter = new ChannelMessageFormatterImpl(maxMessageLength, lineSeparator, sectionSeparator, nonBreakingSpace);
        runAssertions(
            formatter,
            maxMessageLength,
            lineSeparator,
            sectionSeparator,
            nonBreakingSpace
        );
    }

    private void runAssertions(ChannelMessageFormatter formatter, int maxMessageLength, String lineSeparator, String sectionSeparator, String nonBreakingSpace) {
        assertEquals(maxMessageLength, formatter.getMaxMessageLength());
        assertEquals(lineSeparator, formatter.getLineSeparator());
        assertEquals(sectionSeparator + nonBreakingSpace, formatter.getSectionSeparator());
        assertEquals(nonBreakingSpace, formatter.getNonBreakingSpace());

        String textTxt = "A test string";
        String textUrl = "https://a-url";
        ChannelMessageFormatterImpl altFormatter = new ChannelMessageFormatterImpl(1, "");
        assertEquals(altFormatter.encode(textTxt), formatter.encode(textTxt));
        assertEquals(altFormatter.emphasize(textTxt), formatter.emphasize(textTxt));
        assertEquals(altFormatter.createLink(textTxt, textUrl), formatter.createLink(textTxt, textUrl));
    }

    private static class ChannelMessageFormatterImpl extends ChannelMessageFormatter {
        public ChannelMessageFormatterImpl(int maxMessageLength, String lineSeparator) {
            super(maxMessageLength, lineSeparator);
        }

        public ChannelMessageFormatterImpl(int maxMessageLength, String lineSeparator, String sectionSeparator, String nonBreakingSpace) {
            super(maxMessageLength, lineSeparator, sectionSeparator, nonBreakingSpace);
        }

        @Override
        public String encode(String txt) {
            return txt;
        }

        @Override
        public String emphasize(String txt) {
            return txt;
        }

        @Override
        public String createLink(String txt, String url) {
            return txt;
        }

    }

}
