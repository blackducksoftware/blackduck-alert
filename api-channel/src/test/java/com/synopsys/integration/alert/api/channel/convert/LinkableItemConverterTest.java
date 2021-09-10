package com.synopsys.integration.alert.api.channel.convert;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.convert.mock.MockChannelMessageFormatter;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class LinkableItemConverterTest {
    private static final LinkableItem TEST_LINKABLE_ITEM = new LinkableItem("The Label", "The Value", "https://a-url");

    @Test
    public void convertToStringTest() {
        callConvertToString(false);
    }

    @Test
    public void convertToStringBoldTest() {
        callConvertToString(true);
    }

    @Test
    @Disabled
    public void previewConvertToStringFormatting() {
        String defaultString = callConvertToString(false);
        System.out.println("Not bold: ");
        System.out.print(defaultString);

        String boldString = callConvertToString(true);
        System.out.println("Bold: ");
        System.out.print(boldString);
    }

    @Test
    public void convertToStringWithoutLinkTest() {
        callConvertToStringWithoutLink(false);
    }

    @Test
    public void convertToStringWithoutLinkBoldTest() {
        callConvertToStringWithoutLink(true);
    }

    @Test
    @Disabled
    public void previewConvertToStringWithoutLinkFormatting() {
        String defaultString = callConvertToStringWithoutLink(false);
        System.out.println("Not bold: ");
        System.out.print(defaultString);

        String boldString = callConvertToStringWithoutLink(true);
        System.out.println("Bold: ");
        System.out.print(boldString);
    }

    private String callConvertToString(boolean bold) {
        MockChannelMessageFormatter formatter = new MockChannelMessageFormatter(Integer.MAX_VALUE);
        LinkableItemConverter linkableItemConverter = new LinkableItemConverter(formatter);

        return linkableItemConverter.convertToString(TEST_LINKABLE_ITEM, bold);
    }

    private String callConvertToStringWithoutLink(boolean bold) {
        MockChannelMessageFormatter formatter = new MockChannelMessageFormatter(Integer.MAX_VALUE);
        LinkableItemConverter linkableItemConverter = new LinkableItemConverter(formatter);

        return linkableItemConverter.convertToStringWithoutLink(TEST_LINKABLE_ITEM, bold);
    }

}
