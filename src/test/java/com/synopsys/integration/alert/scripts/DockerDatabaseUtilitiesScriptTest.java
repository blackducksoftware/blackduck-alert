package com.synopsys.integration.alert.scripts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import com.synopsys.integration.alert.test.common.junit.docker.EnableIfDockerPresent;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.executable.ExecutableRunnerException;
import com.synopsys.integration.executable.ProcessBuilderRunner;
import com.synopsys.integration.log.BufferedIntLogger;

@EnableIfDockerPresent
class DockerDatabaseUtilitiesScriptTest {
    private static final String SCRIPT_NAME = "database-utilities.sh";
    private static final String ALERT_CONTAINER_NAME = String.format("alert-test-postgres-%s", UUID.randomUUID());
    private static final String CONTAINER_FILTER_BY_NAME = String.format("name=%s", ALERT_CONTAINER_NAME);
    private final File scriptFile = new File(Paths.get("").toFile().getParentFile(), String.format("deployment/docker-swarm/%s", SCRIPT_NAME));

    private File workingDirectory;
    private Executable dockerContainerSearchCommand;
    private ProcessBuilderRunner processBuilderRunner;

    @BeforeEach
    public void initTest() throws IOException, ExecutableRunnerException {
        workingDirectory = File.createTempFile("alert-test", "txt").getParentFile();

        dockerContainerSearchCommand = createSearchCommand(workingDirectory);
        Executable dockerCreateContainerCommand = createContainerCommand(workingDirectory);

        processBuilderRunner = new ProcessBuilderRunner(new BufferedIntLogger());

        ExecutableOutput searchOutput = processBuilderRunner.execute(dockerContainerSearchCommand);
        if (StringUtils.isBlank(searchOutput.getStandardOutput())) {
            processBuilderRunner.execute(dockerCreateContainerCommand);
        }
    }

    private Executable createSearchCommand(File workingDirectory) {
        List<String> commandLine = List.of("docker", "ps", "-q", "--filter", CONTAINER_FILTER_BY_NAME);
        return Executable.create(workingDirectory, commandLine);
    }

    private Executable createContainerCommand(File workingDirectory) {

        List<String> commandLine = List.of("docker", "run", "-d", "--name", ALERT_CONTAINER_NAME, "-e", "POSTGRES_PASSWORD=blackduck", "-p", "5432:5432", "postgres:14.5-alpine");
        return Executable.create(workingDirectory, commandLine);
    }

    @AfterEach
    public void cleanupTest() throws ExecutableRunnerException {
        ExecutableOutput searchOutput = processBuilderRunner.execute(dockerContainerSearchCommand);
        if (searchOutput.getReturnCode() == 0 && !searchOutput.getStandardOutputAsList().isEmpty()) {
            String containerId = searchOutput.getStandardOutput();
            Executable dockerContainerStopCommand = createStopCommand(workingDirectory, containerId);
            Executable dockerContainerRemoveCommand = createCleanupCommand(workingDirectory, containerId);
            processBuilderRunner.execute(dockerContainerStopCommand);
            processBuilderRunner.execute(dockerContainerRemoveCommand);
        }
        workingDirectory.delete();
    }

    private Executable createStopCommand(File workingDirectory, String containerId) {
        List<String> commandLine = List.of("docker", "stop", containerId);
        return Executable.create(workingDirectory, commandLine);
    }

    private Executable createCleanupCommand(File workingDirectory, String containerId) {
        List<String> commandLine = List.of("docker", "rm", containerId);
        return Executable.create(workingDirectory, commandLine);
    }

    @Test
    void testBackupNoParameters() throws ExecutableRunnerException {
        Executable scriptExecutable = Executable.create(workingDirectory, scriptFile);
        ExecutableOutput output = processBuilderRunner.execute(scriptExecutable);
        assertEquals(0, output.getReturnCode());
        assertTrue(output.getStandardOutput().startsWith("usage:"));
    }

    @Test
    void testAnnotation() {}

}
