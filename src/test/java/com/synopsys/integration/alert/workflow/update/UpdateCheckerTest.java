package com.synopsys.integration.alert.workflow.update;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.AboutReader;
import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.database.api.SystemStatusUtility;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.workflow.scheduled.update.UpdateChecker;
import com.synopsys.integration.alert.workflow.scheduled.update.model.DockerTagModel;
import com.synopsys.integration.alert.workflow.scheduled.update.model.UpdateModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class UpdateCheckerTest {
    private final Gson gson = new Gson();
    private final SimpleDateFormat formatter = new SimpleDateFormat(UpdateChecker.DATE_FORMAT);

    @Test
    public void testAlertIsNewer() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final DockerTagModel latestVersion = createDockerTagModel("0.1.0", null);
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, latestVersion, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerPatch() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final DockerTagModel latestVersion = createDockerTagModel("1.0.0", null);
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0.1", null, latestVersion, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlder() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final DockerTagModel latestVersion = createDockerTagModel("1.0.1", null);
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, latestVersion, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderDockerPatch() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final DockerTagModel latestVersion = createDockerTagModel("1.0.0.1", null);
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", null, latestVersion, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final DockerTagModel latestVersion = createDockerTagModel("0.1.0", null);
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", null, latestVersion, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerSnapshotPatch() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();
        final DockerTagModel latestVersion = createDockerTagModel("1.0.0", null);
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0.1-SNAPSHOT", null, latestVersion, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerButCloseSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, -20);

        final DockerTagModel latestVersion = createDockerTagModel("1.0.0", formatter.format(dockerTagDate));
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatter.format(alertTime), latestVersion, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderButCloseSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        final DockerTagModel latestVersion = createDockerTagModel("1.0.0", formatter.format(dockerTagDate));
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatter.format(alertTime), latestVersion, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        final DockerTagModel latestVersion = createDockerTagModel("1.0.0", formatter.format(dockerTagDate));
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatter.format(alertTime), latestVersion, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderSnapshotDockerPatch() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        final DockerTagModel latestVersion = createDockerTagModel("1.0.0.1", formatter.format(dockerTagDate));
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatter.format(alertTime), latestVersion, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerBothSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final DockerTagModel latestVersion = createDockerTagModel("0.1.0-SNAPSHOT", null);
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", null, latestVersion, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerDateBothSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, -80);

        final DockerTagModel latestVersion = createDockerTagModel("1.0.0-SNAPSHOT", formatter.format(dockerTagDate));
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatter.format(alertTime), latestVersion, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsNewerButCloseBothSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, -20);

        final DockerTagModel latestVersion = createDockerTagModel("1.0.0-SNAPSHOT", formatter.format(dockerTagDate));
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatter.format(alertTime), latestVersion, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderButCloseBothSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        final DockerTagModel latestVersion = createDockerTagModel("1.0.0-SNAPSHOT", formatter.format(dockerTagDate));
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatter.format(alertTime), latestVersion, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderBothSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        final DockerTagModel latestVersion = createDockerTagModel("1.0.0-SNAPSHOT", formatter.format(dockerTagDate));
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0-SNAPSHOT", formatter.format(alertTime), latestVersion, null);

        assertTrue(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderButCloseDockerSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 20);

        final DockerTagModel latestVersion = createDockerTagModel("1.0.0-SNAPSHOT", formatter.format(dockerTagDate));
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", formatter.format(alertTime), latestVersion, null);

        assertFalse(updateModel.getUpdatable());
    }

    @Test
    public void testAlertIsOlderDockerSnapshot() throws IntegrationException {
        final UpdateChecker updateChecker = getEmptyUpdateChecker();

        final Date alertTime = new Date();
        final Date dockerTagDate = DateUtils.addMinutes(alertTime, 80);

        final DockerTagModel latestVersion = createDockerTagModel("1.0.0-SNAPSHOT", formatter.format(dockerTagDate));
        final UpdateModel updateModel = updateChecker.getUpdateModel("1.0.0", formatter.format(alertTime), latestVersion, null);

        assertTrue(updateModel.getUpdatable());
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

        final AboutReader reader = new AboutReader(gson, systemStatusUtility);
        final UpdateChecker updateChecker = new UpdateChecker(gson, reader, proxyManager);

        final UpdateModel updateModel = updateChecker.getUpdateModel();

        assertNotNull(updateModel);
    }

    private UpdateChecker getEmptyUpdateChecker() {
        return new UpdateChecker(null, null, null);
    }

    private DockerTagModel createDockerTagModel(final String version, final String updated) {
        return new DockerTagModel(version, null, null, null, null, null, null, updated, true);
    }
}
