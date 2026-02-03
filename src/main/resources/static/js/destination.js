

$(function () {

    

    $('#destinationSetting').select2({
        placeholder: 'Search destination...',
        allowClear: true,
        ajax: {
            url: '/destinations/ajax/settings',
            dataType: 'json',
            delay: 300,
            data: function (params) {
                return {
                    q: params.term,
                    limit: 10
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
    $('#destinationSetting').on('select2:select', function (e) {
        console.log({e});
        const selected = e.params.data;

        const truckId = selected.truckId;
        $('#date').trigger('focus');

    });

    function autoSelectDestination() {
        const id = parseInt($('#destinationSettingId').val()) || null;
        if (!id) return;

        const code = $('#destinationSettingCode').val();
        const name = $('#destinationSettingName').val();
        const distance = $('#destinationSettingDistanceFormat').val();

        const text = `${code} | ${name} | ${distance}`;

        const option = new Option(text, id, true, true);
        $('#destinationSetting').append(option).trigger('change:open');
        setTimeout(() => {
            $('#date').change();
        }, 500);
    }
    autoSelectDestination();

    $('#truck, #date').on('change', function () {
        const truckId = $("#truck").val();
        const destinationSetting = $("#destinationSetting").val();
        const date = $("#date").val();
        const destinationId = $("#id").val() ?? null;

        $.ajax({
            url: "/destinations/ajax/checking-existing-entries",
            type: "GET",
            data: {
                truckId: truckId,
                settingId: destinationSetting,
                date: date,
                destinationId: destinationId,
            },
            success: function (response) {

                console.log(response);

                if (response.isExist) {
                    alert(response.message);
                    $("#btn-submit").prop("disabled", true);
                } else {
                    $("#btn-submit").prop("disabled", false);
                }
            },
            error: function (xhr, status, error) {
                console.error("Error:", error);
            }
        });
    });
  
    $('#destinationSetting').on('select2:clear', function () {
        $('#destinationSetting').val('');
    });

})