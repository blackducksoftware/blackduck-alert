package com.blackduck.integration.alert.common.system;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.util.ResourceUtil;
import com.google.gson.Gson;

@Component
public class SystemInfoReader {
    private final Logger logger = LoggerFactory.getLogger(SystemInfoReader.class);
    private final Gson gson;
    private SystemInfo cachedSystemInfo;

    @Autowired
    public SystemInfoReader(Gson gson) {
        this.gson = gson;
        this.cachedSystemInfo = null;
    }

    public Optional<SystemInfo> getSystemInfo() {
        Optional<SystemInfo> systemInfo = Optional.empty();
        try {
            // read the system info
            if(cachedSystemInfo == null) {
                String aboutJson = ResourceUtil.getResourceAsString(getClass(), "/about.txt", StandardCharsets.UTF_8.toString());
                cachedSystemInfo =  gson.fromJson(aboutJson, SystemInfo.class);
            }
            systemInfo = Optional.ofNullable(cachedSystemInfo);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return systemInfo;
    }
}
