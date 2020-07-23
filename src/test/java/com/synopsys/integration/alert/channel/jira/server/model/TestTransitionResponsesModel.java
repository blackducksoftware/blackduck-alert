package com.synopsys.integration.alert.channel.jira.server.model;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;

public class TestTransitionResponsesModel extends TransitionsResponseModel {
    List<TransitionComponent> transitions;

    public TestTransitionResponsesModel() {
        TransitionComponent doneTransition = new TransitionComponent("1", "done", new TestDoneStatusDetailsComponent(), null, null, null, null, null);
        TransitionComponent openTransition = new TransitionComponent("2", "new", new TestNewStatusDetailsComponent(), null, null, null, null, null);
        List<TransitionComponent> transitions = new ArrayList<>();
        transitions.add(doneTransition);
        transitions.add(openTransition);
        this.transitions = transitions;
    }

    @Override
    public List<TransitionComponent> getTransitions() {
        return transitions;
    }
}
