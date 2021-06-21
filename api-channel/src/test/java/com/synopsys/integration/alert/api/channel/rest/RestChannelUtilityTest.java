package com.synopsys.integration.alert.api.channel.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.body.BodyContentConverter;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class RestChannelUtilityTest {
    private static final String CLASS_NAME = RestChannelUtilityTest.class.getSimpleName();
    private static final Request TEST_REQUEST = Mockito.mock(Request.class);

    @Test
    public void sendMessageSuccessTest() throws IntegrationException {
        Response response = createMockResponse(HttpStatus.OK);
        IntHttpClient intHttpClient = createMockHttpClientWithResponse(response);

        RestChannelUtility restChannelUtility = new RestChannelUtility(intHttpClient);

        try {
            restChannelUtility.sendMessage(List.of(TEST_REQUEST, TEST_REQUEST), CLASS_NAME);
        } catch (AlertException e) {
            fail("Expected no exception to be thrown", e);
        }
    }

    @Test
    public void sendMessageAlertExceptionTest() throws IntegrationException {
        Response response = createMockResponse(HttpStatus.BAD_REQUEST);
        IntHttpClient intHttpClient = createMockHttpClientWithResponse(response);

        RestChannelUtility restChannelUtility = new RestChannelUtility(intHttpClient);

        try {
            restChannelUtility.sendMessage(List.of(TEST_REQUEST, TEST_REQUEST), CLASS_NAME);
            fail("Expected an exception to be thrown");
        } catch (AlertException e) {
            // Pass
        }
    }

    @Test
    public void sendMessageRuntimeExceptionTest() throws IntegrationException {
        IntHttpClient intHttpClient = Mockito.mock(IntHttpClient.class);
        Mockito.when(intHttpClient.execute(Mockito.any(Request.class))).thenThrow(new IllegalStateException("Something is wrong"));

        RestChannelUtility restChannelUtility = new RestChannelUtility(intHttpClient);

        try {
            restChannelUtility.sendMessage(List.of(TEST_REQUEST, TEST_REQUEST), CLASS_NAME);
            fail("Expected an exception to be thrown");
        } catch (AlertException e) {
            // Pass
        }
    }

    @Test
    public void createPostMessageRequestTest() {
        String testUrl = "https://a-url";
        String testJson = "{\"testField\": \"test value\"}";

        Map<String, String> testHeaders = new HashMap<>();
        String testHeaderKey = "Test Header";
        testHeaders.put(testHeaderKey, "header value");

        RestChannelUtility restChannelUtility = new RestChannelUtility(null);

        Request request = restChannelUtility.createPostMessageRequest(testUrl, testHeaders, testJson);
        assertEquals(testUrl, request.getUrl().string());
        assertEquals(HttpMethod.POST, request.getMethod());
        assertTrue(request.getHeaders().containsKey(testHeaderKey));
        assertTrue(request.getQueryParameters().isEmpty());
        assertReflectionEquals(new StringBodyContent(testJson, BodyContentConverter.DEFAULT), request.getBodyContent());
    }

    @Test
    public void createPostMessageRequestParamsTest() {
        String testParam1 = "param1";
        String testParam2 = "param2";
        String testParam3 = "param3";
        Set<String> testValue = Set.of("test value");
        HashMap<String, Set<String>> queryParams = new HashMap<>();
        queryParams.put(testParam1, testValue);
        queryParams.put(testParam2, testValue);
        queryParams.put(testParam3, testValue);

        RestChannelUtility restChannelUtility = new RestChannelUtility(null);

        Request request = restChannelUtility.createPostMessageRequest("https://a-url", new HashMap<>(), queryParams, "{}");
        Map<String, Set<String>> populatedQueryParameters = request.getPopulatedQueryParameters();
        assertMapContains(populatedQueryParameters, testParam1);
        assertMapContains(populatedQueryParameters, testParam2);
        assertMapContains(populatedQueryParameters, testParam3);
        assertEquals(testValue.size(), populatedQueryParameters.get(testParam1).size());
    }

    @Test
    public void createPostMessageRequestInvalidUrlTest() {
        RestChannelUtility restChannelUtility = new RestChannelUtility(null);
        try {
            restChannelUtility.createPostMessageRequest("invalid url", null, null);
            fail("Expected a runtime exception to be thrown");
        } catch (AlertRuntimeException e) {
            // Pass
        }
    }

    private static void assertReflectionEquals(BodyContent lhs, BodyContent rhs) {
        assertTrue(EqualsBuilder.reflectionEquals(lhs, rhs), "Expected the body content to match");
    }

    private static void assertMapContains(Map<String, ?> map, String key) {
        assertTrue(map.containsKey(key), "Expected query params to contain " + key);
    }

    private Response createMockResponse(HttpStatus httpStatus) {
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getStatusCode()).thenReturn(httpStatus.value());
        Mockito.when(response.getStatusMessage()).thenReturn(httpStatus.name());
        return response;
    }

    private IntHttpClient createMockHttpClientWithResponse(Response response) throws IntegrationException {
        IntHttpClient intHttpClient = Mockito.mock(IntHttpClient.class);
        Mockito.when(intHttpClient.execute(Mockito.any(Request.class))).thenReturn(response);
        return intHttpClient;
    }

}
