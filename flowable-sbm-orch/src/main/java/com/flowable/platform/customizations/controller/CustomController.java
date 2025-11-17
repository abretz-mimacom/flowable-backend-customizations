package com.flowable.platform.customizations.controller;

import com.flowable.platform.customizations.properties.BackendCustomizationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.http.HttpClient;

@RestController
@RequestMapping(value = CustomController.ROOT)
public class CustomController {
    public static final String ROOT = "/custom-api";
    private static final Logger log = LoggerFactory.getLogger(CustomController.class);

    private RestTemplate restTemplate;

    BackendCustomizationProperties backendCustomizationProperties;

    public CustomController(RestTemplateBuilder restTemplateBuilder, BackendCustomizationProperties backendCustomizationProperties) {
        initializeRestTemplate(restTemplateBuilder);
        this.backendCustomizationProperties = backendCustomizationProperties;
    }

    private void initializeRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .additionalInterceptors(new RestTemplateInterceptor())
                .requestFactory(this::createClientHttpRequestFactory)
                .build();
    }
    @RequestMapping(path = "{service-name}/**", produces = MediaType.APPLICATION_JSON_VALUE,
            method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<Object> apiGateway(
            @PathVariable("service-name") String name,
            @RequestBody(required = false) String body,
            HttpMethod method,
            HttpServletRequest request
    ) {
        return new ResponseEntity<>("Custom API Response from " + name + " with property: " + backendCustomizationProperties.getExampleProperty(), org.springframework.http.HttpStatus.OK);
    }
    private static class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            if (log.isDebugEnabled()) {
                log.debug("Executing {} {}", request.getMethod(), request.getURI());
            }
            return execution.execute(request, body);
        }

    }

    private ClientHttpRequestFactory createClientHttpRequestFactory() {
        HttpClient jdk = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        return new JdkClientHttpRequestFactory(jdk);
    }
}
