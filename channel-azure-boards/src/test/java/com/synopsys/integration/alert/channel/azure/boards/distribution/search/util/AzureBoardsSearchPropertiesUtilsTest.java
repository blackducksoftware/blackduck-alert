package com.synopsys.integration.alert.channel.azure.boards.distribution.search.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

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
}
