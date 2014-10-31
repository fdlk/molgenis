angular.module('modelvizServices',[]).factory('attributeAPIservice', function ($http) {
  
	var API = {};
	
	API.getAttribute = function( id ) {
        return $http.get('/attribute',{params: {'identifier': id }})
	}

	return API
}).factory('entityAPIservice', function ($http) {
  
	var API = {};
	var paramList = function(name,values) { var r=[]; for  ( i=0; i<values.length ;i++) { r.push( {name: values[i]}) ;} return r; };
	
	API.getEntity = function( id, n ) {
        return $http.get('/plugin/modeldata/entity',{params: {'identifier': id ,'neighbours':n}})
	}

	API.getEntities = function(  ) {
        return $http.get('/plugin/modeldata/entities')
	}

	API.getEntitiesByPackageIds = function( ids  ) {		
        return $http.get('/plugin/modeldata/entities',{params: {'packageIdentifier': ids }});
	}
	
	API.getEntitiesByNamespaceIds = function( ids  ) {
        return $http.get('/plugin/modeldata/entities',{params: {'namespaceIdentifier': ids }});
	}

	API.getPackagesByNamespaceIds = function( ids  ) {
        return $http.get('/plugin/modeldata/packages',{params: {'namespaceIdentifier': ids }});
	}

	API.getAttributesByEntityIds = function( ids  ) {
        return $http.get('/plugin/modeldata/attributes',{params: {'entityIdentifier': ids }});
	}

	API.getAdminInfo = function(  ) {
        return $http.get('/plugin/modeldata/administratedModels')
	}
	
	return API
	
}).factory('modelService', function ($http) {
  
	var API = {};
	
	/*
	 * Rows selected on tables:
	 */
	var _adminInfoData = {};
	var _entityInfoData = {};
	var _attributeInfoData = {};
	var _packageInfoData = {};
	
	
	API.refAdminInfoData = function(  ) {
        return _adminInfoData ;
	}
	API.updateAdminInfoData = function(  data) {
        _adminInfoData  = data;
	}
	
	API.refEntityInfoData = function(  ) {
        return _entityInfoData ;
	}
	API.updateEntityInfoData = function(  data) {
        _entityInfoData  = data;
	}

	API.refAttributeInfoData = function(  ) {
        return _attributeInfoData ;
	}
	API.updateAttributeInfoData = function(  data) {
        _attributeInfoData  = data;
	}

	API.refPackageInfoData = function(  ) {
        return _packageInfoData ;
	}
	API.updatePackageInfoData = function(  data) {
        _packageInfoData  = data;
	}
	
	return API
});