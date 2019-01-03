package com.synopsys.integration.alert.web.controller.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.web.controller.BaseController;

@RestController
@RequestMapping(BaseController.METADATA_BASE_PATH + DescriptorController.DESCRIPTORS_PATH)
public class DescriptorController {
    public static final String DESCRIPTORS_PATH = "/descriptors";

    private final Collection<Descriptor> descriptors;

    @Autowired
    public DescriptorController(final Collection<Descriptor> descriptors) {
        this.descriptors = descriptors;
    }

    // TODO use UIComponents?

    @GetMapping
    public Set<DescriptorTempModel> getDescriptors(@RequestParam(required = false) final String name, @RequestParam(required = false) final DescriptorType type, @RequestParam(required = false) final ConfigContextEnum context) {
        Set<Descriptor> filteredDescriptors = filter(descriptors, Descriptor::hasUIConfigs);
        if (name != null) {
            filteredDescriptors = filter(filteredDescriptors, descriptor -> name.equalsIgnoreCase(descriptor.getName()));
        }
        if (type != null) {
            filteredDescriptors = filter(filteredDescriptors, descriptor -> type.equals(descriptor.getType()));
        }
        if (context != null) {
            filteredDescriptors = filter(filteredDescriptors, descriptor -> descriptor.hasUIConfigForType(context));
        }
        return createModels(filteredDescriptors, context);
    }

    private Set<Descriptor> filter(final Collection<Descriptor> descriptors, final Predicate<Descriptor> predicate) {
        return descriptors
                   .stream()
                   .filter(predicate)
                   .collect(Collectors.toSet());
    }

    private Set<DescriptorTempModel> createModels(final Set<Descriptor> filteredDescriptors, final ConfigContextEnum context) {
        final ConfigContextEnum[] applicableContexts;
        if (context != null) {
            applicableContexts = new ConfigContextEnum[] { context };
        } else {
            applicableContexts = ConfigContextEnum.values();
        }

        final Set<DescriptorTempModel> models = new HashSet<>();
        for (final ConfigContextEnum applicableContext : applicableContexts) {
            for (final Descriptor descriptor : filteredDescriptors) {
                models.add(createModel(descriptor, applicableContext));
            }
        }
        return models;
    }

    private DescriptorTempModel createModel(final Descriptor descriptor, final ConfigContextEnum context) {
        return new DescriptorTempModel(descriptor.getName(), descriptor.getType(), context, descriptor.getUIConfig(context).generateUIComponent());
    }
}
