package com.synopsys.integration.alert.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;

public class PagingParamValidationUtilsTest {
    private static final Integer VALID_PAGE_NUMBER = 0;
    private static final Integer INVALID_PAGE_NUMBER = -1;
    private static final Integer VALID_PAGE_SIZE = 10;
    private static final Integer INVALID_PAGE_SIZE = 0;

    private static List<Pair<Integer, Integer>> providePageNumberAndPageSizePairs() {
        return List.of(
            Pair.of(INVALID_PAGE_NUMBER, INVALID_PAGE_SIZE),
            Pair.of(VALID_PAGE_NUMBER, INVALID_PAGE_SIZE),
            Pair.of(INVALID_PAGE_NUMBER, INVALID_PAGE_SIZE)
        );
    }

    @Test
    public void validParamsTest() {
        Optional<ActionResponse<Object>> result = PagingParamValidationUtils.createErrorActionResponseIfInvalid(VALID_PAGE_NUMBER, VALID_PAGE_SIZE);
        assertTrue(result.isEmpty(), "Expected the Optional ActionResponse to be empty");
    }

    @ParameterizedTest
    @MethodSource("providePageNumberAndPageSizePairs")
    public void invalidPageNumberTest(Pair<Integer, Integer> pageNumberAndPageSize) {
        Optional<ActionResponse<Object>> result = PagingParamValidationUtils.createErrorActionResponseIfInvalid(pageNumberAndPageSize.getLeft(), pageNumberAndPageSize.getRight());
        assertTrue(result.isPresent(), "Expected the Optional ActionResponse to be present");
        ActionResponse<Object> actionResponse = result.get();
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

}
