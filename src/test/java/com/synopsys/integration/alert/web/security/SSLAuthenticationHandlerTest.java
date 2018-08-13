package com.synopsys.integration.alert.web.security;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

public class SSLAuthenticationHandlerTest {

    class PublicSSLAuthenticationHandler extends SSLAuthenticationHandler {

        public PublicSSLAuthenticationHandler() {
            super(new HttpSessionCsrfTokenRepository());
        }

        public void callConfigure(final HttpSecurity http) throws Exception {
            configure(http);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConfigure() {
        final PublicSSLAuthenticationHandler testHandler = new PublicSSLAuthenticationHandler();
        final ObjectPostProcessor<Object> objectProcessor = Mockito.mock(ObjectPostProcessor.class);
        final AuthenticationManagerBuilder builder = Mockito.mock(AuthenticationManagerBuilder.class);
        try {
            testHandler.callConfigure(new HttpSecurity(objectProcessor, builder, new HashMap<Class<?>, Object>()));
        } catch (final Exception e) {
            Assert.fail();
            e.printStackTrace();
        }

    }
}
