package com.synopsys.integration.alert.scripts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.synopsys.integration.alert.test.common.junit.kubernetes.EnableIfKubeAndHelmPresent;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.executable.ExecutableRunnerException;
import com.synopsys.integration.executable.ProcessBuilderRunner;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.LogLevel;

@EnableIfKubeAndHelmPresent
class HelmDatabaseUtilitiesScriptTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String SCRIPT_NAME = "database-utilities.sh";
    private static final String TEST_NAMESPACE = "alert-unit-test";
    private static final File scriptFile = new File(Paths.get("").toFile().getParentFile(), String.format("deployment/helm/%s", SCRIPT_NAME));

    private File workingDirectory;
    private Executable podSearchCommand;
    private ProcessBuilderRunner processBuilderRunner;
    private BufferedIntLogger testLogger;
    private String podName;

    private static String createPodName() {
        return String.format("test-postgres-%s", UUID.randomUUID());
    }

    @BeforeEach
    public void initTest() throws IOException, ExecutableRunnerException {
        podName = createPodName();
        workingDirectory = File.createTempFile("alert-test", "txt").getParentFile();

        podSearchCommand = createSearchCommand(workingDirectory, podName);
        Executable createPodCommand = createPodCommand(workingDirectory, podName);
        testLogger = new BufferedIntLogger();
        processBuilderRunner = new ProcessBuilderRunner(testLogger);

        ExecutableOutput searchOutput = processBuilderRunner.execute(podSearchCommand);
        if (StringUtils.isBlank(searchOutput.getStandardOutput())) {
            processBuilderRunner.execute(createPodCommand);
        }
    }

    private Executable createSearchCommand(File workingDirectory, String searchName) {
        List<String> commandLine = List.of("kubectl", "-n", TEST_NAMESPACE, "get", "pods", "|", "grep", searchName, "|", "awk", "'{print $1}'");
        return Executable.create(workingDirectory, commandLine);
    }

    private Executable createPodCommand(File workingDirectory, String name) throws IOException {
        Path initScriptPath = findFilePath("init-helm-db.sh");
        Path helmChartPath = findFilePath("helm-test-postgres");
        List<String> commandLineArguments = List.of("-n", TEST_NAMESPACE, "-i", name, "-c", helmChartPath.toFile().getAbsolutePath());
        return Executable.create(workingDirectory, initScriptPath.toFile(), commandLineArguments);
    }

    @AfterEach
    public void cleanupTest() throws ExecutableRunnerException {
        logger.info("Command line execution logs: "::toString);
        logger.info(() -> testLogger.getOutputString(LogLevel.INFO));
        Executable cleanupCommand = createCleanupCommand(workingDirectory);
        Executable cleanupNamespaceCommand = createCleanupNamespaceCommand(workingDirectory);
        processBuilderRunner.execute(cleanupCommand);
        processBuilderRunner.execute(cleanupNamespaceCommand);
        workingDirectory.delete();
    }

    private Executable createCleanupCommand(File workingDirectory) {
        List<String> commandLine = List.of("helm", "-n", TEST_NAMESPACE, "uninstall", podName);
        return Executable.create(workingDirectory, commandLine);
    }

    private Executable createCleanupNamespaceCommand(File workingDirectory) {
        List<String> commandLine = List.of("kubectl", "delete", "namespace", TEST_NAMESPACE);
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
        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", podName, "-f", dumpFilePath.toFile().getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());

        // perform restore
        File backupFile = Files.createTempFile("testAlertBackup", "dump").toFile();
        List<String> backupArguments = List.of("-b", "-n", TEST_NAMESPACE, "-k", podName, "-f", backupFile.getAbsolutePath());
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
        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", "invalid-container-name", "-f", dumpFilePath.toFile().getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(1, backupOutput.getReturnCode());
    }

    @Test
    void testDatabaseNameValid() throws IOException, ExecutableRunnerException {
        Path dumpFilePath = findFilePath("alert-example-db.dump");
        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", podName, "-d", "alertdb", "-f", dumpFilePath.toFile().getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());
    }

    @Test
    void testDatabaseNameInvalid() throws IOException, ExecutableRunnerException {
        Path dumpFilePath = findFilePath("alert-example-db.dump");
        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", podName, "-d", "invalid-database-name", "-f", dumpFilePath.toFile().getAbsolutePath());
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
        List<String> restoreArguments = List.of("-h", "-k", podName, "-n", "db-name");
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
