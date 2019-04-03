package com.synopsys.integration.alert.web.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("no_ssl")
public class NoSSLValidator implements SSLValidator {

    @Override
    public boolean isSSLEnabled() {
        return false;
    }
}
