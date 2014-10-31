<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">


<#assign css=['joint.css','modelviz.css','trNgGrid.min.css']>
<#assign js=[ 'jquery-1.11.1.min.js','angular.js','angular-animate.js','angular-touch.js', 'bootstrap.js','ui-bootstrap-tpls-0.11.2.js',
'joint.js','joint.layout.DirectedGraph.min.js','joint.shapes.uml.js','trNgGrid.min.js','modeldataControllers.js',
'modelvizControllers.js','guiDirectives.js','databaseServices.js','app.js']>

<@header css js/>


<div ng-app="modelviz">
	

	</p>
		<#if entityIdentifier??>
		<h3>ModelViz: {{name}}</h3>
		<em>{{description}}</em>
	    <script>
         app.identifier="${entityIdentifier}";
        </script>  	
        <#else>
	    <h2>Class identifier is missing</h2>	
	    <script>
         app.identifier="abcde";
        </script>  	        
        </#if>

    	<div  ng-init="identifier='${entityIdentifier}';neighbors='${neighbors}'" class="diagram" id="mydia" ng-controller="entityCtrl" ng-style="diagramstyle"  
	      ng-mousemove="myMouse.move($event)" ng-mousemove="myMouse.up($event)">
	      
		<div>
		  Entity: {{entity.identifier}} Dim: W:{{wWidth}}/{{testWidth}}xH:{{wHeight}}/{{testHeight}} Mouse: {{mLocation}} 
		</div>	
	</div>

</div>

<@footer/>