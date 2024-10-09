package com.blackduck.integration.alert.test.common.junit.docker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import com.blackduck.integration.alert.test.common.junit.ExecutableExistsUtility;
import com.blackduck.integration.executable.Executable;

public class DockerInstalledCondition implements ExecutionCondition {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        Path presentWorkingDirectory = Paths.get("").toAbsolutePath();
        Executable dockerExecutable = Executable.create(presentWorkingDirectory.toFile(), List.of("docker", "version"));
        return ExecutableExistsUtility.checkIfExecutableExists(logger, dockerExecutable);
    }
}
