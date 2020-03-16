/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.security;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.activemq.broker.BrokerService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.EmptyKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.SingleLogoutProfile;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.web.security.authentication.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.web.security.authentication.event.AuthenticationEventManager;
import com.synopsys.integration.alert.web.security.authentication.saml.AlertFilterChainProxy;
import com.synopsys.integration.alert.web.security.authentication.saml.AlertSAMLEntryPoint;
import com.synopsys.integration.alert.web.security.authentication.saml.AlertSAMLMetadataGenerator;
import com.synopsys.integration.alert.web.security.authentication.saml.AlertSAMLMetadataGeneratorFilter;
import com.synopsys.integration.alert.web.security.authentication.saml.AlertWebSSOProfileOptions;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLAuthProvider;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLContext;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLManager;
import com.synopsys.integration.alert.web.security.authentication.saml.SamlAntMatcher;
import com.synopsys.integration.alert.web.security.authentication.saml.UserDetailsService;

@EnableWebSecurity
@Configuration
public class AuthenticationHandler extends WebSecurityConfigurerAdapter {
    public static final String SSO_PROVIDER_NAME = "Synopsys - Alert";
    private final HttpPathManager httpPathManager;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AlertProperties alertProperties;
    private final Logger logger = LoggerFactory.getLogger(AuthenticationHandler.class);
    private final FilePersistenceUtil filePersistenceUtil;
    private final UserManagementAuthoritiesPopulator authoritiesPopulator;
    private final ConfigurationAccessor configurationAccessor;
    private final AuthenticationDescriptorKey authenticationDescriptorKey;
    private final AuthenticationEventManager authenticationEventManager;
    private final AuthorizationUtility authorizationUtility;

    @Autowired
    AuthenticationHandler(ConfigurationAccessor configurationAccessor, HttpPathManager httpPathManager, CsrfTokenRepository csrfTokenRepository, AlertProperties alertProperties,
        FilePersistenceUtil filePersistenceUtil, UserManagementAuthoritiesPopulator authoritiesPopulator, AuthenticationDescriptorKey authenticationDescriptorKey,
        AuthenticationEventManager authenticationEventManager, AuthorizationUtility authorizationUtility) {
        this.configurationAccessor = configurationAccessor;
        this.httpPathManager = httpPathManager;
        this.csrfTokenRepository = csrfTokenRepository;
        this.alertProperties = alertProperties;
        this.filePersistenceUtil = filePersistenceUtil;
        this.authoritiesPopulator = authoritiesPopulator;
        this.authenticationDescriptorKey = authenticationDescriptorKey;
        this.authenticationEventManager = authenticationEventManager;
        this.authorizationUtility = authorizationUtility;
    }

