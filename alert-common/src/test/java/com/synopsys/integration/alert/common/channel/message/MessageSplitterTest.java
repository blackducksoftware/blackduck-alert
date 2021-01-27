package com.synopsys.integration.alert.common.channel.message;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MessageSplitterTest {

    @Test
    void messageSplitterTest() {
        int limit = 10;
        String lineSeparator = "\n";
        char delimiter = ' ';
        MessageSplitter messageSplitter = new MessageSplitter(limit, lineSeparator, delimiter);
        List<String> splitMessages = messageSplitter.splitMessages(List.of(
            "category (ADD) - component-value \n",
            // Can produce a StackOverflowError if the split index is 0
            " testing_of_splitting_index_0 ",
            "",
            " "
        ));

        for (String splitMessage : splitMessages) {
            System.out.println("'" + splitMessage + "'");
            Assertions.assertTrue(splitMessage.length() <= limit,
                String.format("Failed to split message by the limit (%d): '%s'", limit, splitMessage)
            );
        }
    }

}
