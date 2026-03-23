$(function () {
    // Cache for truck route numbers to avoid repeated API calls
    const truckRouteCache = new Map();
    const destinationCache = new Map();

    // Store selected values for each row
    const rowSelections = new Map();

    async function getDestinationData(destinationName) {
        if (!destinationName) return null;

        if (destinationCache.has(destinationName)) {
            return destinationCache.get(destinationName);
        }

        try {
            const response = await fetch(`/api/v1/scale-fee/destination-with-scales-ports?destinationName=${ encodeURIComponent(destinationName) }`);
            if (response.ok) {
                const data = await response.json();
                destinationCache.set(destinationName, data);
                return data;
            }
        } catch (err) {
            console.error('Error fetching destination:', err);
        }

        return null;
    }

    /* -----------------------
    Get Truck Route Number from Database
    ----------------------- */
    async function getTruckRouteNumber(licensePlate) {
        if (!licensePlate) return null;

        // Check cache first
        if (truckRouteCache.has(licensePlate)) {
            return truckRouteCache.get(licensePlate);
        }

        try {
            const response = await fetch(`/api/v1/trucks/route/${ encodeURIComponent(licensePlate) }`);
            if (response.ok) {
                const data = await response.json();
                const routeNumber = data.routeNumber ? parseInt(data.routeNumber) : null;
                truckRouteCache.set(licensePlate, routeNumber);
                return routeNumber;
            }
        } catch (error) {
            console.error('Error fetching truck route:', error);
        }
        return null;
    }

    /* -----------------------
    Clean Excel Values
    ----------------------- */
    function cleanNumber(value) {
        if (!value) return 0;
        if (typeof value === 'number') return value;

        return parseFloat(
            value.toString()
                .replace(/[^\d.-]/g, '')
                .replace(/,/g, '')
        ) || 0;
    }

    /* -----------------------
    Format Number
    ----------------------- */
    function formatNumber(value, decimals = 2) {
        return new Intl.NumberFormat('en-US', {
            minimumFractionDigits: decimals,
            maximumFractionDigits: decimals
        }).format(value);
    }

    /* -----------------------
    Total Weight
    ----------------------- */
    function calcTotalWeight(cargoWeight, truckWeight) {
        return (cargoWeight > 0 ? cargoWeight + truckWeight : 0);
    }

    /* -----------------------
    Scale Fee Calculation (using API with Truck Route)
    ----------------------- */
    async function calcScaleFee(licensePlate, scaleStation, totalWeight) {
        try {
            // Call backend API to get scale fee from database using truck's route number
            const response = await fetch('/api/v1/scale-fee/calculate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    licensePlate: licensePlate,
                    scaleStationName: scaleStation,
                    totalWeight: totalWeight
                })
            });

            if (response.ok) {
                const data = await response.json();
                return data.amount;
            }
        } catch (error) {
            console.error('Error calculating scale fee:', error);
        }

        // Fallback to local calculation if API fails
        return 20000; // Default fee
    }

    /* -----------------------
    Calculate Row Scale Total from selected options with data-amount
    ----------------------- */
    function calculateRowScaleTotal(rowId, selectElement) {
        let rowTotal = 0;

        $(`#${ selectElement } option:selected`).each(function () {
            const amount = $(this).data('amount') || 0;
            rowTotal += amount;
        });

        // Update row total display
        $(`#scaleTotal-${ rowId }`).text(`${ formatNumber(rowTotal, 0) } Riel`);

        // Update the បង់ជញ្ជីង column (column 15)
        $(`#scaleFeeDisplay-${ rowId }`).text(formatNumber(rowTotal, 0));

        return rowTotal;
    }

    /* -----------------------
    Calculate Row Port Total from selected options with data-amount
    ----------------------- */
    function calculateRowPortTotal(rowId, selectElement) {
        let rowTotal = 0;

        $(`#${ selectElement } option:selected`).each(function () {
            const amount = $(this).data('amount') || 0;
            rowTotal += amount;
        });

        // Update row total display
        $(`#portTotal-${ rowId }`).text(`${ formatNumber(rowTotal, 0) } Riel`);

        // Update the បង់ផែ column (column 17)
        $(`#portFeeDisplay-${ rowId }`).text(formatNumber(rowTotal, 0));

        return rowTotal;
    }

    /* -----------------------
    Calculate Grand Totals
    ----------------------- */
    function calculateGrandTotals() {
        let totalSelectedScales = 0;
        let totalSelectedPorts = 0;

        // Sum all scale totals
        $('[id^="scaleTotal-"]').each(function () {
            const text = $(this).text();
            const value = parseFloat(text.replace(/[^0-9.-]/g, '')) || 0;
            totalSelectedScales += value;
        });

        // Sum all port totals
        $('[id^="portTotal-"]').each(function () {
            const text = $(this).text();
            const value = parseFloat(text.replace(/[^0-9.-]/g, '')) || 0;
            totalSelectedPorts += value;
        });

        // Update grand totals display
        $('#grandTotalScales').text(formatNumber(totalSelectedScales, 0) + ' Riel');
        $('#grandTotalPorts').text(formatNumber(totalSelectedPorts, 0) + ' Riel');
        $('#grandTotalAll').text(formatNumber(totalSelectedScales + totalSelectedPorts, 0) + ' Riel');
    }

    /* -----------------------
    Batch Process Rows with Truck Route Numbers
    ----------------------- */
    async function processRowsWithTruckRoutes(rows, tbody, startIndex = 3) {
        const processedData = [];
        const batchSize = 10; // Process 10 rows at a time to avoid too many concurrent requests

        for (let i = startIndex; i < rows.length; i += batchSize) {
            const batch = rows.slice(i, Math.min(i + batchSize, rows.length));
            const batchPromises = batch.map(async (r, index) => {
                if (!r || r.length === 0 || !r[0]) return null;

                const actualIndex = i + index;
                const licensePlate = r[2]?.toString().trim() || '';
                const routeNumber = await getTruckRouteNumber(licensePlate);

                return {
                    row: r,
                    index: actualIndex,
                    licensePlate,
                    routeNumber
                };
            });

            const batchResults = await Promise.all(batchPromises);

            for (const result of batchResults) {
                if (!result) continue;

                const { row: r, index: actualIndex, licensePlate, routeNumber } = result;

                let cargoWeight = cleanNumber(r[9]);
                let truckWeight = cleanNumber(r[10]);
                let totalWeight = calcTotalWeight(cargoWeight, truckWeight);
                let scaleStation = r[12]?.toString() || ""; // បង់ជញ្ជីង	
                let destination = r[8]?.toString() || ""; // គោលដៅសរុប
                let benValue = cleanNumber(r[13]); // បង់បេន
                let portValue = cleanNumber(r[14]); // បង់ផែ
                let policeValue = cleanNumber(r[15]); // ប៉ូលីស
                let otherValue = cleanNumber(r[16]); // ផ្សេងៗ
                let expenseValue = cleanNumber(r[17]); // ចំណាយ
                let nationalRoadValue = cleanNumber(r[18]); // ផ្លូវជាតិ
                let markValue = r[19]?.toString() || ""; // សម្គាល់
                let noteValue = r[20]?.toString() || ""; // កំណត់ចំណាំ

                console.log({ destination, scaleStation });

                // Calculate scale fee using truck's route number
                let scaleFee = await calcScaleFee(licensePlate, scaleStation, totalWeight);

                const rowData = {
                    no: r[0],
                    date: r[1],
                    truckNo: licensePlate,
                    truckType: r[3],
                    routeNumber: routeNumber,
                    cargoType: r[5],
                    pickup: r[6],
                    dropoff: r[7],
                    destination: destination,
                    cargoWeight: cargoWeight,
                    truckWeight: truckWeight,
                    totalWeight: totalWeight,
                    scaleFee: scaleFee,
                    benValue: benValue,
                    portValue: portValue,
                    policeValue: policeValue,
                    otherValue: otherValue,
                    expenseValue: expenseValue,
                    nationalRoadValue: nationalRoadValue,
                    markValue: markValue,
                    noteValue: noteValue
                };

                processedData.push(rowData);

                // Initialize selections for this row
                if (!rowSelections.has(actualIndex)) {
                    rowSelections.set(actualIndex, {
                        licensePlate: licensePlate,
                        scaleIds: [],
                        portIds: [],
                        benValue: benValue,
                        portValue: portValue,
                        rowData: rowData
                    });
                }

                // Determine row styling based on scale fee
                let rowClass = '';
                if (scaleFee === 0) {
                    rowClass = 'bg-green-50 dark:bg-green-900/20';
                } else if (scaleFee > 20000) {
                    rowClass = 'bg-yellow-50 dark:bg-yellow-900/20';
                }

                const scaleSelectId = `scaleSelect-${ actualIndex }`;
                const portSelectId = `portSelect-${ actualIndex }`;
                const scaleDisplayId = `scaleFeeDisplay-${ actualIndex }`;
                const portDisplayId = `portFeeDisplay-${ actualIndex }`;
                const scaleTotalId = `scaleTotal-${ actualIndex }`;
                const portTotalId = `portTotal-${ actualIndex }`;

                // Add route number badge if available
                const routeBadge = routeNumber ?
                    `<span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300 ml-2">
                        Route ${ routeNumber }
                    </span>` : '';

                let tr = `
                    <tr class="${ rowClass } hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-colors duration-150" data-row-id="${ actualIndex }">
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[0] ?? '' }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[1] ?? '' }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 font-medium text-gray-800 dark:text-gray-200">
                            ${ licensePlate }
                            ${ routeBadge }
                        </td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[3] ?? '' }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[4] ?? '' }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[5] ?? '' }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[6] ?? '' }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[7] ?? '' }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ destination }</td>
                        
                        <!-- តម្លៃជញ្ជីង Column with Select2 -->
                        <td class="px-4 py-2 border-r border-gray-200 dark:border-gray-700">
                            <select id="${ scaleSelectId }" class="scale-select" multiple style="width:100%"></select>
                            <div class="flex justify-between items-center mt-1 text-xs">
                                <span class="text-gray-500 dark:text-gray-400">Total:</span>
                                <span class="font-medium text-blue-600 dark:text-blue-400" id="${ scaleTotalId }">0 Riel</span>
                            </div>
                        </td>

                        <!-- តម្លៃកំពង់ផែ Column with Select2 -->
                        <td class="px-4 py-2 border-r border-gray-200 dark:border-gray-700">
                            <select id="${ portSelectId }" class="port-select" multiple style="width:100%"></select>
                            <div class="flex justify-between items-center mt-1 text-xs">
                                <span class="text-gray-500 dark:text-gray-400">Total:</span>
                                <span class="font-medium text-green-600 dark:text-green-400" id="${ portTotalId }">0 Riel</span>
                            </div>
                        </td>

                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(cargoWeight, 2) }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(truckWeight, 2) }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-right font-medium text-gray-800 dark:text-gray-200">${ formatNumber(totalWeight, 2) }</td>
                        
                        <!-- បង់ជញ្ជីង Column - Updated by scale selection -->
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-right font-bold text-blue-600 dark:text-blue-400" id="${ scaleDisplayId }">0</td>
                        
                       
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-green-600 dark:text-green-400" id="${ portDisplayId }">0</td>
                        
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(policeValue, 0) }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(otherValue, 0) }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(expenseValue, 0) }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(nationalRoadValue, 0) }</td>
                        <td class="border-r border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ markValue }</td>
                        <td class="px-4 py-2 text-gray-800 dark:text-gray-200">${ noteValue }</td>
                    </tr>
                `;

                tbody.append(tr);

                // Fetch destination data and initialize Select2
                const destinationData = await getDestinationData(destination);

                // Initialize Scale Select2 with change event
                $(`#${ scaleSelectId }`).select2({
                    placeholder: "Select Scales",
                    width: '100%'
                }).on('change', function () {
                    // Calculate row total using data-amount attributes
                    calculateRowScaleTotal(actualIndex, scaleSelectId);

                    // Update grand totals
                    calculateGrandTotals();
                });

                // Initialize Port Select2 with change event
                $(`#${ portSelectId }`).select2({
                    placeholder: "Select Ports",
                    width: '100%'
                }).on('change', function () {
                    // Calculate row total using data-amount attributes
                    calculateRowPortTotal(actualIndex, portSelectId);

                    // Update grand totals
                    calculateGrandTotals();
                });

                // Add scale options with data-amount attributes
                if (destinationData?.scales) {
                    destinationData.scales.forEach(s => {
                        const option = new Option(
                            `${ s.scaleStationName } (${ formatNumber(s.amount, 0) } Riel)`,
                            s.scaleStationId,
                            false,
                            false
                        );
                        // Add data-amount attribute
                        $(option).data('amount', s.amount);
                        $(option).attr('data-amount', s.amount);
                        $(`#${ scaleSelectId }`).append(option);
                    });
                }

                // Add port options with data-amount attributes
                if (destinationData?.ports) {
                    destinationData.ports.forEach(p => {
                        const option = new Option(
                            `${ p.portName } (${ formatNumber(p.amount, 0) } Riel)`,
                            p.portId,
                            false,
                            false
                        );
                        // Add data-amount attribute
                        $(option).data('amount', p.amount);
                        $(option).attr('data-amount', p.amount);
                        $(`#${ portSelectId }`).append(option);
                    });
                }

                // Trigger change to initialize displays
                $(`#${ scaleSelectId }`).trigger('change');
                $(`#${ portSelectId }`).trigger('change');
            }

            // Update progress
            const progress = Math.min(100, Math.round((i + batch.length) / rows.length * 100));
            $('#progressBar').css('width', progress + '%').attr('aria-valuenow', progress);
            $('#progressText').text(`Processing... ${ progress }%`);
        }

        return processedData;
    }

    /* -----------------------
    Update Statistics
    ----------------------- */
    function updateStatistics(data) {
        let totalRecords = data.length;
        let totalWeight = 0;
        let totalScaleFees = 0;

        data.forEach(row => {
            totalWeight += row.totalWeight || 0;
            totalScaleFees += row.scaleFee || 0;
        });

        $('#totalRecords').text(totalRecords);
        $('#totalWeight').text(formatNumber(totalWeight, 2) + ' kg');
        $('#totalScaleFees').text(formatNumber(totalScaleFees, 0) + ' Riel');
    }

    /* -----------------------
    Excel Upload
    ----------------------- */
    $("#excelFile").on("change", async function (e) {
        const file = e.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        const $processBtn = $('#processFileBtn');
        const $loading = $('#loadingIndicator');
        const $progressBar = $('#progressBar');
        const $progressContainer = $('#progressContainer');

        reader.onload = async function (event) {
            try {
                $processBtn.prop('disabled', true);
                $loading.removeClass('hidden');
                $progressContainer.removeClass('hidden');

                // Clear previous selections
                rowSelections.clear();

                const data = new Uint8Array(event.target.result);
                const workbook = XLSX.read(data, { type: 'array' });
                const sheetName = workbook.SheetNames[0];
                const sheet = workbook.Sheets[sheetName];
                const rows = XLSX.utils.sheet_to_json(sheet, { header: 1 });

                const tbody = $("#excelPreview");
                tbody.empty();

                // Process rows with truck route lookup
                const scaleFeeData = await processRowsWithTruckRoutes(rows, tbody, 3);

                // Remove processing message if no rows were added
                if (scaleFeeData.length === 0) {
                    tbody.empty();
                    tbody.append(`
                        <tr>
                            <td colspan="23" class="px-4 py-8 text-center text-gray-500 dark:text-gray-400">
                                <i class="fas fa-exclamation-circle text-4xl mb-2 block"></i>
                                No valid data found in the Excel file
                            </td>
                        </tr>
                    `);
                }

                // Update statistics
                updateStatistics(scaleFeeData);

                // Calculate grand totals
                setTimeout(calculateGrandTotals, 500);

            } catch (error) {
                console.error('Error processing file:', error);

                const tbody = $("#excelPreview");
                tbody.empty();
                tbody.append(`
                    <tr>
                        <td colspan="23" class="px-4 py-8 text-center text-red-500 dark:text-red-400">
                            <i class="fas fa-exclamation-triangle text-4xl mb-2 block"></i>
                            Error processing file: ${ error.message }
                        </td>
                    </tr>
                `);
            } finally {
                $processBtn.prop('disabled', false);
                $loading.addClass('hidden');
                $progressContainer.addClass('hidden');
            }
        };

        reader.readAsArrayBuffer(file);
    });

    // Recalculate grand totals when any selection changes
    $(document).on('change', 'select.scale-select, select.port-select', function () {
        setTimeout(calculateGrandTotals, 100);
    });
});