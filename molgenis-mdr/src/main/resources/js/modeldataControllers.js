angular.module('modeldataControllers', []);

angular.module('modeldataControllers', []).filter("truncate", function () {
    return function (fieldValueUnused, item) {
     return item.identifier.substring(1,15)+"...";
   };
 })

angular.module('modeldataControllers').controller(
		'AdminInfoDataCtrl', ['$scope','entityAPIservice','modelService',
		function($scope, api, model) {
			var self = this;
			console.log("Called...");
			$scope.test = "HEY";
			$scope.data = {};
			$scope.selectedItems = [];
			$scope.$watch('selectedItems[0]', function(value) {
				if (value !== undefined) {
					model.updateAdminInfoData ( value);
					console.log("Admin Info: " +value);
				}
			});
			api.getAdminInfo().success(function(response) {
				debug = response;
				$scope.data = response;
			});
		}]);

angular.module('modeldataControllers').controller(
		'PackageDataCtrl', ['$scope','entityAPIservice','modelService',
		function($scope, api,model) {
			var self = this;
			console.log("PackageDataCtrl Called...");
			$scope.data = {};
			if ( $scope.data === undefined || $scope.data.length == 0) {
				$scope.hasData = false;				
			} else {
				$scope.hasData = true;								
			}
			$scope.selectedItems = [];
			$scope.$watch('selectedItems[0]', function(value) {
				if (value !== undefined) {
					console.log(value.identifier);
//					window.open(
//							encodeURI("/menu/main/modelviz/viewer?identifier="+ value.identifier), "_self")
				}
			});
			
		    $scope.$watch(model.refAdminInfoData, function(newValue) {
		    		console.log("Trying to get packages. AdminInfo changed: : "+newValue);
			        if ( newValue !== undefined  && newValue.nameSpace !== undefined) {
			                var ids = [newValue.nameSpace];
			                $scope.namespaceIdentifier = newValue.nameSpace;
			    			api.getPackagesByNamespaceIds( ids).success(function(response,status, headers, config) {
			    				debug = response;
			    				$scope.data = response;
			    				$scope.packages = response.length;
			    				$scope.hasData = true;
			    				console.log("Got packages")
			    			}).error( function (response,status, headers, config) {
			    				console.log("Error "+status)
			    			}) ;
			        }
		    });
		    
		}]);

angular.module('modeldataControllers').controller(
		'EntityDataCtrl', ['$scope','entityAPIservice','modelService',
		function($scope, api,model) {
			var self = this;
			console.log("Called...");
			$scope.test = "HEY";
			$scope.data = {};
			if ( $scope.data === undefined || $scope.data.length == 0) {
				$scope.hasData = false;				
			} else {
				$scope.hasData = true;								
			}
			$scope.selectedItems = [];
			$scope.$watch('selectedItems[0]', function(value) {
				if (value !== undefined) {
					console.log(value.identifier);
					model.updateEntityInfoData ( value);					
//					window.open(
//							encodeURI("/menu/main/modelviz/viewer?identifier="+ value.identifier), "_self")
				}
			});
			$scope.$watch ( $scope.undefMinValue , function(value) {
				if ( $scope.fullData !== undefined ) {
					$scope.data = $scope.fullData.slice(1,5)	
				}	
			});
			
		    $scope.$watch(model.refAdminInfoData, function(newValue) {
			        if ( newValue !== undefined  && newValue.nameSpace !== undefined) {
			                console.log("admin info data has changed: "+newValue.nameSpace);
			                var ids = [newValue.nameSpace ];
			                $scope.namespaceIdentifier = newValue.nameSpace;
			    			api.getEntitiesByNamespaceIds( ids).success(function(response,status, headers, config) {
			    				debug = response;
			    				model.updateEntityInfoData ( response);
			    				$scope.data = response;
			    				if ( response.length > 0 ) {
			    					$scope.hasData = true;
			    				}
			    				console.log("Got entities")
			    			}).error( function (response,status, headers, config) {
			    				console.log("Error "+status)
			    			}) ;
			        }
		    });
		    
		}]);

angular.module('modeldataControllers').controller(
		'AttributeDataCtrl', ['$scope','entityAPIservice','modelService',
		function($scope, api,model) {
			var self = this;
			console.log("Called...");
			$scope.data = {};
			if ( $scope.data === undefined || $scope.data.length == 0) {
				$scope.hasData = false;				
			} else {
				$scope.hasData = true;								
			}
			$scope.onServerSideItemsRequested = function(currentPage, pageItems, filterBy, filterByFields, orderBy, orderByReverse) {
				console.log(filterBy);				
				console.log(filterByFields);
			}
			$scope.selectedItems = [];
			$scope.$watch('selectedItems[0]', function(value) {
				if (value !== undefined) {
					console.log(value.identifier);
//					window.open(
//							encodeURI("/menu/main/modelviz/viewer?identifier="+ value.identifier), "_self")
				}
			});
			
		    $scope.$watch(model.refEntityInfoData, function(newValue) {
			        if ( newValue !== undefined  && newValue.identifier !== undefined) {
			                console.log("Selected entity has changed: "+newValue.identifier);
			                var ids = [newValue.identifier ];
			                $scope.entityName = newValue.name;
			                $scope.entityIdentifier = newValue.identifier;
			    			api.getAttributesByEntityIds( ids).success(function(response,status, headers, config) {
			    				debug = response;
			    				$scope.data = response;
			    				$scope.attributes = response.length;
			    				$scope.hasData = true;
			    				console.log("Got attributes")
			    			}).error( function (response,status, headers, config) {
			    				console.log("Error "+status)
			    			}) ;
			        }
		    });
		    
		}]);


