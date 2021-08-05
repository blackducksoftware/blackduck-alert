package com.synopsys.integration.alert.api.channel.convert;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.convert.mock.MockChannelMessageFormatter;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;

public class SimpleMessageConverterTest {
    @Test
    public void convertToFormattedMessageChunksTest() {
        callConvertToFormattedMessageChunks();
    }

    @Test
    @Disabled
    public void previewConvertToFormattedMessageChunksFormatting() {
        List<String> messageChunks = callConvertToFormattedMessageChunks();
        String joinedMessageChunks = StringUtils.join(messageChunks, "");
        System.out.print(joinedMessageChunks);
    }

    private List<String> callConvertToFormattedMessageChunks() {
        ProviderDetails providerDetails = new ProviderDetails(0L, new LinkableItem("Provider", "The provider name"));
        LinkableItem detail1 = new LinkableItem("Detail", "The first detail (unlinked)");
        LinkableItem detail2 = new LinkableItem("Detail Prime", "The second detail (linked)", "https://a-hub-url");
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, "The Summary", "The Description", List.of(detail1, detail2));

        MockChannelMessageFormatter formatter = new MockChannelMessageFormatter(Integer.MAX_VALUE);
        SimpleMessageConverter simpleMessageConverter = new SimpleMessageConverter(formatter);

        return simpleMessageConverter.convertToFormattedMessageChunks(simpleMessage, "jobName");
    }

}
