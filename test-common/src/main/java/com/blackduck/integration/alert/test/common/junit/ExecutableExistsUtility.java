/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.test.common.junit;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.platform.commons.logging.Logger;

import com.blackduck.integration.executable.Executable;
import com.blackduck.integration.executable.ExecutableOutput;
import com.blackduck.integration.executable.ExecutableRunnerException;
import com.blackduck.integration.executable.ProcessBuilderRunner;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.SilentIntLogger;

public class ExecutableExistsUtility {

    private ExecutableExistsUtility() {
    }

    public static ConditionEvaluationResult checkIfExecutableExists(Logger logger, Executable executable) {
        String executableDescription = executable.getExecutableDescription();
        IntLogger intLogger = new SilentIntLogger();
        ProcessBuilderRunner processBuilderRunner = new ProcessBuilderRunner(intLogger);
        logger.info(String.format("Checking if %s command exists...", executableDescription)::toString);
        ConditionEvaluationResult result;
        try {
            ExecutableOutput output = processBuilderRunner.execute(executable);
            logger.debug(String.format("%s command return code: %d", executableDescription, output.getReturnCode())::toString);
            if (0 == output.getReturnCode()) {
                String textFound = String.format("%s command found", executableDescription);
                logger.info(textFound::toString);
                result = ConditionEvaluationResult.enabled(textFound);
            } else {
                String textNotFound = String.format("%s command not found", executableDescription);
                logger.info(textNotFound::toString);
                result = ConditionEvaluationResult.disabled(textNotFound);
            }
        } catch (ExecutableRunnerException ex) {
            String message = "Error checking if docker command exists.";
            logger.error(ex, message::toString);
            result = ConditionEvaluationResult.disabled(String.format("%s Caused by: %s", message, ex.getMessage()));
        }
        return result;
    }
}
