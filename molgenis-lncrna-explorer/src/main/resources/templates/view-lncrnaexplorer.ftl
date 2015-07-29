<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['lncrna-explorer.css']>
<#assign js=['lncrna-explorer.js']>

<@header css js/>

<div class="row">
	<div class="col-md-12">
	    <h1> LncRNA Explorer </h1>
	</div>
</div>



<div class="row">
	<div class="col-md-4 col-md-offset-4">

    	<div class="input-group">
		
      		<input type="text" class="form-control" name="searchTerm" id="search-input" placeholder="GeneName">	
		<#--<i class="glyphicon glyphicon-search form-control-feedback" aria-hidden="true"></i>-->
      		<span class="input-group-btn">
      	 		<button type="btn" class="btn btn-default" id="submit-input">Search</button>
     	 	</span>
   	 	</div><!-- /input-group -->
	</div><!-- /.col-lg-6 -->
</div>

<#--<div class="row">
	<div class="col-md-12">
		<p>
			Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec interdum velit sed est vulputate pellentesque. Donec a tempus ex. 
			Aenean vel porta est. Nullam ultricies, leo porttitor mattis euismod, enim erat blandit elit, id fringilla massa purus nec neque. 
			Quisque laoreet quam eget enim fermentum fringilla. Maecenas eu suscipit massa. Aliquam aliquam pellentesque risus. Pellentesque 
			ante orci, ullamcorper ac leo vel, sodales ullamcorper lectus. Nulla luctus sit amet mauris vel vestibulum. Etiam congue auctor 
			nulla, sit amet tincidunt velit fringilla quis. Fusce at est massa. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut 
			rutrum sit amet velit eu mattis. Nullam eu nisl nibh. Praesent mi metus, convallis eget quam nec, aliquam ultrices justo. 
		</p>
		
	</div>		
</div> -->
<div class="row">
	<div class="col-md-10" id="ajaxResponse"></div>
</div>
<#--
<div class="row">
	<div role="tabpanel" class="col-md-10 col-md-offset-1">
		<ul class="nav nav-tabs" role="tablist">
  			<li role="tab" class="active"><a href="#expression" aria-controls="expression" role="tab" data-toggle="tab">Expression Data</a></li>
  			<li role="tab"><a href="#test" aria-controls="test" role="tab" data-toggle="tab">Test</a></li>
		</ul>

	<div class="tab-content">
		<div class="tab-pane active" id="expression">
			<div class="col-md-10" id="ajaxResponse"></div>
		</div>

		<div class="tab-pane" id="test">
			Test test test
		</div>
	</div>
</div>
-->

<#--
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">
 	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title">Information</h4>
			</div>
    		<div class="modal-body">
    			Information about the plot and the data...<br>
				Test test test<br>
				...
    		</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			</div>
  		</div>
	</div>
</div>
-->


<div class="modal fade" id="imagemodal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
        <h4 class="modal-title" id="myModalLabel">Image preview</h4>
      </div>
      <div class="modal-body">
        <img src="" id="imagepreview" style="width: 400px; height: 264px;" >
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>

<@footer/>