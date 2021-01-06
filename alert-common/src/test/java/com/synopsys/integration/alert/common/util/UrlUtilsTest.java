package com.synopsys.integration.alert.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UrlUtilsTest {
    @Test
    public void argWithTrailingSlashTest() {
        String url = "https://awebsite.com";
        String formattedUrl = UrlUtils.appendTrailingSlashIfNoneExists(url + "/");
        assertEquals(url + "/", formattedUrl);
    }

    @Test
    public void argWithNoTrailingSlashTest() {
        String url = "https://awebsite.com";
        String formattedUrl = UrlUtils.appendTrailingSlashIfNoneExists(url);
        assertEquals(url + "/", formattedUrl);
    }

    @Test
    public void argSurroundedByWhitespaceTest() {
        String url = "https://awebsite.com/";
        String formattedUrl = UrlUtils.appendTrailingSlashIfNoneExists("  " + url + "     ");
        assertEquals(url, formattedUrl);
    }

    @Test
    public void nullArgTest() {
        String formattedUrl = UrlUtils.appendTrailingSlashIfNoneExists(null);
        assertEquals("", formattedUrl);
    }

    @Test
    public void emptyStringArgTest() {
        String formattedUrl = UrlUtils.appendTrailingSlashIfNoneExists("");
        assertEquals("", formattedUrl);
    }

    @Test
    public void blankStringArgTest() {
        String formattedUrl = UrlUtils.appendTrailingSlashIfNoneExists("       ");
        assertEquals("", formattedUrl);
    }

}
