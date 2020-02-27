package com.synopsys.integration.alert.workflow.scheduled.update;

import static com.synopsys.integration.alert.common.util.DateUtils.DOCKER_DATE_FORMAT;
import static com.synopsys.integration.alert.common.util.DateUtils.formatDate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
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

    @Test
    public void testAlertIsNewerSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", null, "0.1.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerSnapshotPatch() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();
        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0.1-SNAPSHOT", null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerButCloseSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, -20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderButCloseSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderSnapshotDockerPatch() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0.1", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerBothSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", null, "0.1.0-SNAPSHOT", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerDateBothSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, -80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerButCloseBothSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, -20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderButCloseBothSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderBothSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderButCloseDockerSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderDockerSnapshot() {
        UpdateChecker updateChecker = getEmptyUpdateChecker();

        Date alertTime = new Date();
        Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    @Tags({
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    @Disabled
    // FIXME: test with the SIGQA versioning
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
