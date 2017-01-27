<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>

<@header css js/>
<#if importJobHref??>
<div id="job-container"></div>
<div id="job-result"></div>
<script>
    var onCompletionTriggered = false;
    $(function () {
        React.render(new molgenis.ui.jobs.JobContainer({
            'jobHref': '${importJobHref}',
            onCompletion: function (job) {
                if (onCompletionTriggered === false) {
                    var str = '<h3>Imported entities:</h3><br>';
                    var tokens = job.entityTypes.split(",");
                    for (var i = 0; i < tokens.length; ++i) {
                        var val = tokens[i].split(":");

                        str += '<a href="/menu/main/dataexplorer?entity=' + val[0] + '">' + val[1] + "</a><br>";
                    }
                    $('#job-result').html(str);
                    console.log(job);
                    onCompletionTriggered = true;
                }
            }
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