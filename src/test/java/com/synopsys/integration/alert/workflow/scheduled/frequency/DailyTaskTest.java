package com.synopsys.integration.alert.workflow.scheduled.frequency;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;

public class DailyTaskTest {

    @Test
    public void testDigestType() {
        final DailyTask task = new DailyTask(null, null, null, null);
        assertEquals(FrequencyType.DAILY, task.getDigestType());
    }

    @Test
    public void testGetTaskName() {
        final DailyTask task = new DailyTask(null, null, null, null);
        assertEquals(DailyTask.TASK_NAME, task.getTaskName());
    }
}
