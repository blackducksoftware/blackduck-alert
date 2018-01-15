<html>
    <body style="margin:1cm;width:620px;">
        <#macro displayCount type size> 
          <p class="bold indented">${size} ${type}</p>
        </#macro>
        <#macro moreItems size>
          <#if size gt 10>
                <p>${size - 10} more</p>
          </#if>
        </#macro>
        <div style="display:inline-block;width:100%;">
            <div>
              <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: bold;font-size: 24px;color: #4A4A4A;">BLACK</span><span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-size: 24px;color: #73B1F0;">DUCK</span>
            </div>
            <div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #445B68;float: right;">${emailCategory}</div>
        </div> 
        <div style="border: 1px solid #979797;"></div>
        <br/>
        <div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #445B68;">Black Duck captured the following new policy violations and vulnerabilities.</div>
        <a href="${hub_server_url}" style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #225786;">See more details in the Hub</a>
        <br/>
        <br/>
          <#if topic??> 
              <div style="background: #DDDDDD;margin:0px;padding-left: 15px;padding-top: 20px;padding-bottom: 20px;">
                <div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: bold;font-size: 18px;color: #445B68;margin-bottom: 10px;">${topic.projectName} > ${topic.projectVersion}</div>
                <#if topic.categoryMap?? && topic.categoryMap?size gt 0> 
                  <#list topic.categoryMap?values as categoryItem>
                    <#if categoryItem.itemList?? && categoryItem.itemList?size gt 0>
                        <#assign categoryType="${categoryItem.categoryKey}">
                        <#if categoryType == "POLICY_VIOLATION">
                          <#assign categoryName="Policy Violations">
                        <#elseif categoryType == "POLICY_VIOLATION_CLEARED">
                          <#assign categoryName="Policy Violations Cleared">
                        <#elseif categoryType == "POLICY_VIOLATION_OVERRIDE">
                          <#assign categoryName="Policy Violation Overrides">
                        <#elseif categoryType == "HIGH_VULNERABILITY">
                          <#assign categoryName="High Vulnerabilities">
                        <#elseif categoryType == "MEDIUM_VULNERABILITY">
                          <#assign categoryName="Medium Vulnerabilities">
                        <#elseif categoryType == "LOW_VULNERABILITY">
                          <#assign categoryName="Low Vulnerabilities">
                        <#else>
                          <#assign categoryName="${categoryItem.categoryKey}">
                        </#if>
                        <div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: bold;font-size: 14px;color: #445B68; margin-top: 10px; margin-bottom: 10px;">${categoryItem.itemCount} ${categoryName}</div>
                        <#list categoryItem.itemList as item>
                            <#if item.dataSet?? && item.dataSet?size gt 0>
                               <div>
                                   <div style="font-family: monospace;font-size: 14px;color: #445B68;padding-right: 15px;display: inline-block;">
                                   Component: ${item.dataSet.COMPONENT} 
                                   <#if item.dataSet.VERSION??>
                                     [${item.dataSet.VERSION}]
                                   </#if>
                                   <#if item.dataSet.RULE??>
                                     RULE: ${item.dataSet.RULE}
                                   </#if>
                                   <#if item.dataSet.ADDED??>
                                     Added (${item.dataSet.ADDED})
                                   </#if>
                                   <#if item.dataSet.UPDATED??>
                                     Updated (${item.dataSet.UPDATED})
                                   </#if>
                                   <#if item.dataSet.DELETED??>
                                     Deleted (${item.dataSet.DELETED})
                                   </#if>
                                   <#if item.dataSet.PERSON??>
                                     By (${item.dataSet.PERSON})
                                   </#if>
                                   </div>
                               </div>
                               <br/>
                            </#if>
                            <@moreItems item.dataSet?size/>
                        </#list>
                    </#if>
                  </#list>
                </#if>
              </div>
              <div style="height: 20px;"></div>
          </#if>
        <div style="display:inline-block;width:100%;">
            <img src="cid:${logo_image}" height="20" width="20"/>
            <div style="float:right;">
              <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-size: 12px;color: #4A4A4A;">Powered by </span><span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: 600;font-size: 14px;color: #445B68;">BLACK</span><span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-size: 12px;color: #4A4A4A;">DUCK</span>
            </div>
        </div>
    </body>
<html>