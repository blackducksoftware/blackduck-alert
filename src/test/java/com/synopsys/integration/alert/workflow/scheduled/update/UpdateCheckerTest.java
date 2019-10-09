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
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.AboutReader;
import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.database.api.SystemStatusUtility;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.workflow.scheduled.update.model.UpdateModel;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class UpdateCheckerTest {
    private final Gson gson = new Gson();

    @Test
    public void testAlertIsNewer() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "0.1.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerPatch() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0.1", null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlder() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "1.0.1", null, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsSame() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderDockerPatch() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, "1.0.0.1", null, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", null, "0.1.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerSnapshotPatch() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0.1-SNAPSHOT", null, "1.0.0", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerButCloseSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, -20);

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderButCloseSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderSnapshotDockerPatch() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0.1", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerBothSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", null, "0.1.0-SNAPSHOT", null, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerDateBothSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, -80);

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerButCloseBothSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, -20);

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderButCloseBothSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderBothSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderButCloseDockerSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderDockerSnapshot() {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", formatDate(alertTime, DOCKER_DATE_FORMAT), "1.0.0-SNAPSHOT", formatDate(dockerTagDate, DOCKER_DATE_FORMAT), null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    @Tags({
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    public void getUpdateModelTest() {
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);

        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        Mockito.when(systemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        Mockito.when(systemStatusUtility.getStartupTime()).thenReturn(new Date());

        AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertTrustCertificate()).thenReturn(Optional.of(Boolean.TRUE));

        final AboutReader reader = new AboutReader(gson, systemStatusUtility);
        final UpdateChecker updateChecker = new UpdateChecker(gson, reader, proxyManager, alertProperties);

        final UpdateModel updateModel = updateChecker.getUpdateModel();

        assertNotNull(updateModel);
    }

    private UpdateChecker getEmptyUpdateChecker() {
        return new UpdateChecker(null, null, null, null);
    }

}
