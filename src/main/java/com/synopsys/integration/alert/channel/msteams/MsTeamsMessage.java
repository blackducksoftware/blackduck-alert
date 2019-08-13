package com.synopsys.integration.alert.channel.msteams;

import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MsTeamsMessage {
    private Set<LinkableItem> providers = new HashSet<>();
    private List<MsTeamsSection> sections = new ArrayList<>();

    public void addContent(ProviderMessageContent providerMessageContent) {
        providers.add(providerMessageContent.getProvider());

        MsTeamsSection msTeamsSection = new MsTeamsSection();
        msTeamsSection.setProvider(providerMessageContent.getProvider().getValue());
        msTeamsSection.setTopic(providerMessageContent.getTopic().getValue());
        providerMessageContent.getSubTopic()
                .map(LinkableItem::getValue)
                .ifPresent(msTeamsSection::setSubTopic);
        sections.add(msTeamsSection);

        List<MsTeamsComponent> components = new ArrayList<>(providerMessageContent.getComponentItems().size());
        for (ComponentItem componentItem : providerMessageContent.getComponentItems()) {
            MsTeamsComponent msTeamsComponent = new MsTeamsComponent();
            msTeamsComponent.setCategory(componentItem.getCategory());
            msTeamsComponent.setOperation(componentItem.getOperation().toString());
            msTeamsComponent.setText(componentItem.getComponent().getValue());

            String details = createDetails(componentItem.getComponentAttributes());
            msTeamsComponent.setDetails(details);

            components.add(msTeamsComponent);
        }
        msTeamsSection.setComponents(components);
    }

    private String createDetails(Set<LinkableItem> componentAttributes) {
        return componentAttributes
                .stream()
                .map(linkableItem -> String.format("%s: %s", linkableItem.getName(), linkableItem.getValue()))
                .collect(Collectors.joining(", "));
    }

    public int getProviderCount() {
        return providers.size();
    }

    public List<MsTeamsSection> getSections() {
        return sections;
    }

}
