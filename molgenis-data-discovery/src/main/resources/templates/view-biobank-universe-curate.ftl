<#include 'molgenis-header.ftl'>
<#include 'molgenis-footer.ftl'>
<#assign css=['jasny-bootstrap.min.css','biobank-universe-curate.css']>
<#assign js=['jasny-bootstrap.min.js', 'biobank-universe-curate.js']>
<@header css js/>
<div class="row">
    <div class="col-md-12">
        <br/>
        <center><h2>Manage biobank matches</h2></center>
        <br/>
    </div>
</div>
<form method="get">
    <div class="row">
        <div class="col-md-offset-3 col-md-6">
            <select name="targetSampleCollectionName" class="form-control">
            <#list sampleCollections as member>
                <option value="${member.name?html}"
                        <#if member.name==targetSampleCollection.name>selected</#if>>${member.name?html}</option>
            </#list>
            </select></br>
            <button typle="submit" class="btn btn-primary">
                Get candidates
            </button>
        </div>
    </div>
    <div class="row">
        <div class="col-md-offset-1 col-md-10">
        <#if candidateMappingCandidates??>
            <table class="table table-bordered">
                <#if (candidateMappingCandidates?keys?size > 0)>
                    <#assign firstAttributeName = candidateMappingCandidates?keys[0] />
                    <tr>
                        <th>Target attributes</th>
                        <#list candidateMappingCandidates[firstAttributeName]?keys as sampleCollection>
                            <th>Source: ${sampleCollection?html}</th>
                        </#list>
                    </tr>
                    <#list candidateMappingCandidates?keys as attributeName>
                        <tr>
                            <td>${attributeName?html}</td>
                            <#assign candidateMatcheMap =candidateMappingCandidates[attributeName]/>
                            <#list candidateMatcheMap?keys as sourceSampleCollection>
                                <#assign candidateMatches = candidateMatcheMap[sourceSampleCollection]>
                                <#assign curated = candidateMappingCandidatesDecision[attributeName][sourceSampleCollection]>
                                <td>
                                    <#if (candidateMatches?size>0 )>
                                        <!-- This is what is shown in the cell of the overview table -->
                                        <button type="button" class="btn btn-primary" data-toggle="modal"
                                                data-target="#${attributeName}-${sourceSampleCollection}">
                                            <span class="glyphicon <#if curated>glyphicon-ok<#else>glyphicon-pencil</#if>"></span>
                                        </button>

                                        <!-- This is the popup where users can make decisions on the candidate matches -->
                                        <div class="modal fade modal-wide"
                                             id="${attributeName}-${sourceSampleCollection}"
                                             tabindex="-1"
                                             role="dialog"
                                             aria-labelledby="myModalLabel" aria-hidden="true">
                                            <form method="post"
                                                  action="${context_url}/universe/curate/${biobankUniverse.identifier?html}">
                                                <#assign attribute = biobankSampleAttributeMap[attributeName]>
                                                <input type="hidden" name="targetAttribute"
                                                       value="${attribute.identifier?html}">
                                                <input type="hidden" name="sourceAttributes"/>
                                                <input type="hidden" name="targetSampleCollection"
                                                       value="${targetSampleCollection.name}">
                                                <input type="hidden" name="sourceSampleCollection"
                                                       value="${sourceSampleCollection}">
                                                <div class="modal-dialog" role="document">
                                                    <div class="modal-content">
                                                        <div class="modal-header">
                                                            <button type="button" class="close" data-dismiss="modal"
                                                                    aria-label="Close">
                                                                <span aria-hidden="true">&times;</span>
                                                            </button>
                                                            <h4 class="modal-title" id="myModalLabel">
                                                                Curate ${attributeName?html} in the
                                                                Source: ${sourceSampleCollection?html}</h4>
                                                        </div>
                                                        <div class="modal-body">
                                                            <div>
                                                                Target attribute: </br>
                                                                <table class="table table-borded">
                                                                    <tr>
                                                                        <th>Name</th>
                                                                        <td>${attribute.name?html}</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th>Label</th>
                                                                        <td>${attribute.label?html}</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th>Data type</th>
                                                                        <td>${attribute.biobankAttributeDataType?html}</td>
                                                                    </tr>
                                                                </table>
                                                            </div>
                                                            <div>
                                                                <#if (candidateMatches?size > 0)>
                                                                    Candidate source attributes: </br>
                                                                    <table class="table table-borded"
                                                                           style="overflow: auto">
                                                                        <tr>
                                                                            <th>Name</th>
                                                                            <th>Label</th>
                                                                            <th>Data type</th>
                                                                            <th>Score</th>
                                                                            <th>Matched target words</th>
                                                                            <th>Matched source words</th>
                                                                            <th>Select</th>
                                                                        </tr>
                                                                        <#list candidateMatches as candidateMatch>
                                                                            <tr>
                                                                                <td>${candidateMatch.source.name?html}</td>
                                                                                <td>${candidateMatch.source.label?html}</td>
                                                                                <td>${candidateMatch.source.biobankAttributeDataType?html}</td>
                                                                                <td>${(candidateMatch.explanation.ngramScore * 100)?html}
                                                                                    %
                                                                                </td>
                                                                                <td>${candidateMatch.explanation.matchedTargetWords?html}</td>
                                                                                <td>${candidateMatch.explanation.matchedSourceWords?html}</td>
                                                                                <td><input
                                                                                        value="${candidateMatch.source.identifier?html}"
                                                                                        type="checkbox"/></td>
                                                                            </tr>
                                                                        </#list>
                                                                    </table>
                                                                </#if>
                                                            </div>
                                                        </div>
                                                        <div class="modal-footer">
                                                            <button type="button" class="btn btn-secondary"
                                                                    data-dismiss="modal">Close
                                                            </button>
                                                            <button type="submit" class="btn btn-primary">Submit
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>
                                            </form>
                                        </div>
                                    </#if>
                                </td>
                            </#list>
                        </tr>
                    </#list>
                </#if>
            </table>
        </#if>
        </div>
    </div>
</form>
<@footer/>