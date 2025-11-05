package com.flowable.platform.customizations.delegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Component("activitiService")
public class MyActivitiDelegate implements JavaDelegate {
    private static final Logger log = LoggerFactory.getLogger(MyActivitiDelegate.class);

    @Override
    public void execute(DelegateExecution delegateExecution) {
        log.info("Executing MyActivitiDelegate for processInstanceId={}", delegateExecution.getProcessInstanceId());
    }
}
