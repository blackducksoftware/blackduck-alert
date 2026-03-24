package com.blackduck.integration.alert.api.channel.jira;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.google.gson.Gson;

class JiraTaskTest {
    private static final String CONFIG_ID = UUID.randomUUID().toString();
    private static final String CONFIG_NAME = "Test Task Config";
    private static final String TASK_NAME_SUFFIX = "Task name suffix";
    private static final Gson GSON = new Gson();

    @Test
    void testExecuteTask() {
        TestTask task = new TestTask(null, null, CONFIG_ID, CONFIG_NAME, TASK_NAME_SUFFIX, GSON, 0,0);
        task.runTask();

        Assertions.assertEquals(CONFIG_ID, task.getConfigId());
        Assertions.assertEquals(CONFIG_NAME, task.getConfigName());
        Assertions.assertTrue(task.getTaskName().contains(task.getClass().getSimpleName()));
        Assertions.assertTrue(task.getTaskName().contains(TASK_NAME_SUFFIX));
        Assertions.assertTrue(task.getTaskName().contains(CONFIG_ID));
        Assertions.assertEquals(0, task.getConsecutiveFailures());
    }

    @Test
    void testExecuteConsecutiveFailures() {
        int expectedConsecutiveFailures = 10;
        TestTask task = new TestTask(null, null, CONFIG_ID, CONFIG_NAME, TASK_NAME_SUFFIX, GSON, expectedConsecutiveFailures, expectedConsecutiveFailures+1);
        for(int index = 0; index < expectedConsecutiveFailures; index++) {
            task.runTask();
        }
        Assertions.assertEquals(expectedConsecutiveFailures, task.getConsecutiveFailures());
    }

    @Test
    void testIncrementFailures() {
        TestTask task = new TestTask(null, null, CONFIG_ID, CONFIG_NAME, TASK_NAME_SUFFIX, GSON, 1,2);
        task.runTask();
        Assertions.assertEquals(1, task.getConsecutiveFailures());
    }

    @Test
    void testFailureReset() {
        TestTask task = new TestTask(null, null, CONFIG_ID, CONFIG_NAME, TASK_NAME_SUFFIX, GSON, 1,1);
        task.runTask();
        Assertions.assertEquals(1, task.getConsecutiveFailures());
        task.runTask();
        Assertions.assertEquals(0, task.getConsecutiveFailures());
    }

    public static class TestTask extends JiraTask {
        private final int expectedConsecutiveFailures;
        private final int failuresBeforeReset;
        public TestTask(
            final TaskScheduler taskScheduler,
            final TaskManager taskManager,
            final String configId,
            final String configName,
            final String taskNameSuffix,
            final Gson gson,
            int expectedConsecutiveFailures,
            int failuresBeforeReset
        ) {
            super(taskScheduler, taskManager, configId, configName, taskNameSuffix, gson);
            this.expectedConsecutiveFailures = expectedConsecutiveFailures;
            this.failuresBeforeReset = failuresBeforeReset;
        }

        @Override
        public String scheduleCronExpression() {
            return "";
        }

        @Override
        public void runTask() {
            if(getConsecutiveFailures() <= expectedConsecutiveFailures) {
                checkThresholdAndIncrementFailures();
            }

            if(getConsecutiveFailures() > failuresBeforeReset) {
                resetConsecutiveFailures();
            }
        }
    }
}
