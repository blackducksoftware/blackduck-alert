package com.synopsys.integration.azure.boards.common;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.azure.boards.common.model.AzureSpecTemplate;

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
