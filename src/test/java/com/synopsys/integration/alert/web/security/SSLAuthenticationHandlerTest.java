package com.synopsys.integration.alert.web.security;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class SSLAuthenticationHandlerTest {

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

    class PublicSSLAuthenticationHandler extends SSLAuthenticationHandler {

        public PublicSSLAuthenticationHandler() {
            super(new HttpPathManager());
        }

        public void callConfigure(final HttpSecurity http) throws Exception {
            configure(http);
        }
    }
}
