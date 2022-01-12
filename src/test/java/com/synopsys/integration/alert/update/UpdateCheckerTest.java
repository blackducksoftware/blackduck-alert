package com.synopsys.integration.alert.update;

import static com.synopsys.integration.alert.common.util.DateUtils.DOCKER_DATE_FORMAT;
import static com.synopsys.integration.alert.common.util.DateUtils.formatDate;
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

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.rest.AlertWebServerUrlManager;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultSystemStatusAccessor;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.update.model.UpdateModel;
import com.synopsys.integration.alert.web.api.about.AboutReader;
import com.synopsys.integration.alert.web.api.metadata.DescriptorMetadataActions;
import com.synopsys.integration.alert.web.api.metadata.model.DescriptorsResponseModel;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class UpdateCheckerTest {
    private static final String SUFFIX_SNAPSHOT = "SNAPSHOT";
    private static final String SUFFIX_SIGQA_1 = "SIGQA1";
    private static final String SUFFIX_OTHER_TEXT = "TEXT-UNKNOWN-TAG";
    private final Gson gson = new Gson();

    @Test
    public void testAlertIsNewer() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "0.1.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerPatch() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0.1", null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlder() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "1.0.1", null, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsSame() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderDockerPatch() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "1.0.0.1", null, null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsNewerSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, null, "0.1.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsNewerSnapshotPatch(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0.1-" + versionSuffix, null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsNewerButCloseSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.minusMinutes(20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsOlderButCloseSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsOlderSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsOlderSnapshotDockerPatch(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0.1", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsNewerBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, null, "0.1.0-" + versionSuffix, null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsNewerDateBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.minusMinutes(80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsNewerButCloseBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.minusMinutes(20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsOlderButCloseBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsOlderBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsOlderButCloseDockerSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        OffsetDateTime alertTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime dockerTagDate = alertTime.plusMinutes(20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { SUFFIX_SNAPSHOT, SUFFIX_SIGQA_1, SUFFIX_OTHER_TEXT })
    public void testAlertIsOlderDockerSnapshot(String versionSuffix) {
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
    public void getUpdateModelTest() {
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

        AboutReader reader = new AboutReader(gson, alertWebServerUrlManager, defaultSystemStatusUtility, descriptorMetadataActions);
        UpdateChecker updateChecker = new UpdateChecker(gson, reader, proxyManager, alertProperties);

        UpdateModel updateModel = updateChecker.getUpdateModel();

        assertNotNull(updateModel);
    }

    private UpdateChecker getEmptyUpdateChecker() {
        return new UpdateChecker(null, null, null, null);
    }

}
