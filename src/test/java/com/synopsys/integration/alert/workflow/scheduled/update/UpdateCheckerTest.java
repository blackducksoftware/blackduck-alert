package com.synopsys.integration.alert.workflow.scheduled.update;

import static com.synopsys.integration.alert.common.util.DateUtils.DOCKER_DATE_FORMAT;
import static com.synopsys.integration.alert.common.util.DateUtils.formatDate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.AboutReader;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.database.api.DefaultSystemStatusUtility;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.workflow.scheduled.update.model.UpdateModel;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class UpdateCheckerTest {
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
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsNewerSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, null, "0.1.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsNewerSnapshotPatch(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0.1-" + versionSuffix, null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsNewerButCloseSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, -20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsOlderButCloseSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsOlderSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsOlderSnapshotDockerPatch(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0.1", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsNewerBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, null, "0.1.0-" + versionSuffix, null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsNewerDateBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, -80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsNewerButCloseBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, -20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsOlderButCloseBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsOlderBothSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-" + versionSuffix, formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsOlderButCloseDockerSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-" + versionSuffix, formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SNAPSHOT", "SIGQA1" })
    public void testAlertIsOlderDockerSnapshot(String versionSuffix) {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

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
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);

        DefaultSystemStatusUtility defaultSystemStatusUtility = Mockito.mock(DefaultSystemStatusUtility.class);
        Mockito.when(defaultSystemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        Mockito.when(defaultSystemStatusUtility.getStartupTime()).thenReturn(new Date());

        AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertTrustCertificate()).thenReturn(Optional.of(Boolean.TRUE));

        AboutReader reader = new AboutReader(gson, defaultSystemStatusUtility);
        UpdateChecker updateChecker = new UpdateChecker(gson, reader, proxyManager, alertProperties);

        UpdateModel updateModel = updateChecker.getUpdateModel();

        assertNotNull(updateModel);
    }

    private UpdateChecker getEmptyUpdateChecker() {
        return new UpdateChecker(null, null, null, null);
    }

}
