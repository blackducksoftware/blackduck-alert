package com.synopsys.integration.alert.web.channel.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;

public class CommonDistributionConfigActionsTest {

    @Test
    public void validateCommonConfigTestEmpty() {
        final CommonDistributionConfigActions commonDistributionConfigActions = new CommonDistributionConfigActions(null, null, null, null);

        final CommonDistributionConfig commonDistributionConfig = new CommonDistributionConfig();
        final Map<String, String> fieldErrors = new HashMap<>();

        commonDistributionConfigActions.validateCommonConfig(commonDistributionConfig, fieldErrors);
        assertTrue(6 == fieldErrors.size());
        assertTrue(fieldErrors.containsKey("name"));
        assertEquals("Name cannot be blank.", fieldErrors.get("name"));
        assertTrue(fieldErrors.containsKey("distributionType"));
        assertEquals("You must choose a distribution type.", fieldErrors.get("distributionType"));
        assertTrue(fieldErrors.containsKey("providerName"));
        assertEquals("You must choose a provider.", fieldErrors.get("providerName"));
        assertTrue(fieldErrors.containsKey("formatType"));
        assertEquals("You must choose a format.", fieldErrors.get("formatType"));
        assertTrue(fieldErrors.containsKey("frequency"));
        assertEquals("Frequency cannot be blank.", fieldErrors.get("frequency"));
        assertTrue(fieldErrors.containsKey("notificationTypes"));
        assertEquals("Must have at least one notification type.", fieldErrors.get("notificationTypes"));
    }

    @Test
    public void validateCommonConfigTestEntirelyWrong() {
        final CommonDistributionConfigEntity conflictingNameEntity = new CommonDistributionConfigEntity();
        final CommonDistributionRepository commonDistributionRepository = Mockito.mock(CommonDistributionRepository.class);
        Mockito.when(commonDistributionRepository.findByName(Mockito.anyString())).thenReturn(conflictingNameEntity);

        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        final CommonDistributionConfigActions commonDistributionConfigActions = new CommonDistributionConfigActions(null, null, commonDistributionRepository, contentConverter);

        final CommonDistributionConfig commonDistributionConfig = new CommonDistributionConfig("100", "Not numeric", "", "Already exists", "", "", "cat",
            "Definitely not a Pattern....435454534(****", Collections.emptyList(), Collections.emptyList(), "");
        final Map<String, String> fieldErrors = new HashMap<>();
        commonDistributionConfigActions.validateCommonConfig(commonDistributionConfig, fieldErrors);

        assertTrue(9 == fieldErrors.size());
        assertTrue(fieldErrors.containsKey("name"));
        assertEquals("A distribution configuration with this name already exists.", fieldErrors.get("name"));
        assertTrue(fieldErrors.containsKey("distributionConfigId"));
        assertEquals("Not an Integer.", fieldErrors.get("distributionConfigId"));
        assertTrue(fieldErrors.containsKey("distributionType"));
        assertEquals("You must choose a distribution type.", fieldErrors.get("distributionType"));
        assertTrue(fieldErrors.containsKey("providerName"));
        assertEquals("You must choose a provider.", fieldErrors.get("providerName"));
        assertTrue(fieldErrors.containsKey("filterByProject"));
        assertEquals("Not a Boolean.", fieldErrors.get("filterByProject"));
        assertTrue(fieldErrors.containsKey("projectNamePattern"));
        assertTrue(fieldErrors.get("projectNamePattern").contains("Project name pattern is not a regular expression. "));
        assertTrue(fieldErrors.containsKey("formatType"));
        assertEquals("You must choose a format.", fieldErrors.get("formatType"));
        assertTrue(fieldErrors.containsKey("frequency"));
        assertEquals("Frequency cannot be blank.", fieldErrors.get("frequency"));
        assertTrue(fieldErrors.containsKey("notificationTypes"));
        assertEquals("Must have at least one notification type.", fieldErrors.get("notificationTypes"));
    }

    @Test
    public void validateCommonConfigTestRemainingErrors() {
        final CommonDistributionRepository commonDistributionRepository = Mockito.mock(CommonDistributionRepository.class);

        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        final CommonDistributionConfigActions commonDistributionConfigActions = new CommonDistributionConfigActions(null, null, commonDistributionRepository, contentConverter);

        final CommonDistributionConfig commonDistributionConfig = new CommonDistributionConfig("Not numeric", "100", "type", "New name", "provider", "frequency", "true",
            "", Collections.emptyList(), Arrays.asList("notificationType"), "format");
        final Map<String, String> fieldErrors = new HashMap<>();
        commonDistributionConfigActions.validateCommonConfig(commonDistributionConfig, fieldErrors);

        assertTrue(2 == fieldErrors.size());
        assertTrue(fieldErrors.containsKey("id"));
        assertEquals("Not an Integer.", fieldErrors.get("id"));
        assertTrue(fieldErrors.containsKey("configuredProjects"));
        assertEquals("You must select at least one project.", fieldErrors.get("configuredProjects"));
    }

}