    @Bean
    public static SAMLBootstrap SAMLBootstrap() {
        return new SAMLBootstrap();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(samlAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureActiveMQProvider();
        configureWithSSL(http);
        configureH2Console(http);
        http.authorizeRequests()
            .requestMatchers(createAllowedPathMatchers()).permitAll()
            .and().authorizeRequests().anyRequest().authenticated()
            .and().exceptionHandling().authenticationEntryPoint(samlEntryPoint())
            .and().csrf().csrfTokenRepository(csrfTokenRepository).ignoringRequestMatchers(createCsrfIgnoreMatchers())
            .and().addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
            .addFilterAfter(samlFilter(), BasicAuthenticationFilter.class)
            .authorizeRequests().withObjectPostProcessor(createRoleProcessor())
            .and().logout().logoutSuccessUrl("/");
    }

    private void configureActiveMQProvider() {
        // Active MQ initializes the Bouncy Castle provider in a static constructor of the Broker Service
        // static initialization of the Bouncy Castle provider breaks SAML support over SSL
        // https://stackoverflow.com/questions/53906154/spring-boot-2-1-embedded-tomcat-keystore-password-was-incorrect
        try {
            ClassLoader loader = BrokerService.class.getClassLoader();
            Class<?> clazz = loader.loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider");
            Provider bouncycastle = (Provider) clazz.getDeclaredConstructor().newInstance();
            Security.removeProvider(bouncycastle.getName());
            logger.info("Alert Application Configuration: Removing Bouncy Castle provider");
            Security.addProvider(bouncycastle);
            logger.info("Alert Application Configuration: Adding Bouncy Castle provider to the end of the provider list");

        } catch (Throwable e) {
            // nothing needed here if that provider does not exist
            logger.info("Alert Application Configuration: Bouncy Castle provider not found");
        }
    }

    private void configureH2Console(HttpSecurity http) throws Exception {
        if (alertProperties.getH2ConsoleEnabled()) {
            ignorePaths(HttpPathManager.PATH_H2_CONSOLE);
            http.headers().frameOptions().disable();
        }
    }

    private void configureWithSSL(HttpSecurity http) throws Exception {
        if (alertProperties.getSslEnabled()) {
            http.requiresChannel().anyRequest().requiresSecure();
        }
    }

    private void ignorePaths(String... paths) {
        for (String path : paths) {
            httpPathManager.addAllowedPath(path);
            httpPathManager.addSamlAllowedPath(path);
        }
    }

    private RequestMatcher[] createCsrfIgnoreMatchers() {
        RequestMatcher[] matchers = {
            new SamlAntMatcher(samlContext(), httpPathManager.getSamlAllowedPaths(), httpPathManager.getAllowedPaths())
        };
        return matchers;
    }

    private RequestMatcher[] createAllowedPathMatchers() {
        RequestMatcher[] matchers = {
            new SamlAntMatcher(samlContext(), httpPathManager.getSamlAllowedPaths(), httpPathManager.getAllowedPaths())
        };
        return matchers;
    }

    private ObjectPostProcessor<AffirmativeBased> createRoleProcessor() {
        return new ObjectPostProcessor<>() {
            @Override
            public AffirmativeBased postProcess(AffirmativeBased affirmativeBased) {
                WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
                DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
                expressionHandler.setRoleHierarchy(authorities -> {
                    String[] allAlertRoles = retrieveAllowedRoles();
                    return AuthorityUtils.createAuthorityList(allAlertRoles);
                });
                webExpressionVoter.setExpressionHandler(expressionHandler);
                affirmativeBased.getDecisionVoters().add(webExpressionVoter);
                return affirmativeBased;
            }
        };
    }

    private String[] retrieveAllowedRoles() {
        return authorizationUtility.getRoles()
                   .stream()
                   .map(UserRoleModel::getName)
                   .toArray(String[]::new);
    }

    @Bean
    public SAMLContext samlContext() {
        return new SAMLContext(authenticationDescriptorKey, configurationAccessor);
    }

    @Bean
    public SAMLManager samlManager() throws MetadataProviderException {
        return new SAMLManager(parserPool(), extendedMetadata(), metadata(), metadataGenerator(), filePersistenceUtil);
    }

    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());

        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOProcessingFilter;
    }

    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        SAMLEntryPoint samlEntryPoint = new AlertSAMLEntryPoint(samlContext());
        samlEntryPoint.setDefaultProfileOptions(webSSOProfileOptions());
        return samlEntryPoint;
    }

    @Bean
    public WebSSOProfileOptions webSSOProfileOptions() {
        AlertWebSSOProfileOptions alertWebSSOProfileOptions = new AlertWebSSOProfileOptions(samlContext());
        alertWebSSOProfileOptions.setIncludeScoping(false);
        alertWebSSOProfileOptions.setProviderName(AuthenticationHandler.SSO_PROVIDER_NAME);
        alertWebSSOProfileOptions.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        return alertWebSSOProfileOptions;
    }

    @Bean
    public FilterChainProxy samlFilter() throws Exception {
        List<SecurityFilterChain> chains = new ArrayList<>();

        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"), samlEntryPoint()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"), samlWebSSOProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"), samlLogoutFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"), samlLogoutProcessingFilter()));
        return new AlertFilterChainProxy(chains, samlContext());
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler();
    }

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
        SavedRequestAwareAuthenticationSuccessHandler redirectHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        redirectHandler.setDefaultTargetUrl("/");
        return redirectHandler;
    }

    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter() {
        return new AlertSAMLMetadataGeneratorFilter(metadataGenerator(), samlContext());
    }

    @Bean
    public MetadataGenerator metadataGenerator() {
        AlertSAMLMetadataGenerator metadataGenerator = new AlertSAMLMetadataGenerator(samlContext());
        metadataGenerator.setExtendedMetadata(extendedMetadata());
        metadataGenerator.setIncludeDiscoveryExtension(false);
        metadataGenerator.setKeyManager(keyManager());
        metadataGenerator.setRequestSigned(false);
        metadataGenerator.setWantAssertionSigned(false);
        metadataGenerator.setBindingsSLO(Collections.emptyList());
        metadataGenerator.setBindingsSSO(Arrays.asList("post"));
        metadataGenerator.setNameID(Arrays.asList(NameIDType.UNSPECIFIED));

        return metadataGenerator;
    }

    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthProvider samlAuthenticationProvider = new SAMLAuthProvider(authenticationEventManager);
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }

    @Bean
    @Qualifier("metadata")
    public CachingMetadataManager metadata() throws MetadataProviderException {
        return new CachingMetadataManager(Collections.emptyList());
    }

    @Bean
    public KeyManager keyManager() {
        return new EmptyKeyManager();
    }

    @Bean
    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(false);
        extendedMetadata.setSignMetadata(false);
        extendedMetadata.setEcpEnabled(true);
        extendedMetadata.setRequireLogoutRequestSigned(false);
        return extendedMetadata;
    }

    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
    }

    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }

    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(),
            new LogoutHandler[] { logoutHandler() },
            new LogoutHandler[] { logoutHandler() });
    }

    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler =
            new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/");
        simpleUrlLogoutSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
        return simpleUrlLogoutSuccessHandler;
    }

    @Bean
    public VelocityEngine velocityEngine() {
        return VelocityFactory.getEngine();
    }

    @Bean
    public SAMLProcessorImpl processor() {
        Collection<SAMLBinding> bindings = new ArrayList<>();
        bindings.add(httpRedirectDeflateBinding());
        bindings.add(httpPostBinding());
        return new SAMLProcessorImpl(bindings);
    }

    @Bean
    public HTTPPostBinding httpPostBinding() {
        return new HTTPPostBinding(parserPool(), velocityEngine());
    }

    @Bean
    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
        return new HTTPRedirectDeflateBinding(parserPool());
    }

    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }

    @Bean(name = "parserPoolHolder")
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }

    @Bean
    public HttpClient httpClient() throws IOException {
        return new HttpClient(multiThreadedHttpConnectionManager());
    }

    @Bean
    public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
        return new MultiThreadedHttpConnectionManager();
    }

    @Bean
    public SAMLDefaultLogger samlLogger() {
        return new SAMLDefaultLogger();
    }

    @Bean
    public SAMLContextProviderImpl contextProvider() {
        return new SAMLContextProviderImpl();
    }

    // SAML 2.0 WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
        // If the machine running Alert has a different time than the SSO system the maximum difference allowed by default is 60 seconds
        // setResponseSkew(...) to change the skew time if needed
        return new WebSSOProfileConsumerImpl();
    }

    // SAML 2.0 Web SSO profile
    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }

    // not used but autowired...
    // SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    // not used but autowired...
    // SAML 2.0 Holder-of-Key Web SSO profile
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    @Bean
    public SingleLogoutProfile logoutProfile() {
        return new SingleLogoutProfileImpl();
    }

    @Bean
    public SAMLUserDetailsService samlUserDetailsService() {
        return new UserDetailsService(authoritiesPopulator);
    }

}
