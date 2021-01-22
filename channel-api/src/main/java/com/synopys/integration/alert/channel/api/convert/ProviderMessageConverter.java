package com.synopys.integration.alert.channel.api.convert;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;

public abstract class ProviderMessageConverter<T extends ProviderMessage<T>> {
    private final ChannelMessageFormatter messageFormatter;

    public ProviderMessageConverter(ChannelMessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    public abstract List<String> convertToFormattedMessageChunks(T message);

    protected String createLinkableItemString(LinkableItem linkableItem, boolean bold) {
        String name = messageFormatter.encode(linkableItem.getLabel());
        String value = messageFormatter.encode(linkableItem.getValue());
        Optional<String> optionalUrl = linkableItem.getUrl();

        if (bold) {
            name = messageFormatter.emphasize(name);
            value = messageFormatter.emphasize(value);
        }

        if (optionalUrl.isPresent()) {
            // The nuance around stylizing links adds too much complexity for too little value to worry about emphasizing them.
            value = createLinkableItemValueString(linkableItem);
        }

        return String.format("%s:%s%s", name, messageFormatter.getNonBreakingSpace(), value);
    }

    private String createLinkableItemValueString(LinkableItem linkableItem) {
        String value = messageFormatter.encode(linkableItem.getValue());
        Optional<String> optionalUrl = linkableItem.getUrl();

        String formattedString = value;
        if (optionalUrl.isPresent()) {
            String urlString = messageFormatter.encode(optionalUrl.get());
            formattedString = messageFormatter.createLink(value, urlString);
        }
        return formattedString;
    }

}
