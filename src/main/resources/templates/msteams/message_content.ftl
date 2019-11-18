{
"@type": "MessageCard",
"@context": "https:\/\/schema.org\/extensions",
"summary": "New Content from Alert",
"themeColor": "5A2A82",
"title": "${title}",
"sections": [
<#list sections as section>
    {
    "startGroup": true,
    "title": "${topic?json_string}<#if section.subTopic??>${section.subTopic?json_string}</#if>",
    "text": "<#list section.componentsMessage as component>${component?json_string}</#list>"
    }<#sep>, </#sep>
</#list>
]
}
