/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.logging;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class AlertCompositeLogger implements Logger {
    private final Logger auditLogger;
    private final Logger commonLogger;

    public AlertCompositeLogger(Logger auditLogger, Logger commonLogger) {
        this.auditLogger = auditLogger;
        this.commonLogger = commonLogger;
    }

    @Override
    public String getName() {
        return commonLogger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return commonLogger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        commonLogger.trace(msg);
        auditLogger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        commonLogger.trace(format, arg);
        auditLogger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        commonLogger.trace(format, arg1, arg2);
        auditLogger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        commonLogger.trace(format, arguments);
        auditLogger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        commonLogger.trace(msg, t);
        auditLogger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return commonLogger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        commonLogger.trace(marker, msg);
        auditLogger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        commonLogger.trace(marker, format, arg);
        auditLogger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        commonLogger.trace(marker, format, arg1, arg2);
        auditLogger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        commonLogger.trace(marker, format, argArray);
        auditLogger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        commonLogger.trace(marker, msg, t);
        auditLogger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return commonLogger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        commonLogger.debug(msg);
        auditLogger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        commonLogger.debug(format, arg);
        auditLogger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        commonLogger.debug(format, arg1, arg2);
        auditLogger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        commonLogger.debug(format, arguments);
        auditLogger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        commonLogger.debug(msg, t);
        auditLogger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return commonLogger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        commonLogger.debug(marker, msg);
        auditLogger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        commonLogger.debug(marker, format, arg);
        auditLogger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        commonLogger.debug(marker, format, arg1, arg2);
        auditLogger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        commonLogger.debug(marker, format, arguments);
        auditLogger.debug(marker, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        commonLogger.debug(marker, msg, t);
        auditLogger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return commonLogger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        commonLogger.info(msg);
        auditLogger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        commonLogger.info(format, arg);
        auditLogger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        commonLogger.info(format, arg1, arg2);
        auditLogger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        commonLogger.info(format, arguments);
        auditLogger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        commonLogger.info(msg, t);
        auditLogger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return commonLogger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        commonLogger.info(marker, msg);
        auditLogger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        commonLogger.info(marker, format, arg);
        auditLogger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        commonLogger.info(marker, format, arg1, arg2);
        auditLogger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        commonLogger.info(marker, format, arguments);
        auditLogger.info(marker, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        commonLogger.info(marker, msg, t);
        auditLogger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return commonLogger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        commonLogger.warn(msg);
        auditLogger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        commonLogger.warn(format, arg);
        auditLogger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        commonLogger.warn(format, arguments);
        auditLogger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        commonLogger.warn(format, arg1, arg2);
        auditLogger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        commonLogger.warn(msg, t);
        auditLogger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return commonLogger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        commonLogger.warn(marker, msg);
        auditLogger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        commonLogger.warn(marker, format, arg);
        auditLogger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        commonLogger.warn(marker, format, arg1, arg2);
        auditLogger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        commonLogger.warn(marker, format, arguments);
        auditLogger.warn(marker, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        commonLogger.warn(marker, msg, t);
        auditLogger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return commonLogger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        commonLogger.error(msg);
        auditLogger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        commonLogger.error(format, arg);
        auditLogger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        commonLogger.error(format, arg1, arg2);
        auditLogger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        commonLogger.error(format, arguments);
        auditLogger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        commonLogger.error(msg, t);
        auditLogger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return commonLogger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        commonLogger.error(marker, msg);
        auditLogger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        commonLogger.error(marker, format, arg);
        auditLogger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        commonLogger.error(marker, format, arg1, arg2);
        auditLogger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        commonLogger.error(marker, format, arguments);
        auditLogger.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        commonLogger.error(marker, msg, t);
        auditLogger.error(marker, msg, t);
    }

}
