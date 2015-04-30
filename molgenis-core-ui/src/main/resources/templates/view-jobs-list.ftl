<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign js=['sockjs-0.3.4.js', 'stomp.js']>
<#assign css=[]>

<@header css js />

Executions:
<#list executions as execution>
	<div class="row">
		<div class="col-md-1">	
			<p><#if execution.jobId??>${execution.jobId}</#if></p>
		</div>
		<div class="col-md-1">	
			<p><#if execution.jobParameters??>${execution.jobParameters}</#if></p>
		</div>
		<div class="col-md-1">	
			<p><#if execution.createTime??>${execution.createTime}</#if></p>
		</div>
		<div class="col-md-1">	
			<p><#if execution.startTime??>${execution.startTime}</#if></p>
		</div>
		<div class="col-md-1">	
			<p><#if execution.endTime??>${execution.endTime}</#if></p>
		</div>
	</div>
</#list>

<@footer/>
