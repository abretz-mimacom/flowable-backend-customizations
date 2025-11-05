package com.flowable.platform.customizations.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.bpmn.model.FieldExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@org.springframework.stereotype.Component("accountService")
public class MyFlowableDelegate implements JavaDelegate {
    private static final Logger log = LoggerFactory.getLogger(MyFlowableDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        // Read variables
        String input = (String) execution.getVariable("input");
        String greeting = Optional.ofNullable((String) execution.getVariable("greeting"))
                .orElse("Hello");

        // Optionally read a <field> from the BPMN ServiceTask
        String staticMessage = resolveField(execution, "staticMessage").orElse("");

        // Do some work (toy example)
        String result = String.format("%s %s! %s", greeting, input != null ? input : "world", staticMessage).trim();

        // Set output
        execution.setVariable("result", result);

        log.info("MyExampleDelegate produced result='{}' for processInstanceId={}", result, execution.getProcessInstanceId());
    }

    private Optional<String> resolveField(DelegateExecution execution, String fieldName) {
        FlowElement fe = execution.getCurrentFlowElement();
        if (fe instanceof ServiceTask) {
            ServiceTask st = (ServiceTask) fe;
            if (st.getFieldExtensions() != null) {
                for (FieldExtension f : st.getFieldExtensions()) {
                    if (fieldName.equals(f.getFieldName())) {
                        // Prefer stringValue; expressionValue would need evaluation if present
                        String v = f.getStringValue();
                        if (v == null && f.getExpression() != null) {
                            v = f.getExpression();
                        }
                        return Optional.ofNullable(v);
                    }
                }
            }
        }
        return Optional.empty();
    }
}

