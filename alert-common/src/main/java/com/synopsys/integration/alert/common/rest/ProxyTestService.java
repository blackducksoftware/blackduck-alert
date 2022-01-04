package com.synopsys.integration.alert.common.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

@Component
public class ProxyTestService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AlertProperties alertProperties;
    private final ProxyManager proxyManager;
    private final Gson gson;

    @Autowired
    public ProxyTestService(AlertProperties alertProperties, ProxyManager proxyManager, Gson gson) {
        this.alertProperties = alertProperties;
        this.proxyManager = proxyManager;
        this.gson = gson;
    }

    public void pingHost(String testUrl, SettingsProxyModel settingsProxyModel) throws IntegrationException {
        ProxyInfo proxyInfo = proxyManager.createProxyInfo(settingsProxyModel);
        IntHttpClient client = createIntHttpClient(proxyInfo);

        HttpUrl httpUrl = new HttpUrl(testUrl);
        Request testRequest = new Request.Builder(httpUrl).build();
        try (Response response = client.execute(testRequest)) {
            if (RestConstants.OK_200 >= response.getStatusCode() && response.getStatusCode() < RestConstants.MULT_CHOICE_300) {
                logger.info("Successfully pinged {}!", testUrl);
            } else {
                throw new AlertException(String.format("Could not ping: %s. Status Message: %s. Status code: %s", testUrl, response.getStatusMessage(), response.getStatusCode()));
            }
        } catch (Exception e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    private IntHttpClient createIntHttpClient(ProxyInfo proxyInfo) {
        Optional<Boolean> alertTrustCertificate = alertProperties.getAlertTrustCertificate();
        return new IntHttpClient(new Slf4jIntLogger(logger), gson, 5 * 60 * 1000, alertTrustCertificate.orElse(Boolean.FALSE), proxyInfo);
    }
}
