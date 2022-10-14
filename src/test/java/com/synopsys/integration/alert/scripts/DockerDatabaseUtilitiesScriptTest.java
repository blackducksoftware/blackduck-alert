package com.synopsys.integration.alert.scripts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;

import com.synopsys.integration.alert.test.common.TestResourceUtils;
import com.synopsys.integration.alert.test.common.junit.docker.EnableIfDockerPresent;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.executable.ExecutableRunnerException;
import com.synopsys.integration.executable.ProcessBuilderRunner;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.LogLevel;

@EnableIfDockerPresent
class DockerDatabaseUtilitiesScriptTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String SCRIPT_NAME = "database-utilities.sh";
    //    private static final String POSTGRES_CONTAINER_NAME = String.format("alert-test-postgres-%s", UUID.randomUUID());
    //    private static final String CONTAINER_FILTER_BY_NAME = String.format("name=%s", POSTGRES_CONTAINER_NAME);
    private static final File scriptFile = new File(Paths.get("").toFile().getParentFile(), String.format("deployment/docker-swarm/%s", SCRIPT_NAME));

    private File workingDirectory;
    private Executable dockerContainerSearchCommand;
    private ProcessBuilderRunner processBuilderRunner;
    private BufferedIntLogger testLogger;
    private String containerName;

    private static String createContainerName() {
        return String.format("alert-test-postgres-%s", UUID.randomUUID());
    }

    private static String createContainerSearchFilter(String containerName) {
        return String.format("name=%s", containerName);
    }

    @BeforeEach
    public void initTest() throws IOException, ExecutableRunnerException {
        containerName = createContainerName();
        workingDirectory = File.createTempFile("alert-test", "txt").getParentFile();

        dockerContainerSearchCommand = createSearchCommand(workingDirectory, containerName);
        Executable dockerCreateContainerCommand = createContainerCommand(workingDirectory, containerName);
        testLogger = new BufferedIntLogger();
        processBuilderRunner = new ProcessBuilderRunner(testLogger);

        ExecutableOutput searchOutput = processBuilderRunner.execute(dockerContainerSearchCommand);
        if (StringUtils.isBlank(searchOutput.getStandardOutput())) {
            processBuilderRunner.execute(dockerCreateContainerCommand);
        }
    }

    private Executable createSearchCommand(File workingDirectory, String containerName) {
        List<String> commandLine = List.of("docker", "ps", "-q", "--filter", createContainerSearchFilter(containerName));
        return Executable.create(workingDirectory, commandLine);
    }

    private Executable createContainerCommand(File workingDirectory, String containerName) throws IOException {
        Path initScriptPath = findFilePath("init-docker-db.sh");
        List<String> commandLineArguments = List.of("-n", containerName);
        return Executable.create(workingDirectory, initScriptPath.toFile(), commandLineArguments);
    }

    @AfterEach
    public void cleanupTest() throws ExecutableRunnerException {
        logger.info("Command line execution logs: "::toString);
        logger.info(() -> testLogger.getOutputString(LogLevel.INFO));
        ExecutableOutput searchOutput = processBuilderRunner.execute(dockerContainerSearchCommand);
        if (searchOutput.getReturnCode() == 0 && !searchOutput.getStandardOutputAsList().isEmpty()) {
            String containerId = searchOutput.getStandardOutput();
            Executable dockerContainerStopCommand = createStopCommand(workingDirectory, containerId);
            Executable dockerContainerRemoveCommand = createCleanupCommand(workingDirectory, containerId);
            Executable dockerVolumeRemoveCommand = createCleanupVolumeCommand(workingDirectory, containerName);
            processBuilderRunner.execute(dockerContainerStopCommand);
            processBuilderRunner.execute(dockerContainerRemoveCommand);
            processBuilderRunner.execute(dockerVolumeRemoveCommand);
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

    private Executable createCleanupVolumeCommand(File workingDirectory, String containerName) {
        List<String> commandLine = List.of("docker", "volume", "rm", String.format("%s-volume", containerName));
        return Executable.create(workingDirectory, commandLine);
    }

    @Test
    void testScriptExecutionNoParameters() throws ExecutableRunnerException {
        Executable scriptExecutable = Executable.create(workingDirectory, scriptFile);
        ExecutableOutput output = processBuilderRunner.execute(scriptExecutable);
        assertEquals(0, output.getReturnCode());
        assertTrue(output.getStandardOutput().startsWith("usage:"));
    }

    @Test
    void testRestoreAndBackup() throws ExecutableRunnerException, IOException {
        Path dumpFilePath = findFilePath("alert-example-db.dump");
        List<String> restoreArguments = List.of("-r", "-k", containerName, "-f", dumpFilePath.toFile().getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());

        // perform restore
        List<String> backupArguments = List.of("-b", "-k", containerName, "-f", dumpFilePath.toFile().getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(0, restoreOutput.getReturnCode());
    }

    @Test
    void testFileDoesNotExist() throws ExecutableRunnerException {
        Executable scriptExecutable = Executable.create(workingDirectory, scriptFile, List.of("-f", "some/bad/file/example.dump"));
        ExecutableOutput output = processBuilderRunner.execute(scriptExecutable);
        assertEquals(1, output.getReturnCode());
    }

    @Test
    void testContainerKeywordInvalid() throws IOException, ExecutableRunnerException {
        Path dumpFilePath = findFilePath("alert-example-db.dump");
        List<String> restoreArguments = List.of("-r", "-k", "invalid-container-name", "-f", dumpFilePath.toFile().getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(1, backupOutput.getReturnCode());
    }

    @Test
    void testDatabaseNameValid() throws IOException, ExecutableRunnerException {
        Path dumpFilePath = findFilePath("alert-example-db.dump");
        List<String> restoreArguments = List.of("-r", "-k", containerName, "-d", "alertdb", "-f", dumpFilePath.toFile().getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());
    }

    @Test
    void testDatabaseNameInvalid() throws IOException, ExecutableRunnerException {
        Path dumpFilePath = findFilePath("alert-example-db.dump");
        List<String> restoreArguments = List.of("-r", "-k", containerName, "-d", "invalid-database-name", "-f", dumpFilePath.toFile().getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(1, backupOutput.getReturnCode());
    }

    @Test
    void testHelpOption() throws IOException, ExecutableRunnerException {
        List<String> restoreArguments = List.of("-h");
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());
    }

    @Test
    void testHelpOptionWithOtherParameters() throws IOException, ExecutableRunnerException {
        List<String> restoreArguments = List.of("-h", "-k", containerName, "-n", "db-name");
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());
    }

    private Path findFilePath(String fileName) throws IOException {
        File buildOutputDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalFile();
        File subProjectDir = TestResourceUtils.findAncestorDirectory(buildOutputDir, "src").orElse(new File("src"));
        return Path.of(subProjectDir.getAbsolutePath(), "test", "resources", "database", fileName);
    }

}
