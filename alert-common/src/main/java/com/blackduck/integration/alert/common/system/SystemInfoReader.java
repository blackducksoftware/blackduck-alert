package com.blackduck.integration.alert.common.system;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
    private final AtomicReference<SystemInfo> cachedSystemInfo;

    @Autowired
    public SystemInfoReader(Gson gson) {
        this.gson = gson;
        this.cachedSystemInfo = new AtomicReference<>();
    }

    public Optional<SystemInfo> getSystemInfo() {
        Optional<SystemInfo> systemInfo = Optional.empty();
        try {
            // read the system info
            if(cachedSystemInfo.get() == null) {
                String aboutJson = ResourceUtil.getResourceAsString(getClass(), "/about.txt", StandardCharsets.UTF_8.toString());
                SystemInfo info = gson.fromJson(aboutJson, SystemInfo.class);
                if(info != null) {
                    cachedSystemInfo.set(info);
                }
            }
            systemInfo = Optional.ofNullable(cachedSystemInfo.get());
        } catch (Exception e) {
            logger.error("Error retrieving system info data.", e);
        }
        return systemInfo;
    }
}
