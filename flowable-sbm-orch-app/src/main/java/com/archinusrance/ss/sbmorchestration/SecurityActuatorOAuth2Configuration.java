package com.archinsurance.ss.sbmorchestration;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import com.flowable.actuate.autoconfigure.security.servlet.ActuatorRequestMatcher;
import com.flowable.platform.common.security.SecurityConstants;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "application.security", name = "type", havingValue = "oauth2")
public class SecurityActuatorOAuth2Configuration {

    @Bean
    @Order(6) // Actuator configuration should kick in before the Form Login there should always be http basic for the endpoints
    public SecurityFilterChain oauthActuatorSecurity(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable);

        http
                .securityMatcher(new ActuatorRequestMatcher())
                .authorizeHttpRequests(configurer -> configurer
                        .requestMatchers(EndpointRequest.to(InfoEndpoint.class, HealthEndpoint.class)).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority(SecurityConstants.ACCESS_ACTUATORS)
                        .anyRequest().denyAll()
                );

        http.oauth2Login(Customizer.withDefaults());
        http.oauth2Client(Customizer.withDefaults());

        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        JwtDecoder jwtDecoder = applicationContext
                .getBeanProvider(JwtDecoder.class)
                .getIfAvailable();
        if (jwtDecoder != null) {
            // If there is a jwtDecoder bean it means that the oauth2 resource server should be configured
            http.oauth2ResourceServer(configurer -> configurer.jwt(Customizer.withDefaults()));
        }

        return http.build();
    }

}
