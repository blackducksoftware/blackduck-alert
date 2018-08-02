<html>
<body style="margin:1cm;width:620px;">
<div style="display:inline-block;width:100%;">
    <div>
        <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: bold;font-size: 24px;color: #4A4A4A;">BLACK</span><span
            style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-size: 24px;color: #73B1F0;">DUCK</span>
    </div>
</div>
<div style="border: 1px solid #979797;"></div>
<br/>
<div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #445B68;">Black Duck captured the following new policy violations and vulnerabilities.</div>
<a href="${blackduck_server_url}" style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: lighter;font-size: 14px;color: #225786;">See more details in the Black Duck server</a>
<br/>
<br/>
            <#if content??>
                <br/>
                <strong> ${contentTitle} </strong>
                <br/>- - - - - - - - - - - - - - - - - - - -
                ${content}
                <br/>
            <#else>
                <br/><i>A notification was received, but no project data defined.</i>
            </#if>
<div style="display:inline-block;width:100%;">
    <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-size: 10px;color: #4A4A4A;">You are receiving this email because you are part of the group: ${blackduck_group_name}. If you would like to stop receiving this email, please contact your system administrator and have them remove you from the group.</span>
</div>
<br/>
<div style="display:inline-block;width:100%;">
    <img src="cid:${logo_image}" height="20" width="20"/>
    <div style="float:right;">
        <span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-size: 12px;color: #4A4A4A;">Powered by </span><span style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-weight: 600;font-size: 14px;color: #445B68;">BLACK</span><span
            style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-size: 12px;color: #4A4A4A;">DUCK</span>
    </div>
</div>
</body>
</html>
