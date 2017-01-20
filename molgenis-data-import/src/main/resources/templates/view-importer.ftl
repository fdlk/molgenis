<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>

<@header css js/>
<#if importJobHref??>
<div id="job-container"></div>
<script>
    $(function () {
        React.render(new molgenis.ui.jobs.JobContainer({
            'jobHref': '${importJobHref}'
        }), $('#job-container')[0]);
    });
</script>
<#else>
<form action="${context_url?html}/import" method="POST" enctype="multipart/form-data">
    <input type="file" name="file">
    <button type="submit" class="btn btn-default">Import</button>
</form>
</#if>
<#if entityTypes??>
<h3>Imported entities</h3>
    <#list entityTypes as entityType>
    <p><a href="/menu/main/dataexplorer?entity=${entityType.name}">${entityType.label}</a></p>
    </#list>
</#if>
<@footer/>