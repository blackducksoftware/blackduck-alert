/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.scripts;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;

import com.blackduck.integration.alert.test.common.TestResourceUtils;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.alert.test.common.junit.kubernetes.EnableIfKubeAndHelmPresent;
import com.blackduck.integration.executable.Executable;
import com.blackduck.integration.executable.ExecutableOutput;
import com.blackduck.integration.executable.ExecutableRunnerException;
import com.blackduck.integration.executable.ProcessBuilderRunner;
import com.blackduck.integration.log.BufferedIntLogger;
import com.blackduck.integration.log.LogLevel;

@EnableIfKubeAndHelmPresent
@Tag(TestTags.DEFAULT_INTEGRATION)
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
        workingDirectory.deleteOnExit();

        podSearchCommand = createSearchCommand(workingDirectory, podName);
        Executable createPodCommand = createPodCommand(workingDirectory, podName);
        testLogger = new BufferedIntLogger();
        processBuilderRunner = new ProcessBuilderRunner(testLogger);

        ExecutableOutput searchOutput = processBuilderRunner.execute(podSearchCommand);
        if (StringUtils.isBlank(searchOutput.getStandardOutput())) {
            processBuilderRunner.execute(createPodCommand);
            searchOutput = processBuilderRunner.execute(podSearchCommand);
            Executable copySqlCommand = createCopySqlDataCommand(workingDirectory, searchOutput.getStandardOutput());
            Executable copyLoadScriptCommand = createCopyLoadDataScriptCommand(workingDirectory, searchOutput.getStandardOutput());
            Executable loadDataCommand = createLoadDataCommand(workingDirectory, searchOutput.getStandardOutput());
            ExecutableOutput copyOutput = processBuilderRunner.execute(copySqlCommand);
            ExecutableOutput copyLoadScriptOutput = processBuilderRunner.execute(copyLoadScriptCommand);

            if (copyOutput.getReturnCode() > 0) {
                fail("Couldn't copy database dump to pod.");
            }

            if (copyLoadScriptOutput.getReturnCode() > 0) {
                fail("Couldn't copy load data script to pod.");
            }

            ExecutableOutput loadOutput = processBuilderRunner.execute(loadDataCommand);
            if (loadOutput.getReturnCode() > 0) {
                fail("Couldn't load database dump file");
            }
        }
    }

    private Executable createSearchCommand(File workingDirectory, String searchName) throws IOException {
        Path podSearchScript = findFilePath("kubernetes-pod-search.sh");
        List<String> commandLine = List.of(podSearchScript.toFile().getAbsolutePath(), TEST_NAMESPACE, searchName);
        return Executable.create(workingDirectory, commandLine);
    }

    private Executable createPodCommand(File workingDirectory, String name) throws IOException {
        Path initScriptPath = findFilePath("init-helm-db.sh");
        Path helmChartPath = findFilePath("helm-test-postgres");
        List<String> commandLineArguments = List.of("-n", TEST_NAMESPACE, "-i", name, "-c", helmChartPath.toFile().getAbsolutePath());
        return Executable.create(workingDirectory, initScriptPath.toFile(), commandLineArguments);
    }

    private Executable createCopySqlDataCommand(File workingDirectory, String name) throws IOException {
        Path dumpFilePath = findFilePath("alert-example-db.sql");
        String copyDestinationArgument = String.format("%s/%s:/tmp/alert-example-db.sql", TEST_NAMESPACE, name);
        List<String> commandLine = List.of("kubectl", "cp", dumpFilePath.toFile().getAbsolutePath(), copyDestinationArgument);
        return Executable.create(workingDirectory, commandLine);
    }

    private Executable createCopyLoadDataScriptCommand(File workingDirectory, String name) throws IOException {
        Path dumpFilePath = findFilePath("load-example-db.sh");
        String copyDestinationArgument = String.format("%s/%s:/tmp/load-example-db.sh", TEST_NAMESPACE, name);
        List<String> commandLine = List.of("kubectl", "cp", dumpFilePath.toFile().getAbsolutePath(), copyDestinationArgument);
        return Executable.create(workingDirectory, commandLine);
    }

    private Executable createLoadDataCommand(File workingDirectory, String name) {
        List<String> commandLine = List.of("kubectl", "-n", TEST_NAMESPACE, "exec", "-i", name, "--", "bash", "-c", "/tmp/load-example-db.sh");
        return Executable.create(workingDirectory, commandLine);
    }

    @AfterEach
    public void cleanupTest() throws ExecutableRunnerException {
        logger.info("Command line execution logs: "::toString);
        logger.info(() -> testLogger.getOutputString(LogLevel.INFO));
        Executable cleanupCommand = createCleanupCommand(workingDirectory);
        Executable cleanupNamespaceCommand = createCleanupNamespaceCommand(workingDirectory);
        processBuilderRunner.execute(cleanupCommand);
        processBuilderRunner.execute(cleanupNamespaceCommand);
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

    @ParameterizedTest
    @ValueSource(strings = { "binary", "BINARY", "plain", "PLAIN" })
    void testBackupAndRestoreWithFormat(String formatType) throws ExecutableRunnerException {
        File backupFile = makeTempFile();
        List<String> backupArguments = List.of("-b", "-n", TEST_NAMESPACE, "-k", podName, "-t", formatType, "-f", backupFile.getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(0, restoreOutput.getReturnCode());

        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", podName, "-t", formatType, "-f", backupFile.getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { "binary", "BINARY", "plain", "PLAIN" })
    void testBackupAndRestoreWithoutPodName(String formatType) throws ExecutableRunnerException {
        File backupFile = makeTempFile();
        List<String> backupArguments = List.of("-b", "-n", TEST_NAMESPACE, "-t", formatType, "-f", backupFile.getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(0, restoreOutput.getReturnCode());

        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", podName, "-t", formatType, "-f", backupFile.getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Binary", "bINARY", "Plain", "pLAIN", "bad", "unknown", "", "  ", "''", "'    '" })
    void testBackupAndRestoreInvalidFormat(String formatType) throws ExecutableRunnerException {
        File backupFile = makeTempFile();
        List<String> backupArguments = List.of("-b", "-n", TEST_NAMESPACE, "-k", podName, "-t", formatType, "-f", backupFile.getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(1, restoreOutput.getReturnCode());

        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", podName, "-t", formatType, "-f", backupFile.getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(1, backupOutput.getReturnCode());
    }

    @Test
    void testDefaultBackupAndRestore() throws ExecutableRunnerException {
        File backupFile = makeTempFile();
        List<String> backupArguments = List.of("-b", "-n", TEST_NAMESPACE, "-k", podName, "-f", backupFile.getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(0, restoreOutput.getReturnCode());

        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", podName, "-f", backupFile.getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());
    }

    @Test
    void testPlainTextBackupAndRestore() throws ExecutableRunnerException {
        File backupFile = makeTempFile();
        List<String> backupArguments = List.of("-b", "-p", "-n", TEST_NAMESPACE, "-k", podName, "-f", backupFile.getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(0, restoreOutput.getReturnCode());

        List<String> restoreArguments = List.of("-r", "-p", "-n", TEST_NAMESPACE, "-k", podName, "-f", backupFile.getAbsolutePath());
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
        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", "invalid-container-name", "-f", dumpFilePath.toFile().getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(1, backupOutput.getReturnCode());
    }

    @Test
    void testDatabaseNameValid() throws ExecutableRunnerException {
        File backupFile = makeTempFile();
        List<String> backupArguments = List.of("-b", "-n", TEST_NAMESPACE, "-k", podName, "-d", "alertdb", "-f", backupFile.getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());

        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", podName, "-d", "alertdb", "-f", backupFile.getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(0, restoreOutput.getReturnCode());
    }

    @Test
    void testDatabaseNameInvalid() throws ExecutableRunnerException {
        File backupFile = makeTempFile();
        List<String> backupArguments = List.of("-b", "-n", TEST_NAMESPACE, "-k", podName, "-d", "invalid-database-name", "-f", backupFile.getAbsolutePath());
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(1, backupOutput.getReturnCode());

        backupFile = makeTempFile();
        backupArguments = List.of("-b", "-n", TEST_NAMESPACE, "-k", podName, "-d", "alertdb", "-f", backupFile.getAbsolutePath());
        scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, backupArguments);
        backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());

        List<String> restoreArguments = List.of("-r", "-n", TEST_NAMESPACE, "-k", podName, "-d", "invalid-database-name", "-f", backupFile.getAbsolutePath());
        Executable scriptRestoreExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput restoreOutput = processBuilderRunner.execute(scriptRestoreExecutable);
        assertEquals(2, restoreOutput.getReturnCode());
    }

    @Test
    void testHelpOption() throws ExecutableRunnerException {
        List<String> restoreArguments = List.of("-h");
        Executable scriptBackupExecutable = Executable.create(workingDirectory, scriptFile, restoreArguments);
        ExecutableOutput backupOutput = processBuilderRunner.execute(scriptBackupExecutable);
        assertEquals(0, backupOutput.getReturnCode());
    }

    @Test
    void testHelpOptionWithOtherParameters() throws ExecutableRunnerException {
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

    private File makeTempFile() {
        File backupFile = assertDoesNotThrow(() -> Files.createTempFile("testAlertBackup", "dump").toFile());
        backupFile.deleteOnExit();
        return backupFile;
    }
}
