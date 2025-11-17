package com.flowable.platform.customizations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "flowable.customizations.acme")
public class BackendCustomizationProperties {

    /**
     * Enable or disable the customizations.
     */
    private boolean enabled = true;

    /**
     * Example property for customization.
     */
    private String exampleProperty = "defaultValue";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExampleProperty() {
        return exampleProperty;
    }

    public void setExampleProperty(String exampleProperty) {
        this.exampleProperty = exampleProperty;
    }
}