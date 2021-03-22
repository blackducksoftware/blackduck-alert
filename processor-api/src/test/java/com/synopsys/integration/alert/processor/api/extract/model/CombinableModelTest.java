package com.synopsys.integration.alert.processor.api.extract.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.manual.enumeration.OperationType;

public class CombinableModelTest {
    private static final String KEY_1 = "key1";
    private static final String KEY_2 = "key2";

    @Test
    public void combineEqualModels() {
        List<CombinableTestModel> toCombine = List.of(
            new CombinableTestModel(KEY_1, OperationType.CREATE),
            new CombinableTestModel(KEY_1, OperationType.CREATE)
        );

        List<CombinableTestModel> combinedModels = CombinableModel.combine(toCombine);
        assertEquals(1, combinedModels.size());
    }

    @Test
    public void combinePositiveThenNegativeOperation() {
        List<CombinableTestModel> positiveNegative = List.of(
            new CombinableTestModel(KEY_1, OperationType.CREATE),
            new CombinableTestModel(KEY_1, OperationType.DELETE)
        );

        List<CombinableTestModel> combinedModels = CombinableModel.combine(positiveNegative);
        assertEquals(0, combinedModels.size());
    }

    @Test
    public void combineNegativeThenPositiveOperation() {
        List<CombinableTestModel> negativePositive = List.of(
            new CombinableTestModel(KEY_1, OperationType.DELETE),
            new CombinableTestModel(KEY_1, OperationType.CREATE)
        );

        List<CombinableTestModel> combinedModels = CombinableModel.combine(negativePositive);
        assertEquals(0, combinedModels.size());
    }

    @Test
    public void combineMixedModels() {
        List<CombinableTestModel> negativePositive = List.of(
            // Add key 1 - Total: 1
            new CombinableTestModel(KEY_1, OperationType.CREATE),
            // Delete key 2 - Total: 2
            new CombinableTestModel(KEY_2, OperationType.DELETE),
            // Remove key 1 - Total: 1
            new CombinableTestModel(KEY_1, OperationType.DELETE),
            // Update key 2 - Total: 2
            new CombinableTestModel(KEY_2, OperationType.UPDATE),
            // Add key 1 - Total: 3
            new CombinableTestModel(KEY_1, OperationType.CREATE),
            // Update key 1 - Total: 4
            new CombinableTestModel(KEY_1, OperationType.UPDATE),
            // Add key 2 - Total: 3
            new CombinableTestModel(KEY_2, OperationType.CREATE),
            // Update key 1 - Total: 3
            new CombinableTestModel(KEY_1, OperationType.UPDATE)
        );

        List<CombinableTestModel> combinedModels = CombinableModel.combine(negativePositive);
        assertEquals(3, combinedModels.size());
    }

    private static class CombinableTestModel implements CombinableModel<CombinableTestModel> {
        final String key;
        final OperationType operation;

        public CombinableTestModel(String key, OperationType operation) {
            this.key = key;
            this.operation = operation;
        }

        @Override
        public List<CombinableTestModel> combine(CombinableTestModel otherModel) {
            if (!key.equals(otherModel.key)) {
                return List.of(this, otherModel);
            }

            if (operation.equals(otherModel.operation)) {
                return List.of(this);
            }

            if (OperationType.CREATE.equals(operation) && OperationType.DELETE.equals(otherModel.operation)) {
                return List.of();
            } else if (OperationType.DELETE.equals(operation) && OperationType.CREATE.equals(otherModel.operation)) {
                return List.of();
            }
            return List.of(this, otherModel);
        }

    }

}
