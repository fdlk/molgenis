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
                <div class="row">
                    <div class="col-md-12">
                        <div class="form-group">
                            <label for="biobankUniverseIdentifier">Select the universe</label>
                            <select id="biobankUniverseIdentifier" class="form-control">
                            <#list biobankUniverses as biobankUniverse>
                                <option value="${biobankUniverse.identifier?html}">${biobankUniverse.name?html}</option>
                            </#list>
                            </select>
                        </div>
                        <div class="form-group">
                        <#if networkTypes??>
                            <label for="networkType">Select a network type</label>
                            <select id="networkType" class="form-control">
                                <#list networkTypes as networkType>
                                    <option value="${networkType?html}">${networkType?html}</option>
                                </#list>
                            </select>
                        </#if>
                        </div>
                    </div>
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
                <div class="row" id="searchControl">
                    <div class="col-md-offset-4 col-md-4">
                        <div class="form-group">
                            <input type="text" id="ontologyTermTypeahead" data-provide="typeahead"
                                   class="form-control typeahead"
                                   placeholder="Search for...">
                        </div>
                        <div class="form-group">
                            <button id="searchOntologyTerm" type="button" class="btn btn-default">Search</button>
                            <button id="clearOntologyTerm" type="button" class="btn btn-danger">Clear</button>
                        </div>
                    </div>
                </div>
                <!-- TODO : move all the sytles to a css file-->
                <div id='network' style="height:800px;"></div>
            </div>
        </div>
    </div>
</div>
<@footer/>