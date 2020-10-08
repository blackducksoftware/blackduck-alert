package com.synopsys.integration.alert.performance.utility;

import java.nio.charset.Charset;

import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class IntegrationAlertRequestUtility implements AlertRequestUtility {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private final IntLogger intLogger;
    private MockMvc mockMvc;

    public IntegrationAlertRequestUtility(IntLogger intLogger, MockMvc mockMvc) {
        this.intLogger = intLogger;
        this.mockMvc = mockMvc;
    }

    @Override
    public String executeGetRequest(String path, String error) throws IntegrationException {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(path);
        return executeRequest(requestBuilder, error);
    }

    @Override
    public String executePostRequest(String path, String requestBody, String error) throws IntegrationException {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path)
                                                           .content(requestBody)
                                                           .contentType(contentType);
        return executeRequest(requestBuilder, error);
    }

    @Override
    public String executePutRequest(String path, String requestBody, String error) throws IntegrationException {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put(path)
                                                           .content(requestBody)
                                                           .contentType(contentType);
        return executeRequest(requestBuilder, error);
    }

    private String executeRequest(MockHttpServletRequestBuilder requestBuilder, String error) throws IntegrationException {
        MockHttpServletRequestBuilder request = requestBuilder
                                                    .with(
                                                        SecurityMockMvcRequestPostProcessors
                                                            .user("admin")
                                                            .roles(AlertIntegrationTest.ROLE_ALERT_ADMIN)
                                                    )
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        try {
            MvcResult mvcResult = mockMvc.perform(request).andReturn();
            String responseContent = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            if (status >= RestConstants.BAD_REQUEST_400) {
                intLogger.error(String.format("Error code: %s", status));
                intLogger.error(String.format("Response: %s", responseContent));
                intLogger.error(error);
                throw new IntegrationRestException(status, null, responseContent, error);
            }
            return responseContent;
        } catch (IntegrationRestException e) {
            throw e;
        } catch (Exception e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }
}
