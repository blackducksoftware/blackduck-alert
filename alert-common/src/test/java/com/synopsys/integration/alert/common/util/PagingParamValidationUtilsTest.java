package com.synopsys.integration.alert.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;

public class PagingParamValidationUtilsTest {
    private static final Integer VALID_PAGE_NUMBER = 0;
    private static final Integer INVALID_PAGE_NUMBER = -1;
    private static final Integer VALID_PAGE_SIZE = 10;
    private static final Integer INVALID_PAGE_SIZE = 0;

    @Test
    public void validParamsTest() {
        Optional<ActionResponse<Object>> result = PagingParamValidationUtils.createErrorActionResponseIfInvalid(VALID_PAGE_NUMBER, VALID_PAGE_SIZE);
        assertTrue(result.isEmpty(), "Expected the Optional ActionResponse to be empty");
    }

    @Test
    public void invalidPageNumberTest() {
        assertInvalidParams(INVALID_PAGE_NUMBER, VALID_PAGE_SIZE);
    }

    @Test
    public void invalidPageSizeTest() {
        assertInvalidParams(VALID_PAGE_NUMBER, INVALID_PAGE_SIZE);
    }

    @Test
    public void bothParamsInvalidTest() {
        assertInvalidParams(INVALID_PAGE_NUMBER, INVALID_PAGE_SIZE);
    }

    private void assertInvalidParams(Integer pageNumber, Integer pageSize) {
        Optional<ActionResponse<Object>> result = PagingParamValidationUtils.createErrorActionResponseIfInvalid(pageNumber, pageSize);
        assertTrue(result.isPresent(), "Expected the Optional ActionResponse to be present");
        ActionResponse<Object> actionResponse = result.get();
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

}
