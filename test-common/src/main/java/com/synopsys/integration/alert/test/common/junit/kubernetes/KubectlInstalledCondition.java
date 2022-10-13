package com.synopsys.integration.alert.test.common.junit.kubernetes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import com.synopsys.integration.alert.test.common.junit.ExecutableExistsUtility;
import com.synopsys.integration.executable.Executable;

public class KubectlInstalledCondition implements ExecutionCondition {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        Path presentWorkingDirectory = Paths.get("").toAbsolutePath();
        Executable executable = Executable.create(presentWorkingDirectory.toFile(), List.of("kubectl", "version"));
        return ExecutableExistsUtility.checkIfExecutableExists(logger, executable);
    }
}
