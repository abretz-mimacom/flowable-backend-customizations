package com.archinsurance.ss.sbmorchestration;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.util.stream.Collectors;

import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher;

import com.flowable.autoconfigure.security.FlowableHttpSecurityCustomizer;
import com.flowable.core.spring.security.web.authentication.AjaxAuthenticationFailureHandler;
import com.flowable.core.spring.security.web.authentication.AjaxAuthenticationSuccessHandler;
import com.flowable.platform.common.security.SecurityConstants;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "application.security", name = "type", havingValue = "basic", matchIfMissing = true)
@EnableWebSecurity
public class SecurityHttpBasicConfiguration {

    @Bean
    @Order(10)
    public SecurityFilterChain basicDefaultSecurity(HttpSecurity http, ObjectProvider<FlowableHttpSecurityCustomizer> httpSecurityCustomizers) throws Exception {
        for (FlowableHttpSecurityCustomizer customizer : httpSecurityCustomizers.orderedStream()
                .collect(Collectors.toList())) {
            customizer.customize(http);
        }

        http
                .logout(logout -> logout.logoutUrl("/auth/logout").logoutSuccessUrl("/"));

        // Non authenticated exception handling. The formLogin and httpBasic configure the exceptionHandling
        // We have to initialize the exception handling with a default authentication entry point in order to return 401 each time and not have a
        // forward due to the formLogin or the http basic popup due to the httpBasic
        http
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .defaultAuthenticationEntryPointFor((request, response, authException) -> {}, new DispatcherTypeRequestMatcher(DispatcherType.ERROR))
                        .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), AnyRequestMatcher.INSTANCE))
                .formLogin(formLogin -> formLogin
                        .loginProcessingUrl("/auth/login")
                        .successHandler(new AjaxAuthenticationSuccessHandler())
                        .failureHandler(new AjaxAuthenticationFailureHandler())
                )
                .authorizeHttpRequests(configurer -> configurer
                        .requestMatchers(antMatcher("/analytics-api/**")).hasAuthority(SecurityConstants.ACCESS_REPORTS_METRICS)
                        // allow context root for all (it triggers the loading of the initial page)
                        .requestMatchers(antMatcher("/")) .permitAll()
                        .requestMatchers(
                                antMatcher("/**/*.svg"), antMatcher("/**/*.ico"), antMatcher("/**/*.png"), antMatcher("/**/*.woff2"), antMatcher("/**/*.css"),
                                antMatcher("/**/*.woff"), antMatcher("/**/*.html"), antMatcher("/**/*.js"),
                                antMatcher("/**/flowable-frontend-configuration"),
                                antMatcher("/**/index.html")).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
