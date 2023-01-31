/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security;

import com.synopsys.integration.alert.authentication.saml.security.AlertRelyingPartyRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.component.authentication.security.saml.SAMLContext;
import com.synopsys.integration.alert.component.authentication.security.saml.SamlAntMatcher;

@EnableWebSecurity
@Configuration
public class AuthenticationHandler extends WebSecurityConfigurerAdapter {
    public static final String SSO_PROVIDER_NAME = "Synopsys - Alert";
    private final HttpPathManager httpPathManager;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AlertProperties alertProperties;
    private final RoleAccessor roleAccessor;

    private final FilePersistenceUtil filePersistenceUtil;
    private final UserManagementAuthoritiesPopulator authoritiesPopulator;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final AuthenticationDescriptorKey authenticationDescriptorKey;
    private final AuthenticationEventManager authenticationEventManager;

    @Autowired
    AuthenticationHandler(HttpPathManager httpPathManager, CsrfTokenRepository csrfTokenRepository, AlertProperties alertProperties, RoleAccessor roleAccessor,
        FilePersistenceUtil filePersistenceUtil, UserManagementAuthoritiesPopulator authoritiesPopulator, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        AuthenticationDescriptorKey authenticationDescriptorKey, AuthenticationEventManager authenticationEventManager) {
        this.httpPathManager = httpPathManager;
        this.csrfTokenRepository = csrfTokenRepository;
        this.alertProperties = alertProperties;
        this.roleAccessor = roleAccessor;
        this.filePersistenceUtil = filePersistenceUtil;
        this.authoritiesPopulator = authoritiesPopulator;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.authenticationDescriptorKey = authenticationDescriptorKey;
        this.authenticationEventManager = authenticationEventManager;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureWithSSL(http);
        http.authorizeRequests()
            .requestMatchers(createAllowedPathMatchers()).permitAll()
            .and().authorizeRequests().anyRequest().authenticated()
            .and().csrf().csrfTokenRepository(csrfTokenRepository).ignoringRequestMatchers(createCsrfIgnoreMatchers())
            .withObjectPostProcessor(createRoleProcessor())
            .and().logout().logoutSuccessUrl("/");
    }

    private void configureWithSSL(HttpSecurity http) throws Exception {
        if (alertProperties.getSslEnabled()) {
            http.requiresChannel().anyRequest().requiresSecure();
        }
    }

    private RequestMatcher[] createCsrfIgnoreMatchers() {
        return createRequestMatcherArray();
    }

    private RequestMatcher[] createAllowedPathMatchers() {
        return createRequestMatcherArray();
    }

    private RequestMatcher[] createRequestMatcherArray() {
        return new RequestMatcher[] {
            new SamlAntMatcher(samlContext(), httpPathManager.getSamlAllowedPaths(), httpPathManager.getAllowedPaths())
        };
    }

    private ObjectPostProcessor<AffirmativeBased> createRoleProcessor() {
        return new ObjectPostProcessor<>() {
            @Override
            public <O extends AffirmativeBased> O postProcess(O affirmativeBased) {
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
        return roleAccessor.getRoles()
                   .stream()
                   .map(UserRoleModel::getName)
                   .toArray(String[]::new);
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
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }

    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler =
            new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/");
        simpleUrlLogoutSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
        return simpleUrlLogoutSuccessHandler;
    }

    // TODO; See new apache HttpClient and PoolingHttpClientConnectionManager for new way to do this
//    @Bean
//    public HttpClient httpClient() {
//        return new HttpClient(multiThreadedHttpConnectionManager());
//    }
//
//    @Bean
//    public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
//        return new MultiThreadedHttpConnectionManager();
//    }

    // ==========
    // SAML Beans
    // ==========

    @Bean
    public SAMLContext samlContext() {
        return new SAMLContext(authenticationDescriptorKey, configurationModelConfigurationAccessor);
    }

    @Bean
    public AlertRelyingPartyRegistrationRepository alertRelyingPartyRegistrationRepository() {
        return new AlertRelyingPartyRegistrationRepository();
    }
}
