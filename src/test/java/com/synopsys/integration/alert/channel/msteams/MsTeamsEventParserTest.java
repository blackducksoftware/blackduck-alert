package com.synopsys.integration.alert.channel.msteams;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.exception.IntegrationException;

public class MsTeamsEventParserTest {
    @Test
    public void testCreatingJson() throws IOException, IntegrationException, IllegalAccessException, JSONException {
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(new TestAlertProperties());
        MsTeamsEventParser msTeamsEventParser = new MsTeamsEventParser(freemarkerTemplatingService);

        ProviderMessageContent.Builder contentBuilder = new ProviderMessageContent.Builder();
        contentBuilder.applyProvider("Black Duck");
        contentBuilder.applyTopic("not used", "ek-alert-testing");
        contentBuilder.applySubTopic("not used", "1.0.0");

        ReusableComponentItemBuilder componentItemBuilder = new ReusableComponentItemBuilder();
        componentItemBuilder.applyCategory("Vulnerability");
        componentItemBuilder.applyOperation(ItemOperation.ADD);
        componentItemBuilder.applyComponentData("not used", "Apache Struts");
        componentItemBuilder.applySubComponent("not used", "1.2.2");
        componentItemBuilder.applyComponentAttribute(new LinkableItem("Component License", "Apache License 2.0"));
        componentItemBuilder.applyCollapseOnCategory(true);

        componentItemBuilder.applyCategoryGroupingAttribute("Severity", "HIGH");

        componentItemBuilder.applyCategoryItem("Vulnerabilities", "CVE-2016-0785");
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        componentItemBuilder.applyCategoryItem("Vulnerabilities", "CVE-2014-0114");
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        componentItemBuilder.applyCategoryItem("Vulnerabilities", "CVE-2006-1546");
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        componentItemBuilder.applyCategoryItem("Vulnerabilities", "CVE-2006-1547");
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        componentItemBuilder.applyCategoryGroupingAttribute("Severity", "MEDIUM");
        componentItemBuilder.applyCategoryItem("Vulnerabilities", "CVE-2012-0394");
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        componentItemBuilder.applyCategoryItem("Vulnerabilities", "CVE-2006-1548");
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        componentItemBuilder.applyCategoryItem("Vulnerabilities", "CVE-2015-0899");
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        componentItemBuilder.applyCategoryItem("Vulnerabilities", "CVE-2016-1182");
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        componentItemBuilder.applyCategoryItem("Vulnerabilities", "CVE-2016-1181");
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        componentItemBuilder.applyCategoryGroupingAttribute("Severity", "LOW");
        componentItemBuilder.applyCategoryItem("Vulnerabilities", "BDSA-2008-0016");
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        componentItemBuilder.clearAttributes();

        componentItemBuilder.applyOperation(ItemOperation.UPDATE);
        componentItemBuilder.applyComponentAttribute(new LinkableItem("Component License", "Apache License 2.0"));
        componentItemBuilder.applyCategoryItem("Vulnerabilities", "ALL");
        componentItemBuilder.applyCategoryGroupingAttribute("Severity", "LOW");
        componentItemBuilder.applyComponentAttribute(new LinkableItem("Remediation - Fixes Previous Vulnerabilities", "2"));
        componentItemBuilder.applyComponentAttribute(new LinkableItem("Remediation - Latest Version", "2.5.20"));
        componentItemBuilder.applyComponentAttribute(new LinkableItem("Remediation - Without Vulnerabilities", "2"));
        contentBuilder.applyComponentItem(componentItemBuilder.build());

        ProviderMessageContent providerMessageContent = contentBuilder.build();
        MessageContentGroup messageContentGroup = new MessageContentGroup();
        messageContentGroup.add(providerMessageContent);

        DistributionEvent distributionEvent = new DistributionEvent("", "", "", "", "", messageContentGroup, null);
        MsTeamsMessage msTeamsMessage = msTeamsEventParser.createMessage(distributionEvent);
        String json = msTeamsEventParser.toJson(msTeamsMessage);
        assertNotNull(json);

        String expectedJson = IOUtils.toString(getClass().getResourceAsStream("/msteams/do_not_edit_in_intellij_line_endings_matter_expected.json"), StandardCharsets.UTF_8);

        // remove all possible windows style line endings
        json = json.replace("\r", "");
        expectedJson = expectedJson.replace("\r", "");

        JSONObject actual = new JSONObject(json);
        JSONObject expected = new JSONObject(expectedJson);
        JSONAssert.assertEquals(expected, actual, true);
    }

    public class ReusableComponentItemBuilder extends ComponentItem.Builder {
        public ReusableComponentItemBuilder() {
            applyNotificationId(Long.MIN_VALUE);
        }

        public void clearAttributes() throws IllegalAccessException {
            Field componentAttributes = FieldUtils.getField(this.getClass(), "componentAttributes", true);
            FieldUtils.writeField(componentAttributes, this, new LinkedHashSet<LinkableItem>(), true);
        }
    }

}
