<#include 'molgenis-header.ftl'>
<#include 'molgenis-footer.ftl'>
<#assign css=['jasny-bootstrap.min.css', 'vis.min.css']>
<#assign js=['jasny-bootstrap.min.js', 'biobank-universes.js', 'bootbox.min.js', 'vis.min.js']>
<@header css js/>
<div class="row">
    <div class="col-md-offset-1 col-md-10">
        <form method="get" action="${context_url}/test/calculate">
            <div class="form-group">
                <label>Select the biobank universe</label>
                <select id="biobankUniverseIdentifier" name="biobankUniverseIdentifier" class="form-control">
                <#list biobankUniverses as biobankUniverse>
                    <option value="${biobankUniverse.identifier?html}">${biobankUniverse.name?html}</option>
                </#list>
                </select>
            </div>
            <div class="btn-group" role="group">
                <button id="calculate-button" type="submit" class="btn btn-primary">calculate</button>
                <button id="network-button" type="button" class="btn btn-info">network</button>
            </div>
            <br/><br/>
        <#if semanticSimilarityMap??>
            <table class="table table-striped table-condensed">
                <tr>
                    <th></th>
                    <#list semanticSimilarityMap?keys as collection>
                        <th>${collection}</th>
                    </#list>
                </tr>
                <#list semanticSimilarityMap?keys as collection>
                    <tr>
                        <td>${collection}</td>
                        <#assign collectionSimilarities = semanticSimilarityMap[collection]>
                        <#list collectionSimilarities as collectionSimilarity>
                            <td>
                                <#if collectionSimilarity.similarity != 0.0>${collectionSimilarity.similarity}</#if>
                            </td>
                        </#list>
                    </tr>
                </#list>
            </table>
        </#if>
        </form>
    </div>
</div>

<div class="row">
    <div class="col-md-offset-1 col-md-10">
        <div id='network' style="height:600px;"></div>
    </div>
</div>
<script>
    $(document).ready(function () {
        $('select').select2();
        $('#network-button').click(function (event) {
            event.preventDefault();
            var biobankUniverseIdentifier = $('#biobankUniverseIdentifier').val();
            if (biobankUniverseIdentifier) {
                $.ajax({
                    type: 'POST',
                    url: molgenis.getContextUrl() + '/test/network',
                    data: biobankUniverseIdentifier,
                    contentType: 'application/json',
                    success: function (visNetworkReponse) {
                        console.log(visNetworkReponse);
                        createNetwork(visNetworkReponse);
                    }
                });
            }
        });

        function createNetwork(visNetworkReponse) {

            var maxNode = Math.max.apply(null, visNetworkReponse.nodes.map(function (node) {
                return node.size;
            }));

            var minNode = Math.min.apply(null, visNetworkReponse.nodes.map(function (node) {
                return node.size;
            }));

            var scalingOption = {'min': 50, 'max': 100};

            visNetworkReponse.nodes.forEach(function (node) {
                return $.extend(node, {'shape': 'circle', 'scaling': scalingOption})
            });

            visNetworkReponse.nodes.forEach(function (node) {
                node.value = node.size
            });

            var nodes = new vis.DataSet(visNetworkReponse.nodes);

            var maxDistance = Math.max.apply(null, visNetworkReponse.edges.map(function (edge) {
                return edge.length;
            }));

            visNetworkReponse.edges.forEach(function (edge) {
                edge.length = (maxDistance - edge.length + 0.05) * 2000
            });

            // create an array with edges
            var edges = new vis.DataSet(visNetworkReponse.edges);

            // create a network
            var container = document.getElementById('network');

            // provide the data in the vis format
            var data = {
                nodes: nodes,
                edges: edges
            };
            var options = {
                autoResize: true,
                height: '100%',
                width: '100%',
                locale: 'en'
            };

            // initialize your network!
            var network = new vis.Network(container, data, options);
        }
    });
</script>
<@footer/>