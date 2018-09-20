<#macro printLink linkableItem>
    <#assign url = linkableItem.getUrl()/>
    <#if url.isPresent()>
        <a href="${url.get()}">${linkableItem.value}</a>
    <#else>
        ${linkableItem.value}
    </#if>
</#macro>

<#macro printLinkableItem linkableItem>
    ${linkableItem.name}: <@printLink linkableItem/>
</#macro>

<#macro printList lhs rhs>
    ${lhs}:
    <#if (rhs?size == 1) >
        <#assign firstItem = rhs?first/>
        <@printLink firstItem />
    <#else>
        <#list rhs as linkableItem>
            [<@printLink linkableItem/>]
        </#list>
    </#if>
</#macro>

<#macro printCategoryData categoryItem>
    <#if categoryItem??>
        <#assign linkableItemsMap = categoryItem.getItemsOfSameName()/>
        <br/>Type: ${categoryItem.operation}
        <!-- <br/>Number of Changes: ${linkableItemsMap?values?size} -->
        <br/>
        <#list linkableItemsMap as itemKey, linkableItems>
            <@printList itemKey, linkableItems/>
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