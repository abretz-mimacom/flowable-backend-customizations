package com.archinsurance.ss.sbmorchestration;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.util.stream.Collectors;

import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatchers;

import com.flowable.autoconfigure.security.FlowableHttpSecurityCustomizer;
import com.flowable.core.spring.security.token.FlowableJwtResourceServerConfigurer;
import com.flowable.core.spring.security.web.savedrequest.MatchingRequestParameterNameRemovalRedirectFilter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "application.security", name = "type", havingValue = "oauth2")
@EnableWebSecurity
public class SecurityOAuth2Configuration {

    @Autowired
    protected ObjectProvider<OidcClientInitiatedLogoutSuccessHandler> oidcClientInitiatedLogoutSuccessHandlerProvider;

    @Bean
    @Order(10)
    public SecurityFilterChain oauthDefaultSecurity(HttpSecurity http, ObjectProvider<FlowableHttpSecurityCustomizer> httpSecurityCustomizers) throws Exception {
        for (FlowableHttpSecurityCustomizer customizer : httpSecurityCustomizers.orderedStream()
                .collect(Collectors.toList())) {
            customizer.customize(http);
        }

        http.exceptionHandling(exceptionHandling -> exceptionHandling
                // Using this entry point as a default entry point if none of the others match (the first default entry point is the default entry point).
                // This one will not match any request, so it won't override other defaults from Spring Security
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), RequestMatchers.not(AnyRequestMatcher.INSTANCE))
                .defaultAuthenticationEntryPointFor((request, response, authException) -> {}, new DispatcherTypeRequestMatcher(DispatcherType.ERROR))
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest"))
        );

        // We mark all requests as authenticated in order for the redirect to happen when the application is accessed
        // We allow the favicon to always be available, since it is invoked by the browser itself
        http.authorizeHttpRequests(configurer -> configurer
                .requestMatchers(antMatcher("/favicon.ico")).permitAll()
                .anyRequest().authenticated()
        );
        // Currently an HttpSessionSecurityContextRepository is needed for the oauth2 to work
        HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
        securityContextRepository.setDisableUrlRewriting(true);
        http.securityContext(securityContext -> securityContext.securityContextRepository(securityContextRepository));

        http.addFilterAfter(new MatchingRequestParameterNameRemovalRedirectFilter(), RequestCacheAwareFilter.class);

        http.oauth2Login(Customizer.withDefaults());
        http.oauth2Client(Customizer.withDefaults());
        http.logout(logout -> {
            logout
                    .logoutUrl("/auth/logout")
                    .logoutSuccessUrl("/");
            oidcClientInitiatedLogoutSuccessHandlerProvider.ifAvailable(logout::logoutSuccessHandler);
        });

        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        JwtDecoder jwtDecoder = applicationContext
                .getBeanProvider(JwtDecoder.class)
                .getIfAvailable();
        if (jwtDecoder != null) {
            // If there is a jwtDecoder bean and the FlowableJwtResourceServerConfigurer has not been applied
            // it means that the oauth2 resource server should be configured
            if (http.getConfigurer(FlowableJwtResourceServerConfigurer.class) == null) {
                http.oauth2ResourceServer(configurer -> configurer.jwt(Customizer.withDefaults()));
            }
        }

        return http.build();
    }

}
