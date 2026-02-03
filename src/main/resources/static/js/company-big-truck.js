

$(function () {

    $('#destination').select2({
        placeholder: 'Search destination...',
        allowClear: true,
        ajax: {
            url: '/destinations/ajax/pending',
            dataType: 'json',
            delay: 300,
            data: function (params) {
                return {
                    q: params.term,
                    limit: 10,
                    destinationId: $('#destinationId').val() ?? null
                };
            },
            processResults: function (data) {
                return {
                    results: data
                };
            }
        }
    });

    // Set up the event handler BEFORE calling autoSelectDestination
    $('#destination').on('select2:select', function (e) {
        console.log({e});
        const selected = e.params.data;

        const truckId = selected.truckId;
        $('#truck').val(truckId).trigger('change');

        // These lines might be redundant since the value is already set
        // $('#destination').val(selected.id); 

        $('#totalDestination').val(selected.settingName);
        $('#totalKm').val(selected.settingDistance);

        $('#date').prop('readonly', true).val(selected.date).trigger('change');

        console.log('Destination ID:', selected.id);
        console.log('Truck ID:', truckId);
    });

    function autoSelectDestination() {
        const destinationId = $('#destinationId').val();

        if (!destinationId) return;

        $.ajax({
            url: '/destinations/ajax/' + destinationId,
            type: 'GET',
            dataType: 'json'
        }).then(function (data) {

            if(data?.id) {
                console.log({data});
            // Check if option already exists to avoid duplicates
            let existingOption = $('#destination').find('option[value="' + data.id + '"]');

            if (existingOption.length === 0) {
                // Option doesn't exist, create and add it
                const option = new Option(data.text, data.id, true, true);
                $('#destination').append(option);
            }

            // Set the value without triggering select2:select event
            $('#destination').val(data.id).trigger('change');

            // Now manually trigger the same logic as in select2:select
            // This ensures all fields get populated
            $('#totalDestination').val(data.settingName || '');
            $('#totalKm').val(data.settingDistance || '');
            $('#date').prop('readonly', true).val(data.date || '').trigger('change');

            if (data.truckId) {
                $('#truck').val(data.truckId).trigger('change');
            }

            console.log('Auto-selected Destination ID:', data.id);
            }

            
        });
    }

    autoSelectDestination()

    $('#truck').on('change', function () {
        const truckId = $(this).val();
        const $select = $('#measurement');

        // Get the selected measurement from the hidden field (for edit mode)
        const selectedMeasurement = $('#selectedMeasurement').val();

        $select.empty().append('<option value="">Select Measurement</option>');

        if (!truckId) return;

        $.ajax({
            url: '/averages/ajax/measurements/by-truck',
            type: 'GET',
            data: { truckId: truckId },
            dataType: 'json',
            success: function (data) {
                data.forEach(function (item) {
                    // Check if this item should be selected
                    const isSelected = (selectedMeasurement && selectedMeasurement == item.name);

                    $select.append(
                        $('<option>', {
                            value: item.id,
                            text: item.text,
                            'data-average': item.average,
                            selected: isSelected
                        })
                    );
                });

                // Trigger change to calculate values if something is selected
                $select.trigger('change');
            },
            error: function (err) {
                console.error('Failed to load measurements', err);
            }
        });
    });

    $('#measurement').on('change', function () {
        const $selected = $(this).find(':selected');
        const avg = parseFloat($selected.data('average')) || 0;

        // Set average input
        $('#average').val(avg);

        // Get totalKm
        const totalKm = parseFloat($('#totalKm').val()) || 0;

        // Calculate litreQuantity
        const litreQuantity = avg * totalKm;
        $('#litreQuantity').val(litreQuantity.toFixed(2));

        // Recalculate totalOilsChange with current otherOils
        const otherOils = parseFloat($('#otherOils').val()) || 0;
        $('#totalOilsChange').val((litreQuantity - otherOils).toFixed(2));
    });

    // WHEN MEASUREMENT IS SELECTED OR TOTAL KM CHANGES
    function recalcLitreQuantity() {
        const avg = parseFloat($('#average').val()) || 0;
        const totalKm = parseFloat($('#totalKm').val()) || 0;
        const litreQuantity = avg * totalKm;
        $('#litreQuantity').val(litreQuantity.toFixed(2));

        const otherOils = parseFloat($('#otherOils').val()) || 0;
        $('#totalOilsChange').val((litreQuantity + otherOils).toFixed(2));
    }

    // Trigger calculation when measurement changes
    $('#measurement').on('change', recalcLitreQuantity);

    // Trigger calculation when totalKm changes
    $('#totalKm').on('input', recalcLitreQuantity);

    // TRIGGER WHEN OTHER OILS CHANGE (can be negative)
    $('#otherOils').on('input', function () {
        const litreQuantity = parseFloat($('#litreQuantity').val()) || 0;
        const otherOils = parseFloat($(this).val()) || 0;

        // totalOilsChange = litreQuantity + otherOils
        $('#totalOilsChange').val((litreQuantity + otherOils).toFixed(2));
    });


    // WHEN DESTINATION CLEARED
    $('#destination').on('select2:clear', function () {
        // Reset all related fields
        $('#truck').val('').trigger('change');
        $('#destination').val('');
        $('#totalDestination').val('');
        $('#totalKm').val('');
        $('#measurement').val('').trigger('change');
        $('#average').val('');
        $('#litreQuantity').val('');
        $('#otherOils').val('');
        $('#totalOilsChange').val('');

        $('#date').prop('readonly', true).val('').trigger('change');
    });

})