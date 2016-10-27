$(document).ready(function () {

    $('select').select2();

    //Bind events to each network option
    $('#network-button li').on('click', function () {

        if ($(this).text() == 'semantic_similarity') {
            $('#ontologyTermTypeahead').hide();
        } else {
            $('#ontologyTermTypeahead').show();
        }

        var selectedTopic = $('#ontologyTermTypeahead').typeahead("getActive");
        var ontologyTermIri = selectedTopic == null ? '' : selectedTopic.IRI;
        var biobankUniverseIdentifier = $('#biobankUniverseIdentifier').val();
        var networkType = $(this).text();
        if (biobankUniverseIdentifier) {
            var request = {
                'biobankUniverseIdentifier': biobankUniverseIdentifier,
                'networkType': networkType,
                'ontologyTermIri': ontologyTermIri
            };
            $.ajax({
                type: 'POST',
                url: molgenis.getContextUrl() + '/network/create',
                data: JSON.stringify(request),
                contentType: 'application/json',
                success: function (visNetworkReponse) {
                    createNetwork(visNetworkReponse);
                }
            });
        }
    });


    //Always click the first option when the page is loaded
    $('#network-button li:eq(0)').click();

    //Initialize the ontology term typeahead
    $('#ontologyTermTypeahead').typeahead({
        source: function (query, process) {
            $.ajax({
                type: 'POST',
                url: molgenis.getContextUrl() + '/network/topic',
                data: JSON.stringify(query),
                contentType: 'application/json',
                success: function (ontologyTerms) {
                    var items = [];
                    $.each(ontologyTerms, function (index, ontologyTerm) {
                        items.push({IRI: ontologyTerm.IRI, name: ontologyTerm.IRI + ':' + ontologyTerm.label});
                    });
                    process(items);
                }
            });
        },
        sorter: function (items) {
            return items;
        },
        items: 30,
        minLength: 3,
        delay: 1

    });

    //Create the network
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

        // initialize the network!
        var network = new vis.Network(container, data, options);
    }
});