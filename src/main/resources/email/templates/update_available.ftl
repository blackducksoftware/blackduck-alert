<html>
<body style="margin:1cm;width:620px;">
<div style="display:inline-block;width:100%;">
    <div>
        <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: bold;font-size: 24px;color: #4A4A4A;">ALERT</span>
    </div>
    <div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #445B68;float: right;">Update Available</div>
</div>
<div style="border: 1px solid #979797;"></div>
<br />
A new version of Alert is available:
<br />
<a href="${repositoryUrl}">${newVersionName}</a>
<br />
<br />
<div style="border: 1px solid #979797;"></div>
<#if alertServerUrl??>
    Server: <a href="${alertServerUrl}" style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 12px;color: #225786;">${alertServerUrl}</a>
</#if>
<br />
<br />
<div style="display:inline-block;width:100%;">
    <img src="cid:${logo_image}" height="33" width="150" />
</div>
</body>
</html>
