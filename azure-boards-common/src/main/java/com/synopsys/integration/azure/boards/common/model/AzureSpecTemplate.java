package com.synopsys.integration.azure.boards.common.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class AzureSpecTemplate {
    private final String specTemplate;
    private final Map<String, String> replacementMappings;

    public AzureSpecTemplate(String specTemplate) {
        this.specTemplate = specTemplate;
        this.replacementMappings = new HashMap<>();
    }

    private AzureSpecTemplate(String specTemplate, Map<String, String> replacementMappings) {
        this.specTemplate = specTemplate;
        this.replacementMappings = replacementMappings;
    }

    public AzureSpecTemplate defineReplacement(String specTemplateKey, String replacementString) {
        HashMap<String, String> populatedReplacementMappings = new HashMap<>(replacementMappings);
        populatedReplacementMappings.put(specTemplateKey, replacementString);
        return new AzureSpecTemplate(specTemplate, populatedReplacementMappings);
    }

    public String populateSpec() {
        String populatedSpec = specTemplate;
        for (Map.Entry<String, String> replacementEntry : replacementMappings.entrySet()) {
            populatedSpec = StringUtils.replaceOnce(populatedSpec, replacementEntry.getKey(), replacementEntry.getValue());
        }
        return populatedSpec;
    }

    public String populateSpecAndClearReplacements() {
        String populatedSpec = populateSpec();
        if (!replacementMappings.isEmpty()) {
            replacementMappings.clear();
        }
        return populatedSpec;
    }

}
