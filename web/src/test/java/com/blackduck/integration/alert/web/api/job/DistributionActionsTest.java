/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.job;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort;

import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.api.descriptor.MsTeamsKey;
import com.blackduck.integration.alert.api.descriptor.SlackChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.descriptor.DescriptorMap;
import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.rest.model.DistributionWithAuditInfo;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.test.common.MockDescriptorMap;

public class DistributionActionsTest {
    private final String slackKey = new SlackChannelKey().getUniversalKey();
    private final String msTeamsKey = new MsTeamsKey().getUniversalKey();
    private final String jiraCloudKey = new JiraCloudChannelKey().getUniversalKey();

    private final DescriptorMap descriptorMap = new MockDescriptorMap();

    @Test
    void verifyJobDataRetrievedTest() {
        DistributionActions distributionActions = createDistributionActions((first, second) -> 0);
        ActionResponse<AlertPagedModel<DistributionWithAuditInfo>> pagedResponse = distributionActions.retrieveJobWithAuditInfo(1, 100, "name", Sort.Direction.DESC.name(), null);

        assertNotNull(pagedResponse);
        assertTrue(pagedResponse.isSuccessful());

        Optional<AlertPagedModel<DistributionWithAuditInfo>> optionalContent = pagedResponse.getContent();
        assertTrue(optionalContent.isPresent());

        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfoAlertPagedModel = optionalContent.get();
        List<DistributionWithAuditInfo> auditInfos = distributionWithAuditInfoAlertPagedModel.getModels();
        assertTrue(auditInfos.size() > 0);
        assertContainsAllChannelKeys(auditInfos);
    }

    @Test
    void validateChannelKeyNameSortASCTest() {
        DistributionActions distributionActions = createDistributionActions(Comparator.comparing(DistributionWithAuditInfo::getChannelName));
        ActionResponse<AlertPagedModel<DistributionWithAuditInfo>> pagedResponse = distributionActions.retrieveJobWithAuditInfo(1, 100, "channel", Sort.Direction.DESC.name(), null);

        assertNotNull(pagedResponse);

        Optional<AlertPagedModel<DistributionWithAuditInfo>> contentOptional = pagedResponse.getContent();
        assertTrue(contentOptional.isPresent());

        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfoAlertPagedModel = contentOptional.get();
        List<String> channelNames = distributionWithAuditInfoAlertPagedModel.transformContent(DistributionWithAuditInfo::getChannelName).getModels();

        assertTrue(channelNames.size() > 0);

        String previousValue = null;
        for (String channelName : channelNames) {
            if (previousValue != null) {
                Optional<DescriptorKey> currentKey = descriptorMap.getDescriptorKey(channelName);
                Optional<DescriptorKey> lastKey = descriptorMap.getDescriptorKey(previousValue);
                assertTrue(currentKey.isPresent() && lastKey.isPresent());

                boolean comparedNames = lastKey.get().getDisplayName().compareTo(currentKey.get().getDisplayName()) >= 0;
                assertTrue(comparedNames);
            }
            previousValue = channelName;
        }
    }

    private DistributionActions createDistributionActions(Comparator<DistributionWithAuditInfo> sorter) {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);

        DistributionAccessor defaultDistributionAccessor = Mockito.mock(DistributionAccessor.class);
        Mockito.when(defaultDistributionAccessor.getDistributionWithAuditInfo(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.any(), Mockito.anySet())).thenReturn(createDistributionInfo(sorter));

        DistributionActions distributionActions = new DistributionActions(defaultDistributionAccessor, authorizationManager, descriptorMap);
        return distributionActions;
    }

    private void assertContainsAllChannelKeys(List<DistributionWithAuditInfo> auditInfos) {
        assertTrue(auditInfos.stream()
            .map(DistributionWithAuditInfo::getChannelName)
            .allMatch(predicate -> predicate.equals(slackKey) || predicate.equals(msTeamsKey) || predicate.equals(jiraCloudKey)));
    }

    private AlertPagedModel<DistributionWithAuditInfo> createDistributionInfo(Comparator<DistributionWithAuditInfo> sorter) {
        List<DistributionWithAuditInfo> models = List.of(
                createDistributionInfoWithAuditInfo(slackKey),
                createDistributionInfoWithAuditInfo(slackKey),
                createDistributionInfoWithAuditInfo(msTeamsKey),
                createDistributionInfoWithAuditInfo(jiraCloudKey)
            ).stream()
            .sorted(sorter)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(1, 1, 100, models);
    }

    private DistributionWithAuditInfo createDistributionInfoWithAuditInfo(String channelName) {
        return new DistributionWithAuditInfo(
            UUID.randomUUID(),
            true,
            "jobName",
            channelName,
            FrequencyType.REAL_TIME,
            "auditTimeLastSent",
            AuditEntryStatus.SUCCESS.toString()
        );
    }
}
