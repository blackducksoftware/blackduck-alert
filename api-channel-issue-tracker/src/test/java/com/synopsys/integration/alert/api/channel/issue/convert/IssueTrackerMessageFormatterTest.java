package com.synopsys.integration.alert.api.channel.issue.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.convert.ChannelMessageFormatter;

public class IssueTrackerMessageFormatterTest {
    @Test
    public void shortConstructorTest() {
        int maxTitleLength = 10;
        int maxDescriptionLength = 25;
        int maxCommentLength = 18;
        String lineSeparator = "This can be anything...";
        IssueTrackerMessageFormatter formatter = new IssueTrackerMessageFormatterImpl(maxTitleLength, maxDescriptionLength, maxCommentLength, lineSeparator);
        runAssertions(formatter, maxTitleLength, maxDescriptionLength, maxCommentLength, lineSeparator, ChannelMessageFormatter.DEFAULT_SECTION_SEPARATOR, ChannelMessageFormatter.DEFAULT_NON_BREAKING_SPACE);
    }

    @Test
    public void fullConstructorTest() {
        int maxTitleLength = 50;
        int maxDescriptionLength = 100;
        int maxCommentLength = 80;
        String lineSeparator = "\n \n\r \t \\ \"";
        String sectionSeparator = "~~~~~";
        String nonBreakingSpace = ">|<";
        IssueTrackerMessageFormatter formatter = new IssueTrackerMessageFormatterImpl(maxTitleLength, maxDescriptionLength, maxCommentLength, lineSeparator, sectionSeparator, nonBreakingSpace);
        runAssertions(formatter, maxTitleLength, maxDescriptionLength, maxCommentLength, lineSeparator, sectionSeparator, nonBreakingSpace);
    }

    private static void runAssertions(IssueTrackerMessageFormatter formatter, int maxTitleLength, int maxDescriptionLength, int maxCommentLength, String lineSeparator, String sectionSeparator, String nonBreakingSpace) {
        assertEquals(maxTitleLength, formatter.getMaxTitleLength());
        assertEquals(maxDescriptionLength, formatter.getMaxDescriptionLength());
        assertEquals(formatter.getMaxMessageLength(), formatter.getMaxDescriptionLength());
        assertEquals(maxCommentLength, formatter.getMaxCommentLength());
        assertEquals(lineSeparator, formatter.getLineSeparator());
        assertEquals(sectionSeparator + nonBreakingSpace, formatter.getSectionSeparator());
        assertEquals(nonBreakingSpace, formatter.getNonBreakingSpace());

        String textTxt = "A test string";
        String textUrl = "https://a-url";
        IssueTrackerMessageFormatter altFormatter = new IssueTrackerMessageFormatterImpl(1, 1, 1, "");
        assertEquals(altFormatter.encode(textTxt), formatter.encode(textTxt));
        assertEquals(altFormatter.emphasize(textTxt), formatter.emphasize(textTxt));
        assertEquals(altFormatter.createLink(textTxt, textUrl), formatter.createLink(textTxt, textUrl));
    }

    private static class IssueTrackerMessageFormatterImpl extends IssueTrackerMessageFormatter {
        public IssueTrackerMessageFormatterImpl(int maxTitleLength, int maxDescriptionLength, int maxCommentLength, String lineSeparator) {
            super(maxTitleLength, maxDescriptionLength, maxCommentLength, lineSeparator);
        }

        public IssueTrackerMessageFormatterImpl(int maxTitleLength, int maxDescriptionLength, int maxCommentLength, String lineSeparator, String sectionSeparator, String nonBreakingSpace) {
            super(maxTitleLength, maxDescriptionLength, maxCommentLength, lineSeparator, sectionSeparator, nonBreakingSpace);
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
