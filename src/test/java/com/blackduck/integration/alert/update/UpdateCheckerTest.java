/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.update;

import static com.blackduck.integration.alert.common.util.DateUtils.DOCKER_DATE_FORMAT;
import static com.blackduck.integration.alert.common.util.DateUtils.formatDate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.rest.AlertWebServerUrlManager;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.common.system.SystemInfoReader;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.job.api.DefaultSystemStatusAccessor;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.alert.update.model.UpdateModel;
import com.blackduck.integration.alert.web.api.about.AboutReader;
import com.blackduck.integration.alert.web.api.metadata.DescriptorMetadataActions;
import com.blackduck.integration.alert.web.api.metadata.model.DescriptorsResponseModel;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;

class UpdateCheckerTest {
    private static final String SUFFIX_SNAPSHOT = "SNAPSHOT";
    private static final String SUFFIX_SIGQA_1 = "SIGQA1";
    private static final String SUFFIX_OTHER_TEXT = "TEXT-UNKNOWN-TAG";
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    @Test
    void testAlertIsNewer() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "0.1.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    void testAlertIsNewerPatch() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0.1", null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    void testAlertIsOlder() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "1.0.1", null, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    void testAlertIsSame() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    void testAlertIsOlderDockerPatch() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "1.0.0.1", null, null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsNewerSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, null, "0.1.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsNewerSnapshotPatch(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0.1-" + versionSuffix, null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsNewerButCloseSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.minusMinutes(20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsOlderButCloseSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsOlderSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsOlderSnapshotDockerPatch(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0.1", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsNewerBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, null, "0.1.0-" + versionSuffix, null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsNewerDateBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.minusMinutes(80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsNewerButCloseBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.minusMinutes(20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsOlderButCloseBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsOlderBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsOlderButCloseDockerSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    void testAlertIsOlderDockerSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    @Tags({
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    void getUpdateModelTest() {
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);

        DefaultSystemStatusAccessor defaultSystemStatusUtility = Mockito.mock(DefaultSystemStatusAccessor.class);
        Mockito.when(defaultSystemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        Mockito.when(defaultSystemStatusUtility.getStartupTime()).thenReturn(DateUtils.createCurrentDateTimestamp());

        DescriptorMetadataActions descriptorMetadataActions = Mockito.mock(DescriptorMetadataActions.class);
        Mockito.when(descriptorMetadataActions.getDescriptorsByType(Mockito.anyString())).thenReturn(new ActionResponse<>(HttpStatus.OK, new DescriptorsResponseModel()));

        AlertWebServerUrlManager alertWebServerUrlManager = Mockito.mock(AlertWebServerUrlManager.class);
        Mockito.when(alertWebServerUrlManager.getServerComponentsBuilder()).thenReturn(UriComponentsBuilder.newInstance());

        AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertTrustCertificate()).thenReturn(Optional.of(Boolean.TRUE));
        SystemInfoReader systemInfoReader = new SystemInfoReader(gson);
        AboutReader reader = new AboutReader(systemInfoReader, defaultSystemStatusUtility, descriptorMetadataActions);
        UpdateChecker updateChecker = new UpdateChecker(gson, reader, proxyManager, alertProperties);

        UpdateModel updateModel = updateChecker.getUpdateModel();

        assertNotNull(updateModel);
    }

    private UpdateChecker getEmptyUpdateChecker() {
        return new UpdateChecker(null, null, null, null);
    }

}
