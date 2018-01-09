<#macro printCategoryData categoryItem>
    <#if categoryItem??>
        <br />- - - - - - - - - - - - - - - - - - - -
        <br />Type: ${categoryItem.categoryKey}
        <br />Number of Changes: ${categoryItem.itemCount}
        <br />
        <#list categoryItem.itemList as item>
            <#if item.dataSet?? && item.dataSet?size gt 0>
                <#if item.dataSet.RULE??>
                    <br /> Rule: ${item.dataSet.RULE}
                </#if>
                <#if item.dataSet.COUNT??>
                    <br /> Vulnerability Count: ${item.dataSet.COUNT}
                </#if>
                <br /> Component: ${item.dataSet.COMPONENT} [${item.dataSet.VERSION}]
            </#if>
            <br />
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