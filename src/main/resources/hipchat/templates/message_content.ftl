<#macro printLinkableItem linkableItem>
    <#assign url = linkableItem.getUrl()>
    <#if url.isPresent()>
        ${linkableItem.name}: <a href="${url.get()}">${linkableItem.value}</a>
    <#else>
        ${linkableItem.name}: ${linkableItem.value}
    </#if>
</#macro>

<#macro printCategoryData categoryItem>
    <#if categoryItem?? && categoryItem.itemList??>
        <br/>Type: ${categoryItem.operation}
        <br/>Number of Changes: ${categoryItem.itemList?size}
        <br/>
        <#list categoryItem.itemList as item>
            <@printLinkableItem item/>
            <br/>
        </#list>
    </#if>
</#macro>

<#if content??>
    <strong>
        <@printLinkableItem content/>
        <#if content.subTopic.isPresent()>
            <br/>
            <@printLinkableItem content.subTopic.get()/>
        </#if>
    </strong>
    <br/>- - - - - - - - - - - - - - - - - - - -
    <#if content.categoryItemList??>
        <#list content.categoryItemList as categoryItem>
            <@printCategoryData categoryItem/>
        </#list>
    <#else>
		    <br/><i>A notification was received, but it was empty.</i>
    </#if>
<#else>
	<br/><i>A notification was received, but no information was defined.</i>
</#if>