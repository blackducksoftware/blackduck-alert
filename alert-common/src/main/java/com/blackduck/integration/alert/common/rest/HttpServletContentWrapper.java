package com.blackduck.integration.alert.common.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpServletContentWrapper {
    private final HttpServletRequest httpRequest;
    private final HttpServletResponse httpResponse;

    public HttpServletContentWrapper(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public HttpServletResponse getHttpResponse() {
        return httpResponse;
    }

}
