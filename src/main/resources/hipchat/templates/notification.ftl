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
                <#if item.dataSet.ADDED??>
                    <br /> Vulnerability Count Added: ${item.dataSet.ADDED}
                </#if>
                <#if item.dataSet.UPDATED??>
                    <br /> Vulnerability Count Updated : ${item.dataSet.UPDATED}
                </#if>
                <#if item.dataSet.DELETED??>
                    <br /> Vulnerability Count Deleted: ${item.dataSet.DELETED}
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