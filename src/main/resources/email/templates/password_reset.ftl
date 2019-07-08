<html>
<body style="margin:1cm;width:620px;">
<div style="display:inline-block;width:100%;">
    <div>
        <#if alertServerUrl??>
            <a href="${alertServerUrl}" style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: bold;font-size: 24px;color: #4A4A4A;text-decoration: none">ALERT</a>
        <#else>
            <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: bold;font-size: 24px;color: #4A4A4A;">ALERT</span>
        </#if>
    </div>
    <div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #445B68;float: right;">Password Reset</div>
</div>
<div style="border: 1px solid #979797;"></div>
<br />
<strong>Temporary Password: </strong> ${tempPassword}
<br />
<br />
<small>For your security, please change your password immediately.</small>
<br />
<div style="border: 1px solid #979797;"></div>
<#if alertServerUrl??>
    <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 10px;color: #4A4A4A;">Server: <a href="${alertServerUrl}" style="color: #225786;">${alertServerUrl}</a></span>
</#if>
<br />
<br />
<div style="display:inline-block;width:100%;">
    <img src="cid:${logo_image}" height="33" width="150" />
</div>
</body>
</html>
