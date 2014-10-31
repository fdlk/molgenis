<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['joint.css','modelviz.css','trNgGrid.min.css']>
<#assign js=['jquery-1.11.1.min.js','angular.js','angular-animate.js','angular-touch.js', 'bootstrap.js',
'ui-bootstrap-tpls-0.11.2.js','joint.js','joint.layout.DirectedGraph.min.js','joint.shapes.uml.js',
'trNgGrid.min.js','modeldataControllers.js',
'modelvizControllers.js','guiDirectives.js','databaseServices.js','app.js']>
<@header css js/>
<h1>ModelData</h2>

<div ng-app="modelviz">

	<div class="main">

	    <div id="adminInfo" class="left_part" ng-controller="AdminInfoDataCtrl" >
			<h3>Models</h3> 
	        <table  tr-ng-grid='' items='data' page-items="10" 
	        class="table table-condensed table-hover border tableGrid" selection-mode="SingleRow" enable-selections="true" selected-items="selectedItems">
	          <thead>
	           <tr>
	            <th field-name="nameSpace" display-name="Napespace"  ></th>
	            <th field-name="model" cell-width="15em"></th>
	            <th field-name="stewardingOrganization" display-name="Organization" cell-width="15em">
	            </tr>
	          </thead>
	        </table>
	    </div>

	    <div id="packageInfo" class="right_part sample-show-hide" ng-init="hasData=false" ng-controller="PackageDataCtrl" ng-show="hasData" >
			<h3>Packages </h3>
	        <table  tr-ng-grid='' items='data' page-items="10" class="table table-condensed tableGrid" selection-mode="SingleRow" enable-selections="true"
	          selected-items="selectedItems">
	          <thead>
	           <tr>
	            <th field-name="name" cell-width="30em"></th>
	            <th field-name="entities" cell-width="5em"></th>
                <th cell-width="40em">
                    <div class="tr-ng-title">
                        Description
                    </div>
                </th>
	            </tr>	            
	          </thead>
	          <tbody>
	          	<tr>
	          		<td >
		          		<div ng-attr-title="{{gridItem.description}}">
		          			{{gridItem.description.substring(0,50)}} 
		          		</div>
	          		</td>
	          	</tr>	          	
	          </tbody>
	        </table>
	    </div>
	</div>
	
    <div id="entityInfo"  ng-init="hasData=false" ng-controller="EntityDataCtrl" ng-show="hasData" class="sample-show-hide">
		<h3>Selected namespace: {{namespaceIdentifier}} </h3>
		<h3>Entities</h3>
        <table  tr-ng-grid='' items='data' page-items="10" class="table table-condensed tableGrid" selection-mode="SingleRow" enable-selections="true" selected-items="selectedItems">
          <thead>
           <tr>
            <th field-name="identifier" display-format="truncate:gridItem" enable-filtering="false" enable-sorting="false" cell-width="10em"></th>
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
	   on-data-required-delay="100" 
	-->
    <div id="attributeInfo" ng-init="hasData=false" ng-controller="AttributeDataCtrl" ng-show="hasData" class="sample-show-hide">
		<h3>Entity: <em>{{entityName}}</em> - <a ng-href="/menu/main/modelviz/viewer?identifier={{entityIdentifier}}" target="_blank">UML</a></h3>
		<h3>Attributes and association ends</h3>
        <table  tr-ng-grid='' items='data' page-items="10" class="table table-condensed tableGrid" selection-mode="SingleRow" enable-selections="true" selected-items="selectedItems">
          <thead>
           <tr>
            <th field-name="identifier" display-format="truncate:gridItem" enable-filtering="false" enable-sorting="false" cell-width="10em"></th>
            <th field-name="name" cell-width="15em"></th>
            <th field-name="type" display-name="MetaType" cell-width="15em">
            <th field-name="typeName" display-name="Classifier" cell-width="15em"></th>
            <th field-name="typeType" display-name="CTypeName"></th>
            <th field-name="associationName" display-name="AssocName"></th>
            <th field-name="navigable"></th>
            <th field-name="aggregation" display-name="Aggregation"></th>
            <th field-name="lowerBound" display-name="Min" cell-width="4em"></th>
            <th field-name="upperBound" display-name="Max" cell-width="4em"></th>
            </tr>
          </thead>
        </table>
    </div>
            
</body>
	
</div>

<!-- @footer -->
                    </div><#-- close plugin-container -->
                </div><#-- close col-md-12 -->
            </div><#-- close row -->
        </div><#-- close container-fluid -->
    </body>
</html>
