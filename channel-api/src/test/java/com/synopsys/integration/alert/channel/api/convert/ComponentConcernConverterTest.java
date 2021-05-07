package com.synopsys.integration.alert.channel.api.convert;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;

public class ComponentConcernConverterTest {
    @Disabled
    @Test
    public void previewFormatting() {
        ChannelMessageFormatter channelMessageFormatter = createChannelMessageFormatter();
        ComponentConcernConverter componentConcernConverter = new ComponentConcernConverter(channelMessageFormatter);

        List<ComponentConcern> componentConcerns = createALotOfComponentConcerns();

        List<String> sectionPieces = componentConcernConverter.gatherComponentConcernSectionPieces(componentConcerns);
        String joinedSectionPieces = StringUtils.join(sectionPieces, "");
        System.out.print(joinedSectionPieces);
    }

    private List<ComponentConcern> createALotOfComponentConcerns() {
        return List.of(
            ComponentConcern.policy(ItemOperation.ADD, "Added Policy"),
            ComponentConcern.vulnerability(ItemOperation.ADD, "Added-Vuln01", ComponentConcernSeverity.CRITICAL, "https://synopsys.com"),
            ComponentConcern.policy(ItemOperation.ADD, "Added Another Policy"),
            ComponentConcern.vulnerability(ItemOperation.ADD, "Added-Vuln02", ComponentConcernSeverity.CRITICAL, "https://synopsys.com"),
            ComponentConcern.severePolicy(ItemOperation.ADD, "Added Severe Policy", ComponentConcernSeverity.TRIVIAL),
            ComponentConcern.vulnerability(ItemOperation.ADD, "Added-Vuln03", ComponentConcernSeverity.CRITICAL, "https://synopsys.com"),
            ComponentConcern.vulnerability(ItemOperation.UPDATE, "Updated-Vuln01", ComponentConcernSeverity.MAJOR, "https://synopsys.com"),
            ComponentConcern.severePolicy(ItemOperation.DELETE, "Removed Policy", ComponentConcernSeverity.MAJOR),
            ComponentConcern.vulnerability(ItemOperation.UPDATE, "Updated-Vuln02", ComponentConcernSeverity.MAJOR, "https://synopsys.com"),
            ComponentConcern.severePolicy(ItemOperation.DELETE, "Removed Another Policy", ComponentConcernSeverity.MAJOR),
            ComponentConcern.policy(ItemOperation.DELETE, "Removed Severe Policy"),
            ComponentConcern.vulnerability(ItemOperation.DELETE, "Removed-Vuln01", ComponentConcernSeverity.MINOR, "https://synopsys.com"),
            ComponentConcern.vulnerability(ItemOperation.DELETE, "Removed-Vuln02", ComponentConcernSeverity.MINOR, "https://synopsys.com")
        );
    }

    private ChannelMessageFormatter createChannelMessageFormatter() {
        return new ChannelMessageFormatter(Integer.MAX_VALUE, System.lineSeparator()) {

            @Override
            public String encode(String txt) {
                return txt;
            }

            @Override
            public String emphasize(String txt) {
                return "<!>" + txt + "</!>";
            }

            @Override
            public String createLink(String txt, String url) {
                return "<ln>" + txt + " - " + url + "</ln>";
            }
        };
    }

}
