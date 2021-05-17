<html>
<body style="margin:1cm;width:620px;">
<#macro displayCount type size>
    <p class="bold indented">${size} ${type}</p>
</#macro>

<div style="display:inline-block;width:100%;">
    <div>
        <#if alertServerUrl?has_content>
            <a href="${alertServerUrl}" style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: bold;font-size: 24px;color: #4A4A4A;text-decoration: none">ALERT</a>
        <#else>
            <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: bold;font-size: 24px;color: #4A4A4A;">ALERT</span>
        </#if>
    </div>
    <div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #445B68;float: right;">${emailCategory}</div>
</div>
<div style="border: 1px solid #979797;"></div>
<br />
<#if provider_name?has_content>
    <div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #445B68;">${provider_name} captured the following new notifications.</div>
    <a href="${provider_url}" style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #225786;">See more details in the ${provider_name} server</a>
    <br />
    <br />
</#if>

<#if content?has_content>
    ${content}
<#else>
    <br /><i>A notification was received, but no information was defined.</i>
</#if>
<br />
<div style="border: 1px solid #979797;"></div>
<#if alertServerUrl?has_content>
    <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 10px;color: #4A4A4A;">Server: <a href="${alertServerUrl}" style="color: #225786;">${alertServerUrl}</a></span>
</#if>
<br />
<#if provider_project_name?has_content>
    <br />
    <div style="display:inline-block;width:100%;">
        <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-size: 10px;color: #4A4A4A;">You are receiving this email because you are assigned to project: ${provider_project_name}. If you would like to stop receiving this email, please contact your system administrator and have them remove you from the project.</span>
    </div>
</#if>
<#if alertServerUrl?has_content || provider_name?has_content>
    <br />
    <br />
</#if>
<div style="display:inline-block;width:100%;">
    <img src="cid:${logo_image}" height="33" width="150" />
</div>
<br />
</body>
</html>
