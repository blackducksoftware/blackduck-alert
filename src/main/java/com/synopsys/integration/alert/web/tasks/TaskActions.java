package com.synopsys.integration.alert.web.tasks;

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

@Component
@Transactional
public class TaskActions {

    public Collection<Object> getTasks() {
        return List.of();
    }
}
