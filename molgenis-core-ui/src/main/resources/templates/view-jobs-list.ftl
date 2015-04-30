<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign js=['sockjs-0.3.4.js', 'stomp.js']>
<#assign css=[]>

<@header css js />

<h1>Jobs</h1>
<#list jobs.jobNames as jobName>
	<h2>${jobName}</h2>
	Last 10 job instances:
	<table class="table">
		<tr><th>jobId</th><th>params</th><th>created</th><th>start</th><th>end</th><th>duration(ms)</th></tr>
		<#list jobs.findJobInstancesByJobName(jobName, 0, 10) as job>
			<#list jobs.getJobExecutions(job) as execution>
				<tr>
					<td><#if execution.jobId??>${execution.jobId}</#if></td>
					<td><#if execution.jobParameters??>${execution.jobParameters}</#if></td>
					<td><#if execution.createTime??>${execution.createTime}</#if></td>
					<td><#if execution.startTime??>${execution.startTime}</#if></td>
					<td><#if execution.endTime??>${execution.endTime}</#if></td>
					<td><#if execution.endTime?? && execution.startTime??>${execution.endTime?long - execution.startTime?long}</#if></td>
				</tr>
			</#list>
		</#list>
	</table>
</#list>

<@footer/>
