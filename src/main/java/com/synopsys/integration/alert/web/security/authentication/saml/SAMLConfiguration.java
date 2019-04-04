/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.security.authentication.saml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;

@Configuration
public class SAMLConfiguration {
    public static final String SSO_PROVIDER_NAME = "Synopsys - Alert";

    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public SAMLConfiguration(final ConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    //    @Bean
    //    public static SAMLBootstrap sAMLBootstrap() {
    //        return new SAMLBootstrap();
    //    }
    //
    //    @Override
    //    protected void configure(final AuthenticationManagerBuilder auth) {
    //        auth.authenticationProvider(samlAuthenticationProvider());
    //    }
    //
    //    @Override
    //    protected void configure(final HttpSecurity http) throws Exception {
    //        http.exceptionHandling().authenticationEntryPoint(samlEntryPoint());
    //    }
    //
    //    @Bean
    //    @Qualifier("metadata")
    //    public CachingMetadataManager metadata() throws MetadataProviderException {
    //        return new CachingMetadataManager(Collections.emptyList());
    //    }
    //
    //    @Bean
    //    public KeyManager keyManager() {
    //        return new EmptyKeyManager();
    //    }
    //
    //    @Bean(initMethod = "initialize")
    //    public StaticBasicParserPool parserPool() {
    //        return new StaticBasicParserPool();
    //    }
    //
    //    @Bean
    //    public ExtendedMetadata extendedMetadata() {
    //        final ExtendedMetadata extendedMetadata = new ExtendedMetadata();
    //        extendedMetadata.setIdpDiscoveryEnabled(false);
    //        extendedMetadata.setSignMetadata(false);
    //        extendedMetadata.setEcpEnabled(true);
    //        extendedMetadata.setRequireLogoutRequestSigned(false);
    //        return extendedMetadata;
    //    }
    //
    //    @Bean
    //    public SAMLContext samlContext() {
    //        return new SAMLContext(configurationAccessor);
    //    }
    //
    //    @Bean
    //    public SAMLManager samlManager() throws MetadataProviderException {
    //        return new SAMLManager(samlContext(), parserPool(), extendedMetadata(), metadata());
    //    }
    //
    //    @Bean
    //    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
    //        final SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
    //        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManagerBean());
    //
    //        final SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler =
    //            new SavedRequestAwareAuthenticationSuccessHandler();
    //        successRedirectHandler.setDefaultTargetUrl("/");
    //        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler);
    //
    //        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler());
    //        return samlWebSSOProcessingFilter;
    //    }
    //
    //    @Bean
    //    public FilterChainProxy samlFilter() throws Exception {
    //        final List<SecurityFilterChain> chains = new ArrayList<>();
    //
    //        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"), samlEntryPoint()));
    //        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"), samlWebSSOProcessingFilter()));
    //        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"), samlLogoutFilter()));
    //        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"), samlLogoutProcessingFilter()));
    //        return new AlertFilterChainProxy(chains, samlContext());
    //    }
    //
    //    @Bean
    //    public WebSSOProfileOptions webSSOProfileOptions() {
    //        final AlertWebSSOProfileOptions alertWebSSOProfileOptions = new AlertWebSSOProfileOptions(samlContext());
    //        alertWebSSOProfileOptions.setIncludeScoping(false);
    //        alertWebSSOProfileOptions.setProviderName(SAMLConfiguration.SSO_PROVIDER_NAME);
    //        alertWebSSOProfileOptions.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
    //        return alertWebSSOProfileOptions;
    //    }
    //
    //    @Bean
    //    public SAMLEntryPoint samlEntryPoint() {
    //        final SAMLEntryPoint samlEntryPoint = new AlertSAMLEntryPoint(samlContext());
    //        samlEntryPoint.setDefaultProfileOptions(webSSOProfileOptions());
    //        return samlEntryPoint;
    //    }
    //
    //    @Bean
    //    public MetadataGeneratorFilter metadataGeneratorFilter() {
    //        return new AlertSAMLMetadataGeneratorFilter(metadataGenerator(), samlContext());
    //    }
    //
    //    @Bean
    //    public MetadataGenerator metadataGenerator() {
    //        final AlertSAMLMetadataGenerator metadataGenerator = new AlertSAMLMetadataGenerator(samlContext());
    //        metadataGenerator.setExtendedMetadata(extendedMetadata());
    //        metadataGenerator.setIncludeDiscoveryExtension(false);
    //        metadataGenerator.setKeyManager(keyManager());
    //        metadataGenerator.setRequestSigned(false);
    //        metadataGenerator.setWantAssertionSigned(false);
    //        metadataGenerator.setBindingsSLO(Collections.emptyList());
    //        metadataGenerator.setBindingsSSO(Arrays.asList("post"));
    //        metadataGenerator.setNameID(Arrays.asList(NameIDType.UNSPECIFIED));
    //
    //        return metadataGenerator;
    //    }
    //
    //    @Bean
    //    public SAMLAuthenticationProvider samlAuthenticationProvider() {
    //        final SAMLAuthProvider samlAuthenticationProvider = new SAMLAuthProvider();
    //        samlAuthenticationProvider.setForcePrincipalAsString(false);
    //        return samlAuthenticationProvider;
    //    }
    //
    //    @Bean
    //    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
    //        return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
    //    }
    //
    //    @Bean
    //    public SecurityContextLogoutHandler logoutHandler() {
    //        final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    //        logoutHandler.setInvalidateHttpSession(true);
    //        logoutHandler.setClearAuthentication(true);
    //        return logoutHandler;
    //    }
    //
    //    @Bean
    //    public SAMLLogoutFilter samlLogoutFilter() {
    //        return new SAMLLogoutFilter(successLogoutHandler(),
    //            new LogoutHandler[] { logoutHandler() },
    //            new LogoutHandler[] { logoutHandler() });
    //    }
    //
    //    @Bean
    //    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
    //        final SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler =
    //            new SimpleUrlLogoutSuccessHandler();
    //        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/");
    //        simpleUrlLogoutSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
    //        return simpleUrlLogoutSuccessHandler;
    //    }
    //
    //    @Bean
    //    public VelocityEngine velocityEngine() {
    //        return VelocityFactory.getEngine();
    //    }
    //
    //    @Bean(name = "parserPoolHolder")
    //    public ParserPoolHolder parserPoolHolder() {
    //        return new ParserPoolHolder();
    //    }
    //
    //    @Bean
    //    public HTTPPostBinding httpPostBinding(final ParserPool parserPool) {
    //        return new HTTPPostBinding(parserPool, velocityEngine());
    //    }
    //
    //    @Bean
    //    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding(final ParserPool parserPool) {
    //        return new HTTPRedirectDeflateBinding(parserPool);
    //    }
    //
    //    @Bean
    //    public SAMLProcessorImpl processor(final ParserPool parserPool) {
    //        final Collection<SAMLBinding> bindings = new ArrayList<>();
    //        bindings.add(httpRedirectDeflateBinding(parserPool));
    //        bindings.add(httpPostBinding(parserPool));
    //        return new SAMLProcessorImpl(bindings);
    //    }
    //
    //    @Bean
    //    public HttpClient httpClient() throws IOException {
    //        return new HttpClient(multiThreadedHttpConnectionManager());
    //    }
    //
    //    @Bean
    //    public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
    //        return new MultiThreadedHttpConnectionManager();
    //    }
    //
    //    @Bean
    //    public SAMLDefaultLogger samlLogger() {
    //        return new SAMLDefaultLogger();
    //    }
    //
    //    @Bean
    //    public SAMLContextProviderImpl contextProvider() {
    //        return new SAMLContextProviderImpl();
    //    }
    //
    //    // SAML 2.0 WebSSO Assertion Consumer
    //    @Bean
    //    public WebSSOProfileConsumer webSSOprofileConsumer() {
    //        return new WebSSOProfileConsumerImpl();
    //    }
    //
    //    // SAML 2.0 Web SSO profile
    //    @Bean
    //    public WebSSOProfile webSSOprofile() {
    //        return new WebSSOProfileImpl();
    //    }
    //
    //    // not used but autowired...
    //    // SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
    //    @Bean
    //    public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
    //        return new WebSSOProfileConsumerHoKImpl();
    //    }
    //
    //    // not used but autowired...
    //    // SAML 2.0 Holder-of-Key Web SSO profile
    //    @Bean
    //    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
    //        return new WebSSOProfileConsumerHoKImpl();
    //    }
    //
    //    @Bean
    //    public SingleLogoutProfile logoutProfile() {
    //        return new SingleLogoutProfileImpl();
    //    }

}
