<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#--<#assign css=['']>-->
<#assign js=['lncrna-explorer.js']>

<@header css js/>

<div class="row">
	<div class="col-md-12">
	    <h1> LncRNA Explorer </h1>
	</div>
	<div class="col-md-4  col-md-offset-4">


				<#--<form>-->
					<input type="text" class="form-control" name="searchTerm" id="search-input" placeholder="GeneName">	
					<option val=""></option>
					<button type="btn" class="btn btn-default" id="submit-input">Search</button>
				<#--</form>-->
		
<#--<form>
		<div class="input-group">
			
				<input type="text" class="form-control" name="searchTerm" id="search-input"placeholder="Search for a GeneName">	
				<option val=""></option>
				<input type="submit" class="btn btn-default" id="submit-input"></input>
			
		</div>
		</form>-->

	</div>


 

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


        
            <#--<legend>Response from jQuery Ajax Request</legend>-->
                 <div class="col-md-12" id="ajaxResponse"></div>

</div>


<@footer/>