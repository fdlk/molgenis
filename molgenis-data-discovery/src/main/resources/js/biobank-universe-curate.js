$(function () {

    $('#targetSampleCollectionName').select2();

    $('.attribute-candidate-match-modal').on('shown.bs.modal', function () {

        var currentModal = $(this);
        var biobankUniverse = $(this).find('input[name="biobankUniverse"]:eq(0)').val();
        var targetAttribute = $(this).find('input[name="targetAttribute"]:eq(0)').val();
        var sourceBiobankSampleCollection = $(this).find('input[name="sourceSampleCollection"]:eq(0)').val();
        var attributeCandidateMatchContainer = $(this).find('div[name="attribute-candidate-match-container"]');

        var baseUrl = molgenis.getContextUrl() + '/universe/' + biobankUniverse;
        baseUrl += '/attributematch?targetAttribute=' + targetAttribute;
        baseUrl += '&sourceBiobankSampleCollection=' + sourceBiobankSampleCollection;

        $.ajax({
            type: 'get',
            url: baseUrl,
            contentType: 'application/json',
            success: function (attributeCandidateMatches) {

                if (attributeCandidateMatches.length > 0) {
                    var table = createAttributeCandidateTable(attributeCandidateMatches);
                    attributeCandidateMatchContainer.append('Candidate source attributes: </br>');
                    attributeCandidateMatchContainer.append(table);

                    table.find('input[type="checkbox"]').on('click', function () {
                        var checkedboxes = table.find('input:checked');
                        var sourceAttributeIdentifiers = [];
                        $.map(checkedboxes, function (checkbox, i) {
                            sourceAttributeIdentifiers.push($(checkbox).val());
                        });
                        var sourceAttributeIdentifiersHiddenInput = $(this).parents('.modal:eq(0)').find('input[name="sourceAttributes"]');
                        sourceAttributeIdentifiersHiddenInput.val(sourceAttributeIdentifiers.join(','));
                    });

                } else {
                    attributeCandidateMatchContainer.append('There are no matches generated.');
                }
            }
        });

        // var currentModal = $(this);
        // var submitButton = $(this).find('button[type="submit"]:eq(0)');
        //
        // submitButton.on('click', function () {
        //     var sourceAttributes = [];
        //     var candidateMatchTable = $(currentModal).find('div[name="attribute-candidate-match-container"] table:eq(0)');
        //     candidateMatchTable.find("input:checked").each(function () {
        //         sourceAttributes.push($(this).val());
        //     });
        //
        //     var baseUrl = molgenis.getContextUrl() + '/universe/' + biobankUniverse + '/curate';
        //     var attributeMatchCurationRequest = {
        //         'targetAttribute': targetAttribute,
        //         'sourceAttributes': sourceAttributes,
        //         'sourceBiobankSampleCollection': sourceBiobankSampleCollection
        //     };
        //
        //     $.ajax({
        //         type: 'post',
        //         url: baseUrl,
        //         data: JSON.stringify(attributeMatchCurationRequest),
        //         contentType: 'application/json'
        //     });
        // });
    });

    function createAttributeCandidateTable(attributeCandidateMatches) {
        var table = $('<table />').addClass('table table-borded table-hover table-row-clickable');
        table.append('<tr><th>Name</th><th>Label</th><th>Data type</th><th>Score</th><th>Matched target words</th><th>Matched source words</th><th>Select</th></tr>');

        $.each(attributeCandidateMatches, function (index, match) {
            var checked = match.decisions.length > 0 && match.decisions[0].decision.toLowerCase() == 'yes' ? 'checked' : '';
            var row = $('<tr />');
            row.append('<td>' + match.source.name + '</td>');
            row.append('<td>' + match.source.label + '</td>');
            row.append('<td>' + match.source.biobankAttributeDataType + '</td>');
            row.append('<td>' + (match.explanation.vsmScore * 100).toFixed(2) + '</td>');
            row.append('<td>' + match.explanation.matchedTargetWords + '</td>');
            row.append('<td>' + match.explanation.matchedSourceWords + '</td>');
            row.append('<td><input value="' + match.source.identifier + '"' + checked + ' type=checkbox /></td>');

            row.on('click', function () {
                var checkbox = $(this).find(':checkbox');
                checkbox.trigger('click');
            });

            table.append(row);
        });

        return table;
    }
});