package com.synopsys.integration.alert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

@Tag(TestTags.DEFAULT_PERFORMANCE)
public class EndToEndTest {
    private final IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
    private final String BLACKDUCK_PROVIDER_KEY = new BlackDuckProviderKey().getUniversalKey();
    private final String blackDuckProvider = System.getenv("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_URL");
    private final String blackDuckApiToken = System.getenv("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_API_KEY");
    private final String blackDuckTimeout = System.getenv("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_TIMEOUT");
    private final String blackDuckProviderName = blackDuckProvider + UUID.randomUUID();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private String cookie = null;
    private String csrfToken = null;

    @Test
    @Ignore
    public void testEndToEnd() throws Exception {
        IntHttpClient client = new IntHttpClient(intLogger, 60, true, ProxyInfo.NO_PROXY_INFO);
        loginToAlert(client);
        setupBlackDuck(client);
    }

    private void loginToAlert(IntHttpClient client) throws Exception {
        Request.Builder requestBuilder = createRequestBuilder("api/login");
        String loginBody = "{\"alertUsername\":\"sysadmin\",\"alertPassword\":\"blackduck\"}";
        BodyContent bodyContent = new StringBodyContent(loginBody);
        requestBuilder.bodyContent(bodyContent);
        requestBuilder.method(HttpMethod.POST);
        Request request = requestBuilder.build();
        Response response = client.execute(request);
        if (response.isStatusCodeError()) {
            intLogger.error("Could not log into Alert.");
            response.throwExceptionForError();
        }
        csrfToken = response.getHeaderValue("X-CSRF-TOKEN");
        cookie = response.getHeaderValue("Set-Cookie");
        intLogger.info("Logged into Alert.");
    }

    private void setupBlackDuck(IntHttpClient client) throws Exception {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put("provider.common.config.enabled", new FieldValueModel(List.of("true"), true));
        keyToValues.put("provider.common.config.name", new FieldValueModel(List.of(blackDuckProviderName), true));
        keyToValues.put("blackduck.url", new FieldValueModel(List.of(blackDuckProvider), true));
        keyToValues.put("blackduck.api.key", new FieldValueModel(List.of(blackDuckApiToken), true));
        keyToValues.put("blackduck.timeout", new FieldValueModel(List.of(blackDuckTimeout), true));
        FieldModel blackDuckProviderConfiguration = new FieldModel(BLACKDUCK_PROVIDER_KEY, "GLOBAL", keyToValues);

        String blackDuckConfigBody = gson.toJson(blackDuckProviderConfiguration);
        BodyContent bodyContent = new StringBodyContent(blackDuckConfigBody);
        Request.Builder validationRequestBuilder = createRequestBuilder("api/configuration/validate");
        validationRequestBuilder.method(HttpMethod.POST);
        validationRequestBuilder.bodyContent(bodyContent);
        Request validationRequest = validationRequestBuilder.build();
        Response validationResponse = client.execute(validationRequest);
        if (validationResponse.isStatusCodeError()) {
            intLogger.error("Could not create Black Duck Provider.");
            validationResponse.throwExceptionForError();
        }
        intLogger.info("Validated the Black Duck Configuration.");

        Request.Builder creationRequestBuilder = createRequestBuilder("api/configuration");
        creationRequestBuilder.method(HttpMethod.POST);
        creationRequestBuilder.bodyContent(bodyContent);
        Request creationRequest = creationRequestBuilder.build();
        Response creationResponse = client.execute(creationRequest);
        if (creationResponse.isStatusCodeError()) {
            intLogger.error("Could not create Black Duck Provider.");
            creationResponse.throwExceptionForError();
        }
        intLogger.info("Created the Black Duck Configuration.");
    }

    private Request.Builder createRequestBuilder(String path) throws IntegrationException {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(new HttpUrl("https://localhost:8443/alert/" + path));
        if (StringUtils.isNotBlank(csrfToken)) {
            requestBuilder.addHeader("X-CSRF-TOKEN", csrfToken);
        }
        if (StringUtils.isNotBlank(cookie)) {
            requestBuilder.addHeader("Cookie", cookie);
        }
        return requestBuilder;
    }

}
