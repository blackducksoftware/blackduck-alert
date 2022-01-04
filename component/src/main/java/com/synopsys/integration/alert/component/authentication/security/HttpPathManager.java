/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;

@Component
public class HttpPathManager {
    public static final String PATH_ROOT_HASHTAG = "/#";
    public static final String PATH_ROOT = "/";

    public static final String PATH_CSS_STYLE_CSS = "/css/style.css";
    public static final String PATH_FAVICON_ICO = "/favicon.ico";
    public static final String PATH_FONTS = "/fonts/**";
    public static final String PATH_INDEX_HTML = "index.html";
    public static final String PATH_JS_BUNDLE_JS = "/js/bundle.js";
    public static final String PATH_JS_BUNDLE_JS_MAP = "/js/bundle.js.map";
    public static final String PATH_SAML_ROOT = "/saml/**";
    public static final String PATH_ABOUT = AlertRestConstants.BASE_PATH + "/about";
    public static final String PATH_LOGIN = AlertRestConstants.BASE_PATH + "/login";
    public static final String PATH_LOGOUT = AlertRestConstants.BASE_PATH + "/logout";
    public static final String PATH_PASSWORD_RESET = AlertRestConstants.BASE_PATH + "/resetPassword";
    public static final String PATH_PASSWORD_RESET_ROOT = AlertRestConstants.BASE_PATH + "/resetPassword/**";
    public static final String PATH_SYSTEM_MESSAGES_LATEST = AlertRestConstants.BASE_PATH + "/system/messages/latest";
    public static final String PATH_VERIFY = AlertRestConstants.BASE_PATH + "/verify";
    public static final String PATH_VERIFY_SAML = PATH_VERIFY + "/saml";

    private static final String[] DEFAULT_PATHS = {
        PATH_ROOT,
        PATH_ROOT_HASHTAG,
        PATH_FAVICON_ICO,
        PATH_FONTS,
        PATH_JS_BUNDLE_JS,
        PATH_JS_BUNDLE_JS_MAP,
        PATH_CSS_STYLE_CSS,
        PATH_INDEX_HTML,
        PATH_SAML_ROOT,
        PATH_ABOUT,
        PATH_LOGIN,
        PATH_LOGOUT,
        PATH_PASSWORD_RESET,
        PATH_PASSWORD_RESET_ROOT,
        PATH_SYSTEM_MESSAGES_LATEST,
        PATH_VERIFY,
        PATH_VERIFY_SAML
    };

    private static final String[] DEFAULT_SAML_PATHS = {
        PATH_ROOT_HASHTAG,
        PATH_FAVICON_ICO,
        PATH_FONTS,
        PATH_JS_BUNDLE_JS,
        PATH_JS_BUNDLE_JS_MAP,
        PATH_CSS_STYLE_CSS,
        PATH_SAML_ROOT,
        PATH_ABOUT,
        PATH_SYSTEM_MESSAGES_LATEST,
        PATH_VERIFY,
        PATH_VERIFY_SAML,
        PATH_LOGOUT
    };

    private final Collection<String> allowedPaths;
    private final Collection<String> samlAllowedPaths;

    @Autowired
    public HttpPathManager() {
        allowedPaths = createDefaultAllowedPaths();
        samlAllowedPaths = createSamlDefaultAllowedPaths();
    }

    private List<String> createDefaultPaths(String[] paths) {
        List<String> list = new LinkedList<>();
        Collections.addAll(list, paths);
        return list;
    }

    private List<String> createDefaultAllowedPaths() {
        return createDefaultPaths(DEFAULT_PATHS);
    }

    private List<String> createSamlDefaultAllowedPaths() { return createDefaultPaths(DEFAULT_SAML_PATHS);}

    public void addAllowedPath(String path) {
        allowedPaths.add(path);
    }

    public void addSamlAllowedPath(String path) {
        samlAllowedPaths.add(path);
    }

    public String[] getAllowedPaths() {
        String[] allowedPathArray = new String[allowedPaths.size()];
        return allowedPaths.toArray(allowedPathArray);
    }

    public String[] getSamlAllowedPaths() {
        String[] allowedPathArray = new String[samlAllowedPaths.size()];
        return samlAllowedPaths.toArray(allowedPathArray);
    }
}
