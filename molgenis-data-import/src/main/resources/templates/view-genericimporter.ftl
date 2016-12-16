<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>

<@header css js/>
<form action="${context_url?html}/import" method="POST" enctype="multipart/form-data">
    <input type="file" name="file"/>
    <input type="submit" value="Import"/>
</form>
<#if entityTypes??>
<h3>Imported entities</h3>
    <#list entityTypes as entityType>
    <p><a href="/menu/main/dataexplorer?entity=${entityType.name}">${entityType.label}</a></p>
    </#list>
</#if>
<@footer/>