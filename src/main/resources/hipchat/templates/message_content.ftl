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

<#macro printComponentData componentItem>
    <br />
    <#if componentItem??>
        Category: ${componentItem.getCategory()}
        <br />
        Operation: ${componentItem.getOperation()}
        <br />

        <@printLinkableItem componentItem.getComponent()/>
        <br />
        <#assign subComponent = componentItem.getSubComponent()/>
        <#if subComponent.isPresent() >
            <@printLinkableItem subComponent.get()/>
            <br />
        </#if>

        <#assign linkableItemsMap = componentItem.getItemsOfSameName()/>
        <#list linkableItemsMap as itemKey, linkableItems>
            <@printList itemKey, linkableItems/>
            <br />
        </#list>
    </#if>
</#macro>

<#if content??>
    <#if content.commonTopic??>
        <strong>
            <@printLinkableItem content.commonTopic/>
        </strong>
        <#list content.subContent as providerMessageContent>
            <strong>
                <#if providerMessageContent.subTopic.isPresent()>
                    <br />
                    <@printLinkableItem providerMessageContent.subTopic.get()/>
                </#if>
            </strong>
            <br />- - - - - - - - - - - - - - - - - - - -
            <#if providerMessageContent.componentItems??>
                <#list providerMessageContent.componentItems as componentItem>
                    <@printComponentData componentItem/>
                </#list>
            <#else>
                <br /><i>A notification was received, but it was empty.</i>
            </#if>
        </#list>
    <#else>
        <br /><i>A notification was received, but no information was defined.</i>
    </#if>
<#else>
    <br /><i>A notification was received, but no information was defined.</i>
</#if>
