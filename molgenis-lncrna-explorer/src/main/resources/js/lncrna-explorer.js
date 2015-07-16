(function($, molgenis) {

	
//	alert('test');
	
	$(function() {
	
	$('#submit-input').on('click', function() {
		
		var submittedValue = $('#search-input').val();
//		alert(submittedValue);

		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + "/validate",
			contentType : 'application/json',
			data : JSON.stringify(submittedValue),
			success : 
				function( data) {

                     $("#ajaxResponse").html("");
                     $("#ajaxResponse").append("" + data + "");
				}
		
			})
	})
	})
	
}($, window.top.molgenis = window.top.molgenis || {}));