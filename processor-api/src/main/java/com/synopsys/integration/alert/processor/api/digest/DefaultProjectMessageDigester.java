package com.synopsys.integration.alert.processor.api.digest;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.processor.api.extract.model.CombinableModel;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

@Component
public class DefaultProjectMessageDigester implements ProjectMessageDigester {
    @Override
    public List<ProjectMessage> digest(List<ProjectMessage> notifications) {
        return CombinableModel.combine(notifications);
    }

}
