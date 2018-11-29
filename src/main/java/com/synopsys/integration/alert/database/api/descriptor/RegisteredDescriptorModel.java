package com.synopsys.integration.alert.database.api.descriptor;

import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorsEntity;
import com.synopsys.integration.util.Stringable;

public class RegisteredDescriptorModel extends Stringable {
    private final Long id;
    private final String name;
    private final String type;

    public RegisteredDescriptorModel(final RegisteredDescriptorsEntity registeredDescriptorsEntity) {
        this.id = registeredDescriptorsEntity.getId();
        this.name = registeredDescriptorsEntity.getName();
        this.type = registeredDescriptorsEntity.getType();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
