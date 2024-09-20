/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.synopsys.integration.alert.authentication.saml.security.SAMLGroupConverter;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

@EnableWebSecurity
@Configuration
public class AuthenticationHandler {
    private final HttpPathManager httpPathManager;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AlertProperties alertProperties;
    private final RoleAccessor roleAccessor;

    private final SAMLGroupConverter samlGroupConverter;
    private final AlertAuthenticationProvider authenticationProvider;

    @Autowired
    AuthenticationHandler(
        HttpPathManager httpPathManager,
        CsrfTokenRepository csrfTokenRepository,
        AlertProperties alertProperties,
        RoleAccessor roleAccessor,
        SAMLGroupConverter samlGroupConverter,
        AlertAuthenticationProvider authenticationProvider
    ) {
        this.httpPathManager = httpPathManager;
        this.csrfTokenRepository = csrfTokenRepository;
        this.alertProperties = alertProperties;
        this.roleAccessor = roleAccessor;
        this.samlGroupConverter = samlGroupConverter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        configureWithSSL(http);
        RequestMatcher[] allowedRequestMatchers = createAllowedPathMatchers();
        CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
        // set the name of the attribute the CsrfToken will be populated on
        csrfRequestHandler.setCsrfRequestAttributeName(null);
        http.securityContext(securityContext -> {
                securityContext.requireExplicitSave(true);
                securityContext.securityContextRepository(securityContextRepository);
            })
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(customizer -> {
                customizer.requestMatchers(allowedRequestMatchers).permitAll();
                customizer.anyRequest().authenticated();
            })
            .headers(customizer -> {
                customizer.contentSecurityPolicy(cspCustomizer -> {
                    cspCustomizer.policyDirectives(
                        "form-action 'self'; default-src 'self' 'https://www.synopsys.com'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' 'https://www.synopsys.com';");
                });
            })
            .csrf(customizer -> {
                customizer.csrfTokenRequestHandler(csrfRequestHandler);
                customizer.csrfTokenRepository(csrfTokenRepository);
                customizer.ignoringRequestMatchers(allowedRequestMatchers);
            })
            .authorizeHttpRequests(customizer -> {
                customizer.withObjectPostProcessor(createRoleProcessor());
            })
            .logout(customizer -> customizer.logoutSuccessUrl(HttpPathManager.PATH_ROOT));

        configureSAML(http);
        return http.build();
    }

    private void configureWithSSL(HttpSecurity http) throws Exception {
        if (alertProperties.getSslEnabled()) {
            http.requiresChannel().anyRequest().requiresSecure();
        }
    }

    private void configureSAML(HttpSecurity http) throws Exception {
        //eventually configure SAML
        OpenSaml4AuthenticationProvider openSaml4AuthenticationProvider = new OpenSaml4AuthenticationProvider();
        openSaml4AuthenticationProvider.setResponseAuthenticationConverter(samlGroupConverter.groupsConverter());

        http.saml2Login(saml2 -> {
                saml2.authenticationManager(new ProviderManager(openSaml4AuthenticationProvider));
                saml2.loginPage("/");
            })
            .saml2Logout(Customizer.withDefaults());
    }

    private RequestMatcher[] createCsrfIgnoreMatchers() {
        return createRequestMatcherArray();
    }

    private RequestMatcher[] createAllowedPathMatchers() {
        return createRequestMatcherArray();
    }

    private RequestMatcher[] createRequestMatcherArray() {
        return new RequestMatcher[] {
            request ->
                Arrays.stream(httpPathManager.getAllowedPaths())
                    .map(AntPathRequestMatcher::new)
                    .anyMatch(requestMatcher -> requestMatcher.matches(request))
        };
    }

    private ObjectPostProcessor<AffirmativeBased> createRoleProcessor() {
        return new ObjectPostProcessor<AffirmativeBased>() {
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

    @Bean
    public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(authorities -> {
            String[] allAlertRoles = retrieveAllowedRoles();
            return AuthorityUtils.createAuthorityList(allAlertRoles);
        });
        return expressionHandler;
    }

    private String[] retrieveAllowedRoles() {
        return roleAccessor.getRoles()
            .stream()
            .map(UserRoleModel::getName)
            .toArray(String[]::new);
    }

    // ==========
    // SAML Beans
    // ==========

    @Bean
    Saml2AuthenticationRequestResolver authenticationRequestResolver(RelyingPartyRegistrationRepository registrations) {
        RelyingPartyRegistrationResolver registrationResolver =
            new DefaultRelyingPartyRegistrationResolver(registrations);
        OpenSaml4AuthenticationRequestResolver authenticationRequestResolver =
            new OpenSaml4AuthenticationRequestResolver(registrationResolver);
        authenticationRequestResolver.setAuthnRequestCustomizer(context -> context
            .getAuthnRequest().setForceAuthn(false));
        return authenticationRequestResolver;
    }
}
