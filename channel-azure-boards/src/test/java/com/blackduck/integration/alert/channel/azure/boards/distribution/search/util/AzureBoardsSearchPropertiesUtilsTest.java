package com.blackduck.integration.alert.channel.azure.boards.distribution.search.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

class AzureBoardsSearchPropertiesUtilsTest {
    @Test
    void createNullableLinkableItemKeyWithQuotesTest() {
        String label = "ProjectName";
        String value = "Project'VersionWithSingleQuote";
        String url = "https://url";
        LinkableItem linkableItem = new LinkableItem(label, value, url);
        String stringWithQuotesEscaped = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(linkableItem);

        String expectedString = String.format("%s:%s|%s", label, "Project''VersionWithSingleQuote", url);
        assertEquals(expectedString, stringWithQuotesEscaped);
    }

    @Test
    void createNullableLinkableItemKeyTruncates() {
        String label = "ProjectName";
        String value = "Project'VersionWithSingleQuote";
        // 277 character count
        String url = "https://Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas ultrices nibh semper eros sollicitudin, ac mattis quam laoreet. Quisque rhoncus vitae purus in lacinia. Maecenas suscipit, leo a sodales dignissim, urna libero dictum odio, at semper leo velit eu turpis.";
        LinkableItem linkableItem = new LinkableItem(label, value, url);
        String stringWithQuotesEscaped = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(linkableItem);

        assertEquals(AzureBoardsSearchPropertiesUtils.MAX_STRING_VALUE_LENGTH, stringWithQuotesEscaped.length());
    }
}
