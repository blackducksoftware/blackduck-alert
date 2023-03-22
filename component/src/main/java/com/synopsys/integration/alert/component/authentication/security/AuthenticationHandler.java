/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security;

import com.synopsys.integration.alert.authentication.saml.security.SAMLGroupConverter;
import com.synopsys.integration.alert.authentication.saml.security.SAMLSignedAssertionsValidator;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.security.saml2.core.Saml2ResponseValidatorResult;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

import java.util.Arrays;

import static org.springframework.security.saml2.core.Saml2ErrorCodes.INVALID_ASSERTION;

@EnableWebSecurity
@Configuration
public class AuthenticationHandler extends WebSecurityConfigurerAdapter {
    private final HttpPathManager httpPathManager;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AlertProperties alertProperties;
    private final RoleAccessor roleAccessor;

    private final SAMLGroupConverter samlGroupConverter;
    private final SAMLSignedAssertionsValidator samlSignedAssertionsValidator;

    @Autowired
    AuthenticationHandler(
        HttpPathManager httpPathManager,
        CsrfTokenRepository csrfTokenRepository,
        AlertProperties alertProperties,
        RoleAccessor roleAccessor,
        SAMLGroupConverter samlGroupConverter,
        SAMLSignedAssertionsValidator samlSignedAssertionsValidator
    ) {
        this.httpPathManager = httpPathManager;
        this.csrfTokenRepository = csrfTokenRepository;
        this.alertProperties = alertProperties;
        this.roleAccessor = roleAccessor;
        this.samlGroupConverter = samlGroupConverter;
        this.samlSignedAssertionsValidator = samlSignedAssertionsValidator;
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
        configureSAML(http);
    }

    private void configureWithSSL(HttpSecurity http) throws Exception {
        if (alertProperties.getSslEnabled()) {
            http.requiresChannel().anyRequest().requiresSecure();
        }
    }

    private void configureSAML(HttpSecurity http) throws Exception {
        //eventually configure SAML
        OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(samlGroupConverter.groupsConverter());
        authenticationProvider.setAssertionValidator(assertionToken -> {
            Saml2ResponseValidatorResult result = OpenSaml4AuthenticationProvider
                .createDefaultAssertionValidator()
                .convert(assertionToken);
            if (samlSignedAssertionsValidator.validateSignedAssertions(assertionToken) == ValidationResult.VALID) {
                return result;
            }
            return result.concat(new Saml2Error(INVALID_ASSERTION, "Assertion signature was not present or valid"));
        });

        http.saml2Login(saml2 -> {
                saml2.authenticationManager(new ProviderManager(authenticationProvider));
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
