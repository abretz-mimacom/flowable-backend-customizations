package com.flowable.platform.customizations;

import com.flowable.autoconfigure.platform.PlatformAutoConfiguration;
import com.flowable.platform.customizations.delegates.MyFlowableDelegate;
import com.flowable.platform.customizations.properties.BackendCustomizationProperties;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = {PlatformAutoConfiguration.class})
@EnableConfigurationProperties(BackendCustomizationProperties.class)
public class BackendCustomizationsAutoconfiguration {
//	@ConditionalOnMissingBean(name = "accountService")
	@Bean
	public JavaDelegate accountService(){
		return new MyFlowableDelegate();
	}
}
