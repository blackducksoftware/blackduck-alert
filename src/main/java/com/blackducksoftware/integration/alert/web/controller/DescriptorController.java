package com.blackducksoftware.integration.alert.web.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.alert.common.descriptor.Descriptor;
import com.blackducksoftware.integration.alert.common.descriptor.DescriptorMap;

@RestController
@RequestMapping(DescriptorController.DESCRIPTOR_PATH + "/{descriptorType}")
public class DescriptorController extends BaseController {
    public static final String DESCRIPTOR_PATH = BaseController.BASE_PATH + "/descriptors";

    private final DescriptorMap descriptorMap;

    @Autowired
    public DescriptorController(final DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
    }

    @GetMapping
    public List<Descriptor> getDescriptors(@RequestParam(value = "descriptorName", required = false) final String descriptorName, @PathVariable final String descriptorType) {
        if (StringUtils.isNotBlank(descriptorName)) {
            return Arrays.asList(descriptorMap.getDescriptor(descriptorName));
        }

        return descriptorMap.getDescriptorMap().values()
                .stream()
                .filter(descriptorVals -> descriptorVals.getType().name().equals(descriptorType))
                .collect(Collectors.toList());
    }
}
