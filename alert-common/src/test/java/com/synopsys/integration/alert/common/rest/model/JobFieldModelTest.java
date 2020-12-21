package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class JobFieldModelTest {
    @Test
    public void jobFieldModelTest() {
        JobFieldModel testJobFieldModel = new JobFieldModel();
        assertNull(testJobFieldModel.getJobId());
        assertNull(testJobFieldModel.getFieldModels());
    }

    @Test
    public void jobIdTest() {
        String testJobId = "test-job-id";
        String newJobId = "new-job-id";
        FieldModel testFieldModel = Mockito.mock(FieldModel.class);

        Set<FieldModel> fieldModels = Set.of(testFieldModel);
        JobFieldModel testJobFieldModel = new JobFieldModel(testJobId, fieldModels, List.of());
        testJobFieldModel.setJobId(newJobId);

        assertEquals(newJobId, testJobFieldModel.getJobId());
    }

    @Test
    public void fieldModelsTest() {
        String testJobId = "test-job-id";
        FieldModel testFieldModel = Mockito.mock(FieldModel.class);
        FieldModel newTestFieldModel = Mockito.mock(FieldModel.class);

        Set<FieldModel> fieldModels = Set.of(testFieldModel);
        Set<FieldModel> newFieldModels = Set.of(testFieldModel, newTestFieldModel);
        JobFieldModel testJobFieldModel = new JobFieldModel(testJobId, fieldModels, List.of());
        testJobFieldModel.setFieldModels(newFieldModels);

        assertEquals(newFieldModels, testJobFieldModel.getFieldModels());
        assertEquals(2, testJobFieldModel.getFieldModels().size());
    }

}
