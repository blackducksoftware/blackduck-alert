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
        <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: bold;font-size: 24px;color: #4A4A4A;">ALERT</span>
    </div>
    <div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #445B68;float: right;">${emailCategory}</div>
</div>
<div style="border: 1px solid #979797;"></div>
<br />
<div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #445B68;">${provider_name} captured the following new policy violations and vulnerabilities.</div>
<a href="${provider_url}" style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #225786;">See more details in the ${provider_name} server</a>
<br />
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
        <#if categoryItem.operation?? >
            <br />Type: ${categoryItem.operation}
            <!-- <br/>Number of Changes: ${linkableItemsMap?values?size} -->
            <br />
        <#else>
            <br />
        </#if>
        <#list linkableItemsMap as itemKey, linkableItems>
            <@printList itemKey, linkableItems/>
            <br />
        </#list>
    </#if>
</#macro>

<#if content??>
    <strong>
        <@printLinkableItem content/>
        <#if content.subTopic.isPresent()>
            <br />
            <@printLinkableItem content.subTopic.get()/>
        </#if>
    </strong>
    <br />- - - - - - - - - - - - - - - - - - - -
    <#if content.categoryItemList??>
        <#list content.categoryItemList as categoryItem>
            <@printCategoryData categoryItem/>
        </#list>
    <#else>
        <br /><i>A notification was received, but it was empty.</i>
    </#if>
<#else>
    <br /><i>A notification was received, but no information was defined.</i>
</#if>
<br />
<div style="border: 1px solid #979797;"></div>
<br />
<div style="display:inline-block;width:100%;">
    <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-size: 10px;color: #4A4A4A;">You are receiving this email because you are assigned to project: ${provider_project_name}. If you would like to stop receiving this email, please contact your system administrator and have them remove you from the project.</span>
</div>
<br />
<div style="display:inline-block;width:100%;">
    <img src="cid:${logo_image}" height="33" width="150" />
</div>
<br />
</body>
<html>
