<#include 'molgenis-header.ftl'>
<#include 'molgenis-footer.ftl'>
<#assign css=['jasny-bootstrap.min.css', 'vis.min.css']>
<#assign js=['bootstrap3-typeahead.min.js','jasny-bootstrap.min.js', 'biobank-universe-network.js', 'bootbox.min.js', 'vis.min.js']>
<@header css js/>
<div class="row">
    <div class="col-md-offset-1 col-md-10">
        <div class="panel panel-primary">
            <div class="panel-heading">
                <div>
                    <h2 class="panel-title">Discover Biobanks</h2>
                </div>
            </div>
            <div class="panel-body">
                <div class="form-group">
                    <select id="biobankUniverseIdentifier" name="biobankUniverseIdentifier" class="form-control">
                    <#list biobankUniverses as biobankUniverse>
                        <option value="${biobankUniverse.identifier?html}">${biobankUniverse.name?html}</option>
                    </#list>
                    </select>
                </div>
                <div id="network-button" class="btn-group">
                    <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown"
                            aria-haspopup="true" aria-expanded="false">
                        Network <span class="caret"></span>
                    </button>
                    <ul id="network-option" class="dropdown-menu">
                    <#if networkTypes??>
                        <#list networkTypes as networkType>
                            <li><a href="#">${networkType?html}</a></li>
                        </#list>
                    </#if>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-md-offset-1 col-md-10">
        <div class="panel panel-primary">
            <div class="panel-heading"><h2 class="panel-title">Network</h2></div>
            <div class="panel-body">
                <div class="row">
                    <div class="col-md-offset-4 col-md-4">
                        <input type="text" id="ontologyTermTypeahead" data-provide="typeahead" class="form-control">
                    </div>
                </div>
                <!-- TODO : move all the sytles to a css file-->
                <div id='network' style="height:800px;"></div>
            </div>
        </div>
    </div>
</div>
<@footer/>