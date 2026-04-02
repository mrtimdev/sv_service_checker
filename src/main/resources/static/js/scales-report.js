$(function () {
    // Cache for truck route numbers to avoid repeated API calls
    const truckRouteCache = new Map();
    const destinationCache = new Map();

    // Main data structure for storing all report data
    window.scalesReportData = {
        displayData: [],
        timestamp: null
    };

    // Initialize datepicker with default to today if null
    $(".datepicker").flatpickr({
        dateFormat: "M d, Y",
        // maxDate: "today",
        defaultDate: "today"
    });

    function getCurrentDate() {
        const today = new Date();
        // Format: "MMM DD, YYYY" (e.g., "Dec 31, 2024")
        return today.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    }

    // ==================== SINGLE RENDER FUNCTION ====================
    function renderTable() {
        const tbody = $("#excelPreview");
        tbody.empty();

        if (!window.scalesReportData.displayData.length) {
            tbody.append(`
                <tr>
                    <td colspan="21" class="px-4 py-8 text-center text-gray-500 dark:text-gray-400">
                        <i class="fas fa-info-circle text-4xl mb-2 block"></i>
                        No data loaded. Please upload an Excel file.
                    </td>
                </tr>
            `);
            return;
        }

        window.scalesReportData.displayData.forEach((rowData, index) => {
            const actualIndex = rowData.rowIndex || index + 3;

            // Calculate totals
            const scaleTotal = Array.from(rowData.scaleAmounts?.values() || []).reduce((a, b) => a + b, 0);
            const portTotal = Array.from(rowData.portAmounts?.values() || []).reduce((a, b) => a + b, 0);
            const totalExpense = scaleTotal + portTotal + (rowData.otherValue || 0) + (rowData.policeValue || 0);
            rowData.totalExpense = totalExpense;
            // Determine row styling
            let rowClass = '';
            if (rowData.scaleFee === 0) {
                rowClass = 'bg-blue-50 dark:bg-blue-900/20';
            } else if (rowData.scaleFee > 20000) {
                rowClass = 'bg-yellow-50 dark:bg-yellow-900/20';
            }

            rowData.date = formatExcelDate(rowData.date) || getCurrentDate();
            let tr = '';
            console.log(rowData.date)
            if (rowData.truckNo != "") {
                tr = `
                <tr class="${ rowClass } hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-colors duration-150" data-row-id="${ actualIndex }" data-row-index="${ index }">
                    <td class="text-center border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ index ?? '#' }</td>
                    
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">
                        <input type="text" 
                            class="date-input w-full rounded-lg border border-gray-300 dark:border-gray-600 px-4 py-2.5 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-primary-500 focus:border-transparent shadow-sm flatpickr-input"
                            value="${ rowData.date }"
                            data-row-id="${ actualIndex }"
                            data-row-index="${ index }">
                    </td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 font-medium text-gray-800 dark:text-gray-200">
                        ${ rowData.truckNo }
                    </td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.truckType ?? '' }</td>
                    <td class="text-center border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.routeNumber ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.cargoType ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.pickup ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.dropoff ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200 space-y-2">
                        <div class="font-semibold text-gray-900 dark:text-white">
                            ${ rowData.destination }
                        </div>
                        <div class="flex items-center gap-2 flex-wrap">
                            <div 
                                title="View Scale Fees"
                                class="badge-scales btn-show-scales cursor-pointer flex items-center gap-1 px-3 py-1 rounded-full bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300 hover:scale-105 transition"
                                data-row-id="${ actualIndex }"
                                data-row-index="${ index }"
                                data-destination="${ rowData.destination }"
                            >
                                <i class="fas fa-weight-hanging text-xs"></i>
                                <span>ជញ្ជីង</span>
                                <span class="ml-1 text-xs bg-blue-600 text-white px-2 rounded-full">
                                    ${ rowData.scaleIds?.length || 0 }
                                </span>
                            </div>
                            <div 
                                title="View Ports and Ben Fees"
                                class="badge-ports btn-show-ports cursor-pointer flex items-center gap-1 px-3 py-1 rounded-full bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300 hover:scale-105 transition"
                                data-row-id="${ actualIndex }"
                                data-row-index="${ index }"
                                data-destination="${ rowData.destination }"
                            >
                                <i class="fas fa-ship text-xs"></i>
                                <span>ផែ នឹង បេន</span>
                                <span class="ml-1 text-xs bg-green-600 text-white px-2 rounded-full">
                                    ${ rowData.portIds?.length || 0 }
                                </span>
                            </div>
                        </div>
                    </td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(rowData.cargoWeight, 2) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(rowData.truckWeight, 2) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right font-medium text-gray-800 dark:text-gray-200">${ formatNumber(rowData.totalWeight, 2) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right font-bold text-blue-600 dark:text-blue-400" id="scaleFeeDisplay-${ actualIndex }">${ formatNumber(scaleTotal, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-green-600 dark:text-green-400" id="portFeeDisplay-${ actualIndex }">${ formatNumber(portTotal, 0) }</td>

                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">
                        <input 
                            value="${ rowData.policeValue || 0 }"
                            class="police-input text-center w-full rounded-lg border border-gray-300 dark:border-gray-600 px-4 py-2.5 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-primary-500 focus:border-transparent shadow-sm"
                            data-row-id="${ actualIndex }"
                            data-row-index="${ index }">
                    </td>

                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">
                        <input 
                            value="${ rowData.otherValue || 0 }"
                            class="other-input text-center w-full rounded-lg border border-gray-300 dark:border-gray-600 px-4 py-2.5 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-primary-500 focus:border-transparent shadow-sm"
                            data-row-id="${ actualIndex }"
                            data-row-index="${ index }">
                    </td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200 total-expense" id="totalExpense-${ actualIndex }">${ formatNumber(rowData.totalExpense, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ rowData.nationalRoadValue }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.markValue }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">
                        <input 
                            value="${ rowData.noteValue || "" }"
                            class="note-input w-full rounded-lg border border-gray-300 dark:border-gray-600 px-4 py-2.5 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-primary-500 focus:border-transparent shadow-sm">
                    </td>
                </tr>
            `;
            }

            tbody.append(tr);
        });

        // Reattach event listeners
        attachOtherInputListeners();
        initializeDateInputs();
    }
    // ==================== END RENDER FUNCTION ====================

    function initializeDateInputs() {
        $('.date-input').flatpickr({
            dateFormat: "M d, Y",
            // maxDate: "today",
            onChange: function (selectedDates, dateStr, instance) {
                const $input = $(instance.input);
                const rowId = $input.data('row-id');
                const rowIndex = $input.data('row-index');

                // Update the data structure
                if (window.scalesReportData.displayData[rowIndex]) {
                    window.scalesReportData.displayData[rowIndex].date = dateStr;
                    saveToLocalStorage();
                }
            }
        });
    }

    function saveToLocalStorage() {
        try {
            const dataForStorage = {
                displayData: window.scalesReportData.displayData
                    .filter(item => item.truckNo && item.truckNo.trim() !== '') // ✅ keep only valid truckNo
                    .map(item => ({
                        ...item,
                        scaleAmounts: Object.fromEntries(item.scaleAmounts || new Map()),
                        portAmounts: Object.fromEntries(item.portAmounts || new Map())
                    })),
                timestamp: new Date().toISOString()
            };

            localStorage.setItem('scales-report', JSON.stringify(dataForStorage));
            console.log('Data saved to localStorage');
        } catch (error) {
            console.error('Error saving to localStorage:', error);
        }
    }

    // Load data from localStorage on page load
    function loadFromLocalStorage() {
        const savedData = localStorage.getItem('scales-report');
        if (savedData) {
            try {
                const parsed = JSON.parse(savedData);
                if (parsed.displayData) {
                    window.scalesReportData.displayData = parsed.displayData.map(item => ({
                        ...item,
                        scaleAmounts: new Map(Object.entries(item.scaleAmounts || {})),
                        portAmounts: new Map(Object.entries(item.portAmounts || {}))
                    }));
                    window.scalesReportData.timestamp = parsed.timestamp;
                }
                // Use the SINGLE render function
                renderTable();
                console.log('Data loaded from localStorage');
            } catch (error) {
                console.error('Error loading from localStorage:', error);
            }
        }
    }

    // Attach event listeners to other inputs
    function attachOtherInputListeners() {
        $('.other-input')
            .off('input')
            .on('input', function () {
                let cleanValue = $(this).val().replace(/[^0-9.]/g, '');
                const parts = cleanValue.split('.');
                if (parts.length > 2) {
                    cleanValue = parts[0] + '.' + parts.slice(1).join('');
                }
                $(this).val(cleanValue);

                const rowId = $(this).data('row-id');
                const rowIndex = $(this).data('row-index');
                const value = parseFloat(cleanValue) || 0;

                if (window.scalesReportData.displayData[rowIndex]) {
                    window.scalesReportData.displayData[rowIndex].otherValue = value;

                    const scaleTotal = Array.from(window.scalesReportData.displayData[rowIndex].scaleAmounts?.values() || []).reduce((a, b) => a + b, 0);
                    const portTotal = Array.from(window.scalesReportData.displayData[rowIndex].portAmounts?.values() || []).reduce((a, b) => a + b, 0);
                    const policeAmount = window.scalesReportData.displayData[rowIndex].policeValue || 0;
                    const totalExpense = scaleTotal + portTotal + value + policeAmount;

                    window.scalesReportData.displayData[rowIndex].totalExpense = totalExpense;
                    $(`#totalExpense-${ rowId }`).text(formatNumber(totalExpense, 0));
                    saveToLocalStorage();
                }
            });

        $('.police-input')
            .off('input')
            .on('input', function () {
                let cleanValue = $(this).val().replace(/[^0-9.]/g, '');
                const parts = cleanValue.split('.');
                if (parts.length > 2) {
                    cleanValue = parts[0] + '.' + parts.slice(1).join('');
                }
                $(this).val(cleanValue);

                const rowId = $(this).data('row-id');
                const rowIndex = $(this).data('row-index');
                const value = parseFloat(cleanValue) || 0;

                if (window.scalesReportData.displayData[rowIndex]) {
                    window.scalesReportData.displayData[rowIndex].policeValue = value;

                    const scaleTotal = Array.from(window.scalesReportData.displayData[rowIndex].scaleAmounts?.values() || []).reduce((a, b) => a + b, 0);
                    const portTotal = Array.from(window.scalesReportData.displayData[rowIndex].portAmounts?.values() || []).reduce((a, b) => a + b, 0);
                    const otherAmount = window.scalesReportData.displayData[rowIndex].otherValue || 0;
                    const totalExpense = scaleTotal + portTotal + value + otherAmount;

                    window.scalesReportData.displayData[rowIndex].totalExpense = totalExpense;

                    $(`#totalExpense-${ rowId }`).text(formatNumber(totalExpense, 0));
                    saveToLocalStorage();
                }
            });

        $('.note-input').off('input').on('input', function () {
            const row = $(this).closest('tr');
            const rowId = $(row).data('row-id');
            const rowIndex = $(row).data('row-index');
            const value = $(this).val() || "";

            if (window.scalesReportData.displayData[rowIndex]) {
                window.scalesReportData.displayData[rowIndex].noteValue = value;
                saveToLocalStorage();
            }
        });

        $('.date-input').off('change').on('change', function () {
            const rowId = $(this).data('row-id');
            const rowIndex = $(this).data('row-index');
            const value = $(this).val();

            if (window.scalesReportData.displayData[rowIndex]) {
                window.scalesReportData.displayData[rowIndex].date = value;
                saveToLocalStorage();
            }
        });
    }

    function formatExcelDate(value) {
        if (!value) return null;

        // Case 1: Excel serial number (number)
        if (typeof value === 'number') {
            const excelEpoch = new Date(1899, 11, 30);
            const date = new Date(excelEpoch.getTime() + value * 86400000);
            return date.toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric'
            });
        }

        // Case 2: string like "05-Nov-2026"
        if (typeof value === 'string') {
            const parsed = new Date(value);
            if (!isNaN(parsed)) {
                return parsed.toLocaleDateString('en-US', {
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric'
                });
            }
        }

        return null;
    }

    // ==================== PROCESS ROWS FUNCTION ====================
    async function processRowsWithTruckRoutes(rows, startIndex = 3) {
        const processedData = [];
        const batchSize = 10;

        // 🔹 Helper: filter + map + create Map
        const extractValidItems = (items, idKey) => {
            if (!items?.length) return { ids: [], amounts: new Map() };

            const valid = items.reduce((acc, item) => {
                const amount = parseFloat(item.amount);
                if (!isNaN(amount) && amount > 0) {
                    acc.ids.push(item[idKey]);
                    acc.amounts.set(item[idKey], amount);
                }
                return acc;
            }, { ids: [], amounts: new Map() });

            return valid;
        };

        for (let i = startIndex; i < rows.length; i += batchSize) {
            const batch = rows.slice(i, i + batchSize);

            const batchResults = await Promise.all(
                batch.map(async (r, index) => {
                    if (!r || !r[0]) return null;

                    const actualIndex = i + index;
                    const licensePlate = r[2]?.toString().trim();
                    if (!licensePlate) return null;

                    try {
                        const data = await getTruckRouteNumber(licensePlate);

                        return {
                            row: r,
                            index: actualIndex,
                            licensePlate: data?.found ? licensePlate : '',
                            routeNumber: data?.found ? data.routeNumber : null
                        };
                    } catch (err) {
                        console.error('Truck route error:', err);
                        return null;
                    }
                })
            );

            for (const result of batchResults) {
                if (!result) continue;

                const { row: r, index, licensePlate, routeNumber } = result;

                // 🔹 Basic fields
                const cargoWeight = cleanNumber(r[9]);
                const truckWeight = cleanNumber(r[10]);
                const totalWeight = calcTotalWeight(cargoWeight, truckWeight);

                const destination = r[8]?.toString() || '';
                const scaleStation = r[12]?.toString() || '';

                // 🔹 Parallel async (faster)
                const [scaleFee, destinationData] = await Promise.all([
                    calcScaleFee(licensePlate, scaleStation, totalWeight),
                    destination ? getDestinationData(destination).catch(err => {
                        console.error('Destination error:', err);
                        return null;
                    }) : Promise.resolve(null)
                ]);

                // 🔹 Extract scale/port data
                const { ids: scaleIds, amounts: scaleAmounts } =
                    extractValidItems(destinationData?.scales, 'scaleStationId');

                const { ids: portIds, amounts: portAmounts } =
                    extractValidItems(destinationData?.ports, 'portId');

                // 🔹 Final object
                processedData.push({
                    rowIndex: index,
                    no: r[0],
                    date: r[1],
                    truckNo: licensePlate,
                    truckType: r[3],
                    routeNumber,
                    cargoType: r[5],
                    pickup: r[6],
                    dropoff: r[7],
                    destination,
                    cargoWeight,
                    truckWeight,
                    totalWeight,
                    scaleFee,
                    benValue: cleanNumber(r[13]),
                    portValue: cleanNumber(r[14]),
                    policeValue: cleanNumber(r[15]),
                    otherValue: 0,
                    expenseValue: 0,
                    nationalRoadValue: r[18]?.toString() || '',
                    markValue: r[19]?.toString() || '',
                    noteValue: r[20]?.toString() || '',
                    scaleIds,
                    scaleAmounts,
                    portIds,
                    portAmounts
                });
            }

            // 🔹 Progress update
            const progress = Math.min(100, Math.round((i + batch.length) / rows.length * 100));
            $('#progressBar').css('width', progress + '%').attr('aria-valuenow', progress);
            $('#progressText').text(`Processing... ${ progress }%`);
        }

        // 🔹 Final updates
        window.scalesReportData.displayData = processedData;
        renderTable();
        saveToLocalStorage();

        return processedData;
    }
    // ==================== END PROCESS ROWS ====================

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

    async function getTruckRouteNumber(licensePlate) {
        if (!licensePlate) return null;
        if (truckRouteCache.has(licensePlate)) {
            return truckRouteCache.get(licensePlate);
        }
        try {
            const response = await fetch(`/api/v1/trucks/route/${ encodeURIComponent(licensePlate) }`);
            if (response.ok) {
                const data = await response.json();
                const routeNumber = data.routeNumber ? parseInt(data.routeNumber) : null;
                truckRouteCache.set(licensePlate, routeNumber);
                console.log({ data });
                return data.found ? data : null;
            }
        } catch (error) {
            console.error('Error fetching truck route:', error);
        }
        return null;
    }

    async function calcScaleFee(licensePlate, scaleStation, totalWeight) {
        try {
            const response = await fetch('/api/v1/scale-fee/calculate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
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
        return 20000;
    }

    function cleanNumber(value) {
        if (!value) return 0;
        if (typeof value === 'number') return value;
        return parseFloat(value.toString().replace(/[^\d.-]/g, '').replace(/,/g, '')) || 0;
    }

    function formatNumber(value, decimals = 2) {
        return new Intl.NumberFormat('en-US', {
            minimumFractionDigits: decimals,
            maximumFractionDigits: decimals
        }).format(value);
    }

    function calcTotalWeight(cargoWeight, truckWeight) {
        return (cargoWeight > 0 ? cargoWeight + truckWeight : 0);
    }

    function calculateGrandTotals() {
        let totalSelectedScales = 0;
        let totalSelectedPorts = 0;
        let totalExpenses = 0;

        $('[id^="scaleFeeDisplay-"]').each(function () {
            const text = $(this).text();
            const value = parseFloat(text.replace(/[^0-9.-]/g, '')) || 0;
            totalSelectedScales += value;
        });

        $('[id^="portFeeDisplay-"]').each(function () {
            const text = $(this).text();
            const value = parseFloat(text.replace(/[^0-9.-]/g, '')) || 0;
            totalSelectedPorts += value;
        });

        $('[id^="totalExpense-"]').each(function () {
            const text = $(this).text();
            const value = parseFloat(text.replace(/[^0-9.-]/g, '')) || 0;
            totalExpenses += value;
        });

        $('#grandTotalScales').text(formatNumber(totalSelectedScales, 0) + ' Riel');
        $('#grandTotalPorts').text(formatNumber(totalSelectedPorts, 0) + ' Riel');
        $('#grandTotalExpenses').text(formatNumber(totalExpenses, 0) + ' Riel');
        $('#grandTotalAll').text(formatNumber(totalSelectedScales + totalSelectedPorts, 0) + ' Riel');
    }


    // ==================== FILE UPLOAD HANDLER ====================
    $("#excelFile").on("change", async function (e) {
        const file = e.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        const $processBtn = $('#processFileBtn');
        const $loading = $('#loadingIndicator');
        const $progressContainer = $('#progressContainer');

        reader.onload = async function (event) {
            try {
                $processBtn.prop('disabled', true);
                $loading.removeClass('hidden');
                $progressContainer.removeClass('hidden');

                const data = new Uint8Array(event.target.result);
                const workbook = XLSX.read(data, { type: 'array' });
                const sheetName = workbook.SheetNames[0];
                const sheet = workbook.Sheets[sheetName];
                const rows = XLSX.utils.sheet_to_json(sheet, { header: 1 });

                const processedData = await processRowsWithTruckRoutes(rows, 3);

                if (processedData.length === 0) {
                    $("#excelPreview").empty();
                    $("#excelPreview").append(`
                        <tr>
                            <td colspan="21" class="px-4 py-8 text-center text-gray-500 dark:text-gray-400">
                                <i class="fas fa-exclamation-circle text-4xl mb-2 block"></i>
                                No valid data found in the Excel file
                            </td>
                        </tr>
                    `);
                }

            } catch (error) {
                console.error('Error processing file:', error);
                $("#excelPreview").empty();
                $("#excelPreview").append(`
                    <tr>
                        <td colspan="21" class="px-4 py-8 text-center text-red-500 dark:text-red-400">
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
    // ==================== END FILE UPLOAD ====================

    // Modal functions (keep existing modal functions here)
    // ... [keep all your existing modal functions] ...


    /* -----------------------
    Modal Functions
    ----------------------- */
    window.openScalesModal = function (rowId, rowIndex, destination, scales) {
        const modal = document.getElementById('scalesModal');
        if (!modal) return;

        // Store current row info globally for modal access
        window.currentScaleRowId = rowId;
        window.currentScaleRowIndex = rowIndex;

        // Update destination display
        document.getElementById('scalesDestinationName').textContent = `Destination: ${ destination }`;

        // Populate scales table
        populateScalesTable(scales, rowId, rowIndex);

        // Show modal
        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';

        // Reset search
        document.getElementById('scaleSearch').value = '';
    };

    window.closeScalesModal = function () {
        const modal = document.getElementById('scalesModal');
        if (modal) {
            modal.classList.add('hidden');
            document.body.style.overflow = 'auto';
            window.currentScaleRowId = null;
            window.currentScaleRowIndex = null;
        }
    };

    window.openPortsModal = function (rowId, rowIndex, destination, ports) {
        const modal = document.getElementById('portsModal');
        if (!modal) return;

        // Store current row info globally for modal access
        window.currentPortRowId = rowId;
        window.currentPortRowIndex = rowIndex;

        // Update destination display
        document.getElementById('portsDestinationName').textContent = `Destination: ${ destination }`;

        // Populate ports table
        populatePortsTable(ports, rowId, rowIndex);

        // Show modal
        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';

        // Reset search
        document.getElementById('portSearch').value = '';
    };

    window.closePortsModal = function () {
        const modal = document.getElementById('portsModal');
        if (modal) {
            modal.classList.add('hidden');
            document.body.style.overflow = 'auto';
            window.currentPortRowId = null;
            window.currentPortRowIndex = null;
        }
    };

    // Event delegation for scale buttons
    $(document).on('click', '.btn-show-scales', async function () {
        const $btn = $(this);
        const rowId = $btn.data('row-id');
        const rowIndex = $btn.data('row-index');
        const destination = $btn.data('destination');

        // Show loading state
        $btn.prop('disabled', true);

        try {
            // Fetch destination data
            const destinationData = await getDestinationData(destination);

            // Open modal with scales
            openScalesModal(rowId, rowIndex, destination, destinationData?.scales || []);
        } catch (error) {
            console.error('Error loading scales:', error);
            alert('Failed to load scale data. Please try again.');
        } finally {
            // Reset button
            $btn.prop('disabled', false);
        }
    });

    // Event delegation for port buttons
    $(document).on('click', '.btn-show-ports', async function () {
        const $btn = $(this);
        const rowId = $btn.data('row-id');
        const rowIndex = $btn.data('row-index');
        const destination = $btn.data('destination');

        // Show loading state
        $btn.prop('disabled', true);

        try {
            // Fetch destination data
            const destinationData = await getDestinationData(destination);

            // Open modal with ports
            openPortsModal(rowId, rowIndex, destination, destinationData?.ports || []);
        } catch (error) {
            console.error('Error loading ports:', error);
            alert('Failed to load port data. Please try again.');
        } finally {
            // Reset button
            $btn.prop('disabled', false);
        }
    });

    // Populate scales table
    function populateScalesTable(scales, rowId, rowIndex) {
        const tbody = document.getElementById('scalesModalBody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!scales || scales.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="4" class="px-4 py-8 text-center text-gray-500 dark:text-gray-400">
                        <i class="fas fa-info-circle text-3xl mb-2 block"></i>
                        No scale stations available for this destination
                    </td>
                </tr>
            `;
            updateScalesCount();
            return;
        }

        // Get previously selected scales for this row
        const rowData = window.scalesReportData.displayData[rowIndex];
        const selectedScaleIds = (rowData?.scaleIds || []).map(id => parseInt(id, 10));
        console.log({ selectedScaleIds });
        scales.forEach(scale => {
            console.log({ scale });
            const isChecked = selectedScaleIds.includes(scale.scaleStationId);
            const row = document.createElement('tr');
            row.className = 'hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer transition-colors';
            if (isChecked) {
                row.classList.add('bg-blue-50', 'dark:bg-blue-900/20');
            }

            row.innerHTML = `
                <td class="border border-gray-300 dark:border-gray-700 px-4 py-3 text-center" onclick="event.stopPropagation()">
                    <input type="checkbox" class="scale-checkbox rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50" 
                           data-id="${ scale.scaleStationId }"
                           data-amount="${ scale.amount }"
                           data-name="${ scale.scaleStationName }"
                           ${ isChecked ? 'checked' : '' }>
                </td>
                <td class="border border-gray-300 dark:border-gray-700 px-4 py-3 text-gray-800 dark:text-gray-200 font-medium text-center">${ scale.scaleStationName }</td>
                <td class="border border-gray-300 dark:border-gray-700 px-4 py-3 text-gray-800 dark:text-gray-200 text-right">${ formatNumber(scale.amount, 0) } Riel</td>
            `;

            // Add click handler to row
            row.addEventListener('click', function (e) {
                const checkbox = this.querySelector('.scale-checkbox');
                if (checkbox) {
                    checkbox.checked = !checkbox.checked;
                    updateScaleRowHighlight(checkbox);
                    updateScalesTotal();
                    updateScalesCount();
                    updateScalesHeaderCheckbox();
                }
            });

            tbody.appendChild(row);
        });

        // Add change handlers to checkboxes
        document.querySelectorAll('.scale-checkbox').forEach(cb => {
            cb.addEventListener('change', function () {
                updateScaleRowHighlight(this);
                updateScalesTotal();
                updateScalesCount();
                updateScalesHeaderCheckbox();
            });
        });

        // Update header checkbox
        updateScalesHeaderCheckbox();
        updateScalesTotal();
        updateScalesCount();
    }

    // Populate ports table
    function populatePortsTable(ports, rowId, rowIndex) {
        const tbody = document.getElementById('portsModalBody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!ports || ports.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="4" class="px-4 py-8 text-center text-gray-500 dark:text-gray-400">
                        <i class="fas fa-info-circle text-3xl mb-2 block"></i>
                        No ports available for this destination
                    </td>
                </tr>
            `;
            updatePortsCount();
            return;
        }

        // Get previously selected ports for this row
        const rowData = window.scalesReportData.displayData[rowIndex];
        const selectedPortIds = (rowData?.portIds || []).map(id => parseInt(id, 10));

        ports.forEach(port => {
            const isChecked = selectedPortIds.includes(port.portId);
            const row = document.createElement('tr');
            row.className = 'hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer transition-colors';
            if (isChecked) {
                row.classList.add('bg-blue-50', 'dark:bg-blue-900/20');
            }

            row.innerHTML = `
                <td class="border border-gray-300 dark:border-gray-700 px-4 py-3 text-gray-800 dark:text-gray-200 font-medium text-center" onclick="event.stopPropagation()">
                    <input type="checkbox" class="text-center port-checkbox rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50" 
                           data-id="${ port.portId }"
                           data-amount="${ port.amount }"
                           data-name="${ port.portName }"
                           ${ isChecked ? 'checked' : '' }>
                </td>
                <td class="border border-gray-300 dark:border-gray-700 px-4 py-3 text-gray-800 dark:text-gray-200 font-medium text-center">${ port.portName }</td>
                <td class="border border-gray-300 dark:border-gray-700 px-4 py-3 text-gray-800 dark:text-gray-200 font-medium text-right">${ formatNumber(port.amount, 0) } Riel</td>
            `;

            // Add click handler to row
            row.addEventListener('click', function (e) {
                const checkbox = this.querySelector('.port-checkbox');
                if (checkbox) {
                    checkbox.checked = !checkbox.checked;
                    updatePortRowHighlight(checkbox);
                    updatePortsTotal();
                    updatePortsCount();
                }
            });

            tbody.appendChild(row);
        });

        // Add change handlers to checkboxes
        document.querySelectorAll('.port-checkbox').forEach(cb => {
            cb.addEventListener('change', function () {
                updatePortRowHighlight(this);
                updatePortsTotal();
                updatePortsCount();
            });
        });

        // Update header checkbox
        updatePortsHeaderCheckbox();
        updatePortsTotal();
        updatePortsCount();
    }

    // Update scale row highlight
    function updateScaleRowHighlight(checkbox) {
        const row = checkbox.closest('tr');
        if (checkbox.checked) {
            row.classList.add('bg-blue-50', 'dark:bg-blue-500');
        } else {
            row.classList.remove('bg-blue-50', 'dark:bg-blue-500');
        }
    }

    // Update port row highlight
    function updatePortRowHighlight(checkbox) {
        const row = checkbox.closest('tr');
        if (checkbox.checked) {
            row.classList.add('bg-blue-50', 'dark:bg-blue-500');
        } else {
            row.classList.remove('bg-blue-50', 'dark:bg-blue-500');
        }
    }

    // Update scales total
    function updateScalesTotal() {
        let total = 0;
        document.querySelectorAll('.scale-checkbox:checked').forEach(cb => {
            total += parseFloat(cb.dataset.amount) || 0;
        });
        document.getElementById('totalScalesAmount').textContent = formatNumber(total, 0) + ' Riel';
    }

    // Update ports total
    function updatePortsTotal() {
        let total = 0;
        document.querySelectorAll('.port-checkbox:checked').forEach(cb => {
            total += parseFloat(cb.dataset.amount) || 0;
        });
        document.getElementById('totalPortsAmount').textContent = formatNumber(total, 0) + ' Riel';
    }

    // Update scales count
    function updateScalesCount() {
        const checked = document.querySelectorAll('.scale-checkbox:checked').length;
        document.getElementById('selectedScalesItemsCount').textContent = checked;
        updateScalesHeaderCheckbox();
    }

    // Update ports count
    function updatePortsCount() {
        const checked = document.querySelectorAll('.port-checkbox:checked').length;
        document.getElementById('selectedPortsItemsCount').textContent = checked;
        updatePortsHeaderCheckbox();
    }

    // Update scales header checkbox
    function updateScalesHeaderCheckbox() {
        const all = document.querySelectorAll('.scale-checkbox');
        const checked = document.querySelectorAll('.scale-checkbox:checked');

        const selectAll = document.getElementById('selectAllScales');
        if (!selectAll) return;

        if (checked.length === 0) {
            selectAll.checked = false;
            selectAll.indeterminate = false;
        } else if (checked.length === all.length) {
            selectAll.checked = true;
            selectAll.indeterminate = false;
        } else {
            selectAll.checked = false;
            selectAll.indeterminate = true;
        }
    }

    // Update ports header checkbox
    function updatePortsHeaderCheckbox() {
        const headerCheckbox = document.getElementById('portHeaderCheckbox');
        if (!headerCheckbox) return;

        const checkboxes = document.querySelectorAll('.port-checkbox');
        const checkedCount = document.querySelectorAll('.port-checkbox:checked').length;

        if (checkedCount === 0) {
            headerCheckbox.checked = false;
            headerCheckbox.indeterminate = false;
        } else if (checkedCount === checkboxes.length) {
            headerCheckbox.checked = true;
            headerCheckbox.indeterminate = false;
        } else {
            headerCheckbox.indeterminate = true;
        }
    }

    // Save scales selection
    window.saveScalesSelection = function () {
        const rowId = window.currentScaleRowId;
        const rowIndex = window.currentScaleRowIndex;
        if (!rowId || rowIndex === undefined) return;

        const selectedScales = [];
        document.querySelectorAll('.scale-checkbox:checked').forEach(cb => {
            const id = cb.dataset.id;

            if (!id || id === "undefined" || id === "null") return;
            selectedScales.push({
                id: cb.dataset.id,
                name: cb.dataset.name,
                amount: parseFloat((cb.dataset.amount || '0').replace(/,/g, '')) || 0
            });
        });

        // Update the data structure
        if (window.scalesReportData.displayData[rowIndex]) {
            const rowData = window.scalesReportData.displayData[rowIndex];
            rowData.scaleIds = selectedScales.map(s => s.id);
            rowData.scaleAmounts = new Map(selectedScales.map(s => [s.id, s.amount]));

            // Calculate total
            const total = selectedScales.reduce((sum, s) => sum + s.amount, 0);
            $(`#scaleFeeDisplay-${ rowId }`).text(formatNumber(total, 0));

            // Update total expense for this row
            const portTotal = Array.from(rowData.portAmounts?.values() || []).reduce((a, b) => a + b, 0);
            const totalExpense = total + portTotal + (rowData.otherValue || 0);
            $(`#totalExpense-${ rowId }`).text(formatNumber(totalExpense, 0));

            // Save to localStorage
            saveToLocalStorage();
        }

        closeScalesModal();
        loadFromLocalStorage();
    };

    // Save ports selection
    window.savePortsSelection = function () {
        const rowId = window.currentPortRowId;
        const rowIndex = window.currentPortRowIndex;
        if (!rowId || rowIndex === undefined) return;

        const selectedPorts = [];
        document.querySelectorAll('.port-checkbox:checked').forEach(cb => {
            const id = cb.dataset.id;

            if (!id || id === "undefined" || id === "null") return;

            selectedPorts.push({
                id,
                name: cb.dataset.name,
                amount: parseFloat((cb.dataset.amount || '0').replace(/,/g, '')) || 0
            });
        });

        // Update the data structure
        if (window.scalesReportData.displayData[rowIndex]) {
            const rowData = window.scalesReportData.displayData[rowIndex];
            rowData.portIds = selectedPorts.map(p => p.id);
            rowData.portAmounts = new Map(selectedPorts.map(p => [p.id, p.amount]));

            // Calculate total
            const total = selectedPorts.reduce((sum, p) => sum + p.amount, 0);
            $(`#portFeeDisplay-${ rowId }`).text(formatNumber(total, 0));

            // Update total expense for this row
            const scaleTotal = Array.from(rowData.scaleAmounts?.values() || []).reduce((a, b) => a + b, 0);
            const totalExpense = scaleTotal + total + (rowData.otherValue || 0);
            $(`#totalExpense-${ rowId }`).text(formatNumber(totalExpense, 0));
            saveToLocalStorage();
        }

        closePortsModal();
        loadFromLocalStorage();
    };
    // Initialize header checkbox handlers
    $(document).on('change', '#scaleHeaderCheckbox', function () {
        document.querySelectorAll('.scale-checkbox').forEach(cb => {
            cb.checked = this.checked;
            updateScaleRowHighlight(cb);
        });
        updateScalesTotal();
        updateScalesCount();
    });

    $(document).on('change', '#portHeaderCheckbox', function () {
        document.querySelectorAll('.port-checkbox').forEach(cb => {
            cb.checked = this.checked;
            updatePortRowHighlight(cb);
        });
        updatePortsTotal();
        updatePortsCount();
    });

    // Select all buttons
    $('#selectAllScales').on('click', function () {
        const isChecked = this.checked;

        document.querySelectorAll('.scale-checkbox').forEach(cb => {
            cb.checked = isChecked;
            updateScaleRowHighlight(cb);
        });

        updateScalesTotal();
        updateScalesCount();
    });

    $('#deselectAllScales').on('click', function () {
        document.querySelectorAll('.scale-checkbox').forEach(cb => {
            cb.checked = false;
            updateScaleRowHighlight(cb);
        });
        updateScalesTotal();
        updateScalesCount();
    });

    $('#selectAllPorts').on('click', function () {
        const isChecked = this.checked;
        document.querySelectorAll('.port-checkbox').forEach(cb => {
            cb.checked = isChecked;
            updatePortRowHighlight(cb);
        });
        updatePortsTotal();
        updatePortsCount();
    });

    $('#deselectAllPorts').on('click', function () {
        document.querySelectorAll('.port-checkbox').forEach(cb => {
            cb.checked = false;
            updatePortRowHighlight(cb);
        });
        updatePortsTotal();
        updatePortsCount();
    });

    // Search functionality
    $('#scaleSearch').on('input', function () {
        const searchTerm = this.value.toLowerCase();
        $('#scalesModalBody tr').each(function () {
            const name = $(this).find('td:nth-child(2)').text().toLowerCase();
            $(this).toggle(name.includes(searchTerm));
        });
    });

    $('#portSearch').on('input', function () {
        const searchTerm = this.value.toLowerCase();
        $('#portsModalBody tr').each(function () {
            const name = $(this).find('td:nth-child(2)').text().toLowerCase();
            $(this).toggle(name.includes(searchTerm));
        });
    });


    /* ========================
       SUBMIT
    ======================== */
    $('#submitReportBtn').on('click', async function () {
        const saved = localStorage.getItem('scales-report');
        if (!saved) return alert('No data');

        try {
            const res = await fetch('/api/v1/scale-reports/submit', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: saved
            });

            if (!res.ok) throw new Error('Submit failed');

            alert('Submitted successfully');
            localStorage.removeItem('scales-report');
            window.scalesReportData = { displayData: [], timestamp: null };
            renderTable();
            $('#excelFile').val('');
            window.location.reload();
        } catch (err) {
            alert(err.message);
        }
    });

    // Clear data button
    $('#clearDataBtn').on('click', function () {
        if (!confirm('Clear all data?')) return;
        localStorage.removeItem('scales-report');
        window.scalesReportData = { displayData: [], timestamp: null };
        renderTable();
        $('#excelFile').val('');
        window.location.reload();
    });

    // Load data from localStorage on page load
    loadFromLocalStorage();
});