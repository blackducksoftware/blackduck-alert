package com.synopsys.integration.alert.web.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ssl")
public class EnabledSSLValidator implements SSLValidator {

    @Override
    public boolean isSSLEnabled() {
        return true;
    }
}
