(function($, molgenis) {

	$(function() {

		$('#submit-input').on('click', function() {
			submitUserInput()

		})

		$('#search-input').keypress(function(e) {
			if (e.which == 13) {
				submitUserInput()
			}

		})

		$('#myModal').on('shown.bs.modal', function() {
			$('#myInput').focus()
		})
		
		$("#pop").on("click", function() {
			   $('#imagepreview').attr('src', $('#imageresource').attr('src')); // here asign the image to the modal when the user click the enlarge link
			   $('#imagemodal').modal('show'); // imagemodal is the id attribute assigned to the bootstrap modal, then i use the show function
			});

	})

	function submitUserInput(submittedGene) {

		var submittedValue = $('#search-input').val();
		var failed = '';
		var success = '';

		$
				.ajax({
					type : 'POST',
					url : molgenis.getContextUrl() + "/validate",
					contentType : 'application/json',
					data : JSON.stringify(submittedValue),
					success : function(data) {

						if (data.search('Fail') != -1) {
							var genes = data.split(",");
							for (el in genes) {
								if (genes[el].search('Fail') != -1) {
									console.log(genes[el].split(':'));
									failed += genes[el].split(':')[1] + ' ';
								}

							}

							molgenis.createAlert([ {
								message : 'Could not find gene(s) with gene name: ' + failed + ''
							} ], 'warning');

						} else {
							var genes = data.split(",");
							for (el in genes) {
								success += genes[el].split(':')[1] + ',';
							}
							console.log(success);
							$("#ajaxResponse").html("");
							$("#ajaxResponse")
									.append(
											""
												+ '<div class="col-sm-6 col-md-4"> <div class="thumbnail">'
												+ '<a href="#" id="pop"> <img id="imageresource" class="thumbnail" src="http://localhost:8080/scripts/generateExpression%28rpkm%29Heatmap/run?genes=' + success + '"></a>'
												+ '<div class="caption"> <h3>Thumbnail label</h3> <p>...</p> <p>'
												+ '<a href="#" class="btn btn-primary" role="button">Button</a> <a href="#" class="btn btn-default" role="button">Button</a></p>'
												+ '</div> </div> </div>');
							
							
//							$("#ajaxResponse")
//									.append(
//											""
//													+ '<div role="tabpanel" class="col-md-12 col-md-offset-1"><ul class="nav nav-tabs" role="tablist"><li role="tab" class="active"><a href="#expression" aria-controls="expression" role="tab" data-toggle="tab">Expression Data</a></li><li role="tab"><a href="#test" aria-controls="test" role="tab" data-toggle="tab">Test</a></li></ul><div class="tab-content"><div class="tab-pane active" id="expression">'
//													+ '<img src="http://localhost:8080/scripts/generateExpression%28rpkm%29Heatmap/run?genes=' + success + '">'
//													+ '<a data-toggle="modal" data-target="#myModal"><span id="info" class="glyphicon glyphicon-info-sign" aria-hidden="true" style="font-size:1.5em;"></span> </a>'
//													+ '</div><div class="tab-pane" id="test">Test test test</div></div>' + "")

						}
					}

				})

	}

}($, window.top.molgenis = window.top.molgenis || {}));