package com.synopsys.integration.alert.scripts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        Executable copySqlCommand = createCopySqlDataCommand(workingDirectory, containerName);
        Executable copyLoadScriptCommand = createCopyLoadDataScriptCommand(workingDirectory, containerName);
        Executable loadDataCommand = createLoadDataCommand(workingDirectory, containerName);
        testLogger = new BufferedIntLogger();
        processBuilderRunner = new ProcessBuilderRunner(testLogger);

        ExecutableOutput searchOutput = processBuilderRunner.execute(dockerContainerSearchCommand);
        if (StringUtils.isBlank(searchOutput.getStandardOutput())) {
            processBuilderRunner.execute(dockerCreateContainerCommand);
            ExecutableOutput copyOutput = processBuilderRunner.execute(copySqlCommand);
            processBuilderRunner.execute(copyLoadScriptCommand);

            if (copyOutput.getReturnCode() > 0) {
                fail("Couldn't copy database dump to container.");
            }

            ExecutableOutput loadOutput = processBuilderRunner.execute(loadDataCommand);
            if (loadOutput.getReturnCode() > 0) {
                fail("Couldn't load database dump file");
            }
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

    private Executable createCopySqlDataCommand(File workingDirectory, String containerName) throws IOException {
        Path dumpFilePath = findFilePath("alert-example-db.sql");
        String copyDestinationArgument = String.format("%s:/tmp/alert-example-db.sql", containerName);
        List<String> commandLine = List.of("docker", "cp", dumpFilePath.toFile().getAbsolutePath(), copyDestinationArgument);
        return Executable.create(workingDirectory, commandLine);
    }

    private Executable createCopyLoadDataScriptCommand(File workingDirectory, String containerName) throws IOException {
        Path dumpFilePath = findFilePath("load-example-db.sh");
        String copyDestinationArgument = String.format("%s:/tmp/load-example-db.sh", containerName);
        List<String> commandLine = List.of("docker", "cp", dumpFilePath.toFile().getAbsolutePath(), copyDestinationArgument);
        return Executable.create(workingDirectory, commandLine);
    }

    private Executable createLoadDataCommand(File workingDirectory, String containerName) throws IOException {
        List<String> commandLine = List.of("docker", "exec", "-i", containerName, "bash", "-c", "/tmp/load-example-db.sh");
        return Executable.create(workingDirectory, commandLine);
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
        // perform backup
        File backupFile = Files.createTempFile("testAlertBackup", "dump").toFile();
        List<String> backupArguments = List.of("-b", "-k", containerName, "-f", backupFile.getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(0, restoreOutput.getReturnCode());

        List<String> restoreArguments = List.of("-r", "-k", containerName, "-f", backupFile.getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());

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
        File backupFile = Files.createTempFile("testAlertBackup", "dump").toFile();
        List<String> backupArguments = List.of("-b", "-k", containerName, "-d", "alertdb", "-f", backupFile.getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());

        List<String> restoreArguments = List.of("-r", "-k", containerName, "-d", "alertdb", "-f", backupFile.getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(0, restoreOutput.getReturnCode());
    }

    @Test
    void testDatabaseNameInvalid() throws IOException, ExecutableRunnerException {
        File backupFile = Files.createTempFile("testAlertBackup", "dump").toFile();
        List<String> backupArguments = List.of("-b", "-k", containerName, "-d", "invalid-database-name", "-f", backupFile.getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(1, backupOutput.getReturnCode());

        // create a valid backup
        backupFile = Files.createTempFile("testAlertBackup", "dump").toFile();
        backupArguments = List.of("-b", "-k", containerName, "-d", "alertdb", "-f", backupFile.getAbsolutePath());
        scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());

        List<String> restoreArguments = List.of("-r", "-k", containerName, "-d", "invalid-database-name", "-f", backupFile.getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(1, restoreOutput.getReturnCode());
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
