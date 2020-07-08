package com.synopsys.integration.azure.boards.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AzureSpecTemplateTest {
    @Test
    public void basicReplacementTest() {
        String prefix = "prefix ";
        String suffix = " suffix";
        String stringToReplace = "<replace_this>";
        String replacementValue = "REPLACEMENT";
        String templateString = prefix + stringToReplace + suffix;
        String expectedResultString = prefix + replacementValue + suffix;

        AzureSpecTemplate azureSpecTemplate = new AzureSpecTemplate(templateString);
        assertEquals(templateString, azureSpecTemplate.populateSpec());

        AzureSpecTemplate conditionedSpec = azureSpecTemplate.defineReplacement(stringToReplace, replacementValue);
        assertEquals(expectedResultString, conditionedSpec.populateSpec());
    }

}
