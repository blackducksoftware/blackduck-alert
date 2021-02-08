package com.synopsys.integration.alert.channel.slack2;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopys.integration.alert.channel.api.convert.ChannelMessageFormatter;

@Component
public class SlackChannelMessageFormatter extends ChannelMessageFormatter {
    private static final int MAX_SLACK_MESSAGE_LENGTH = 3500;
    private static final String SLACK_LINE_SEPARATOR = "\n";
    private static final Map<Character, String> SLACK_CHARACTER_ENCODING_MAP;

    private final MarkupEncoderUtil markupEncoderUtil;

    static {
        // Insertion order matters, so '&' must always be inserted first.
        SLACK_CHARACTER_ENCODING_MAP = new LinkedHashMap<>();
        SLACK_CHARACTER_ENCODING_MAP.put('&', "&amp;");
        SLACK_CHARACTER_ENCODING_MAP.put('<', "&lt;");
        SLACK_CHARACTER_ENCODING_MAP.put('>', "&gt;");
    }

    @Autowired
    public SlackChannelMessageFormatter(MarkupEncoderUtil markupEncoderUtil) {
        super(MAX_SLACK_MESSAGE_LENGTH, SLACK_LINE_SEPARATOR);
        this.markupEncoderUtil = markupEncoderUtil;
    }

    @Override
    public String encode(String txt) {
        return markupEncoderUtil.encodeMarkup(SLACK_CHARACTER_ENCODING_MAP, txt);
    }

    @Override
    public String emphasize(String txt) {
        return String.format("*%s*", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("<%s|%s>", url, txt);
    }

    @Override
    public String getLineSeparator() {
        return SLACK_LINE_SEPARATOR;
    }
}
