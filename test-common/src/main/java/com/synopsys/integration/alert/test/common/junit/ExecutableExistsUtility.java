package com.synopsys.integration.alert.test.common.junit;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.platform.commons.logging.Logger;

import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.executable.ExecutableRunnerException;
import com.synopsys.integration.executable.ProcessBuilderRunner;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.SilentIntLogger;

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
