package com.synopsys.integration.alert.workflow.scheduled.frequency;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.synopsys.integration.alert.common.enumeration.DigestType;

public class OnDemandTaskTest {

    @Test
    public void testDigestType() {
        final OnDemandTask task = new OnDemandTask(null, null, null, null);
        assertEquals(DigestType.REAL_TIME, task.getDigestType());
    }

    @Test
    public void testGetTaskName() {
        final OnDemandTask task = new OnDemandTask(null, null, null, null);
        assertEquals(OnDemandTask.TASK_NAME, task.getTaskName());
    }
}
