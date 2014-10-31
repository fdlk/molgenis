<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['modelviz/lib/joint.css','modelviz/lib/bootstrap.css' ,'modelviz/css/modelviz.css','modelviz/lib/trNgGrid.min.css']>
<#assign js=[
'modelviz/lib/jquery-1.11.1.min.js','modelviz/lib/bootstrap.js',
'modelviz/lib/angular.js','modelviz/lib/angular-animate.js',
'modelviz/lib/angular-touch.js','modelviz/lib/ui-bootstrap-tpls-0.11.2.js',
'modelviz/lib/joint.js','modelviz/lib/joint.layout.DirectedGraph.min.js',
'modelviz/lib/joint.shapes.uml.js','modelviz/lib/trNgGrid.min.js',
'modelviz/controllers/modeldataControllers.js','modelviz/controllers/modelvizControllers.js',
'modelviz/directives/guiDirectives.js','modelviz/services/databaseServices.js','modelviz/app.js']>

<@header css js/>


<h1>ModelData</h2>

<div ng-app="modelviz">

    <div id="adminInfo" ng-controller="AdminInfoDataCtrl" >
		<h3>Models</h3> 
        <table  tr-ng-grid='' items='data' page-items="10" 
        class="table table-condensed table-hover border tableGrid" selection-mode="SingleRow" enable-selections="true" selected-items="selectedItems">
          <thead>
           <tr>
            <th field-name="nameSpace" display-name="Napespace"  ></th>
            <th field-name="model"></th>
            <th field-name="stewardingOrganization" display-name="Organization">
            </tr>
          </thead>
        </table>
    </div>

    <div id="entityInfo" ng-init="hasData=false" ng-controller="EntityDataCtrl" ng-show="hasData">
		<h3>Entities </h3>
		<h4>Name space: {{namespaceIdentifier}} </h4>
        <table  tr-ng-grid='' items='data' page-items="10" class="table table-condensed tableGrid" selection-mode="SingleRow" enable-selections="true" selected-items="selectedItems">
          <thead>
           <tr>
            <th field-name="identifier" display-format="truncate:gridItem" enable-filtering="false" enable-sorting="false" cell-width="6em"></th>
            <th field-name="undefs" display-name="Mis." cell-width="6em" enable-filtering="false">
            </th>
            <th field-name="name"></th>
            <th field-name="type">
            <th field-name="typeQualifier" display-name="Qualifier"></th>
            <th field-name="description"></th>
            <th field-name="packageName" display-name="Package"></th>
            </tr>
          </thead>
        </table>
    </div>

	<!--
	page-items="myPageItemsCount" total-items="myItemsTotalCount" on-data-required="onServerSideItemsRequested(currentPage, pageItems, filterBy, filterByFields, orderBy, orderByReverse)
	-->
    <div id="attributeInfo" ng-init="hasData=false" ng-controller="AttributeDataCtrl" ng-show="hasData">
		<h3>Attributes</h3>
		<h4>Entity: <em>{{entityName}}</em> <a ng-href="/menu/main/modelviz/viewer?identifier={{entityName}}" target="_blank">UML</a></h4>
        <table  tr-ng-grid='' items='data' page-items="10" class="table table-condensed tableGrid" selection-mode="SingleRow" enable-selections="true" selected-items="selectedItems"
        page-items="myPageItemsCount" total-items="myItemsTotalCount" on-data-required="onServerSideItemsRequested(currentPage, pageItems, filterBy, filterByFields, orderBy, orderByReverse)"
        on-data-required-delay="100" 
        >
          <thead>
           <tr>
            <th field-name="identifier" display-format="truncate:gridItem" enable-filtering="false" enable-sorting="false" cell-width="6em"></th>
            <th field-name="name"></th>
            <th field-name="type" display-name="MetaType">
            <th field-name="typeName" display-name="Classifier"></th>
            <th field-name="typeType" display-name="CTypeName"></th>
            <th field-name="associationName" display-name="AssocName"></th>
            <th field-name="navigable"></th>
            <th field-name="aggregation" display-name="Aggregation"></th>
            <th field-name="lowerBound" display-name="Min"></th>
            <th field-name="upperBound" display-name="Max"></th>
            </tr>
          </thead>
        </table>
    </div>
       
     <br/>
     <br/>
            
</body>
	
</div>


<@footer/>