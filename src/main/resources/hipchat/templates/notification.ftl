<#macro printCategoryData categoryItem>
    <#if categoryItem??>
        <br />- - - - - - - - - - - - - - - - - - - -
        <br />Type: ${categoryItem.categoryKey}
        <br />Number of Changes: ${categoryItem.itemCount}
        <#list categoryItem.itemList as item>
            <#if item.dataSet?? && item.dataSet?size gt 0>
                <p>
                    <#if item.dataSet.COUNT??>
                        Vulnerability Count: ${item.dataSet.COUNT}
                    </#if>
                    <#if item.dataSet.RULE??>
                        Rule: ${item.dataSet.RULE}
                    </#if>
                </p>
                <p>  Component: ${item.dataSet.COMPONENT} [${item.dataSet.VERSION}]</p>
            </#if>
        </#list>
    </#if>
</#macro>


<strong> ${projectName} > ${projectVersion} </strong>
<#if categoryMap??>
    <#list categoryMap?values as categoryItem>
        <@printCategoryData categoryItem/>
    </#list>
<#else>
    <br /><i>A notification was received, but it was empty.</i>
</#if>