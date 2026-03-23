$(function () {
    // Cache for truck route numbers to avoid repeated API calls
    const truckRouteCache = new Map();
    const destinationCache = new Map();

    // Main data structure for storing all report data
    window.scalesReportData = {
        displayData: [], // Array of row objects with their selections
        timestamp: null
    };

    // Initialize datepicker with default to today if null
    $(".datepicker").flatpickr({
        dateFormat: "M d, Y",
        maxDate: "today",
        defaultDate: "today" // Set default to today if no date selected
    });

    // Load data from localStorage on page load
    function loadFromLocalStorage() {
        const savedData = localStorage.getItem('scales-report');
        if (savedData) {
            try {
                const parsed = JSON.parse(savedData);

                // Restore the data structure
                if (parsed.displayData) {
                    window.scalesReportData.displayData = parsed.displayData.map(item => ({
                        ...item,
                        scaleAmounts: new Map(Object.entries(item.scaleAmounts || {})),
                        portAmounts: new Map(Object.entries(item.portAmounts || {}))
                    }));
                    window.scalesReportData.timestamp = parsed.timestamp;
                }

                // Render the saved data
                renderSavedData();
                console.log('Data loaded from localStorage');
            } catch (error) {
                console.error('Error loading from localStorage:', error);
            }
        }
    }

    // Save data to localStorage
    function saveToLocalStorage() {
        try {
            // Prepare data for storage (convert Maps to objects)
            const dataForStorage = {
                displayData: window.scalesReportData.displayData.map(item => ({
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

    // Render saved data to table
    function renderSavedData() {
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
            const totalExpense = scaleTotal + portTotal + (rowData.otherValue || 0);

            // Determine row styling
            let rowClass = '';
            if (rowData.scaleFee === 0) {
                rowClass = 'bg-blue-50 dark:bg-blue-900/20';
            } else if (rowData.scaleFee > 20000) {
                rowClass = 'bg-yellow-50 dark:bg-yellow-900/20';
            }

            let tr = `
                <tr class="${ rowClass } hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-colors duration-150" data-row-id="${ actualIndex }" data-row-index="${ index }">
                    <td class="text-center border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.no ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.date ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 font-medium text-gray-800 dark:text-gray-200">
                        ${ rowData.truckNo }
                    </td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.truckType ?? '' }</td>
                    <td class="text-center border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.routeNumber ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.cargoType ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.pickup ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.dropoff ?? '' }</td>
     

                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200 space-x-2">

                        <span class="font-medium">
                            ${ rowData.destination }
                        </span>

                        <button class="btn-show-scales text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 underline"
                            data-row-id="${ actualIndex }"
                            data-row-index="${ index }"
                            data-destination="${ rowData.destination }">
                            Scales
                        </button>

                        <button class="btn-show-ports text-green-600 hover:text-green-800 dark:text-green-400 dark:hover:text-green-300 underline text-xs"
                            data-row-id="${ actualIndex }"
                            data-row-index="${ index }"
                            data-destination="${ rowData.destination }">
                            Ports
                        </button>

                    </td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(rowData.cargoWeight, 2) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(rowData.truckWeight, 2) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right font-medium text-gray-800 dark:text-gray-200">${ formatNumber(rowData.totalWeight, 2) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right font-bold text-blue-600 dark:text-blue-400" id="scaleFeeDisplay-${ actualIndex }">${ formatNumber(scaleTotal, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-green-600 dark:text-green-400" id="portFeeDisplay-${ actualIndex }">${ formatNumber(portTotal, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(rowData.policeValue, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">
                        <input 
                            value="${ rowData.otherValue || 0 }"
                            class="other-input w-full rounded-lg border border-gray-300 dark:border-gray-600 px-4 py-2.5 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-primary-500 focus:border-transparent shadow-sm"
                            data-row-id="${ actualIndex }"
                            data-row-index="${ index }">
                    </td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200 total-expense" id="totalExpense-${ actualIndex }">${ formatNumber(totalExpense, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(rowData.nationalRoadValue, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.markValue }</td>
                    <td class="px-4 py-2 text-gray-800 dark:text-gray-200">${ rowData.noteValue }</td>
                </tr>
            `;
            tbody.append(tr);
        });

        // Reattach event listeners for other inputs
        attachOtherInputListeners();
        calculateGrandTotals();
    }

    // Attach event listeners to other inputs
    function attachOtherInputListeners() {
        $('.other-input').off('input').on('input', function () {
            const rowId = $(this).data('row-id');
            const rowIndex = $(this).data('row-index');
            const value = parseFloat($(this).val()) || 0;

            // Update the data structure
            if (window.scalesReportData.displayData[rowIndex]) {
                window.scalesReportData.displayData[rowIndex].otherValue = value;

                // Update total expense for this row
                const scaleTotal = Array.from(window.scalesReportData.displayData[rowIndex].scaleAmounts?.values() || []).reduce((a, b) => a + b, 0);
                const portTotal = Array.from(window.scalesReportData.displayData[rowIndex].portAmounts?.values() || []).reduce((a, b) => a + b, 0);
                const totalExpense = scaleTotal + portTotal + value;

                $(`#totalExpense-${ rowId }`).text(formatNumber(totalExpense, 0));

                // Save to localStorage
                saveToLocalStorage();

                // Update grand totals
                calculateGrandTotals();
            }
        });
    }

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

        return 20000; // Default fee
    }

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
        $btn.prop('disabled', true).text('Loading...');

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
            $btn.prop('disabled', false).text("Scales");
        }
    });

    // Event delegation for port buttons
    $(document).on('click', '.btn-show-ports', async function () {
        const $btn = $(this);
        const rowId = $btn.data('row-id');
        const rowIndex = $btn.data('row-index');
        const destination = $btn.data('destination');

        // Show loading state
        $btn.prop('disabled', true).text('Loading...');

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
            $btn.prop('disabled', false).text('Ports');
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
        const selectedScaleIds = rowData?.scaleIds || [];

        scales.forEach(scale => {
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
        const selectedPortIds = rowData?.portIds || [];

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

            // Update grand totals
            calculateGrandTotals();
        }

        closeScalesModal();
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

            // Save to localStorage
            saveToLocalStorage();

            // Update grand totals
            calculateGrandTotals();
        }

        closePortsModal();
    };

    // Calculate grand totals
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

    // Make grand totals function globally available
    window.calculateGrandTotals = calculateGrandTotals;

    /* -----------------------
Batch Process Rows with Truck Route Numbers
----------------------- */
    async function processRowsWithTruckRoutes(rows, tbody, startIndex = 3) {
        const processedData = [];
        const batchSize = 10;

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
                let scaleStation = r[12]?.toString() || "";
                let destination = r[8]?.toString() || "";
                let benValue = cleanNumber(r[13]);
                let portValue = cleanNumber(r[14]);
                let policeValue = cleanNumber(r[15]);
                let otherValue = cleanNumber(r[16]);
                let expenseValue = cleanNumber(0);
                let nationalRoadValue = cleanNumber(r[18]);
                let markValue = r[19]?.toString() || "";
                let noteValue = r[20]?.toString() || "";

                // Calculate scale fee
                let scaleFee = await calcScaleFee(licensePlate, scaleStation, totalWeight);

                // Fetch destination data for auto-selection
                let destinationData = null;
                let scaleIds = [];
                let scaleAmounts = new Map();
                let portIds = [];
                let portAmounts = new Map();

                if (destination) {
                    try {
                        destinationData = await getDestinationData(destination);

                        // Auto-set scale data
                        if (destinationData?.scales && destinationData.scales.length > 0) {
                            scaleIds = destinationData.scales.map(scale => scale.scaleStationId);
                            scaleAmounts = new Map(
                                destinationData.scales.map(scale => [
                                    scale.scaleStationId,
                                    parseFloat(scale.amount) || 0
                                ])
                            );
                        }

                        // Auto-set port data
                        if (destinationData?.ports && destinationData.ports.length > 0) {
                            portIds = destinationData.ports.map(port => port.portId);
                            portAmounts = new Map(
                                destinationData.ports.map(port => [
                                    port.portId,
                                    parseFloat(port.amount) || 0
                                ])
                            );
                        }
                    } catch (error) {
                        console.error('Error fetching destination data for auto-selection:', error);
                    }
                }

                const rowData = {
                    rowIndex: actualIndex,
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
                    otherValue: 0, // Initialize as 0
                    expenseValue: expenseValue,
                    nationalRoadValue: nationalRoadValue,
                    markValue: markValue,
                    noteValue: noteValue,
                    // Selection data - auto-set from destination
                    scaleIds: scaleIds,
                    scaleAmounts: scaleAmounts,
                    portIds: portIds,
                    portAmounts: portAmounts
                };

                processedData.push(rowData);

                // Calculate totals for display
                const scaleTotal = Array.from(scaleAmounts.values()).reduce((a, b) => a + b, 0);
                const portTotal = Array.from(portAmounts.values()).reduce((a, b) => a + b, 0);
                const totalExpense = scaleTotal + portTotal;

                // Determine row styling
                let rowClass = '';
                if (scaleFee === 0) {
                    rowClass = 'bg-blue-50 dark:bg-blue-900/20';
                } else if (scaleFee > 20000) {
                    rowClass = 'bg-yellow-50 dark:bg-yellow-900/20';
                }

                let tr = `
                <tr class="${ rowClass } hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-colors duration-150" data-row-id="${ actualIndex }" data-row-index="${ processedData.length - 1 }">
                    <td class="text-center border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[0] ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[1] ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 font-medium text-gray-800 dark:text-gray-200">
                        ${ licensePlate }
                    </td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[3] ?? '' }</td>
                    <td class="text-center border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ routeNumber ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[5] ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[6] ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ r[7] ?? '' }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200 space-x-2">

                        <span class="font-medium">
                            ${ destination }
                        </span>

                        <button class="btn-show-scales text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 underline"
                            data-row-id="${ actualIndex }"
                            data-row-index="${ processedData.length - 1 }"
                            data-destination="${ destination }">
                            Scales
                        </button>

                        <button class="btn-show-ports text-green-600 hover:text-green-800 dark:text-green-400 dark:hover:text-green-300 underline text-xs"
                            data-row-id="${ actualIndex }"
                            data-row-index="${ processedData.length - 1 }"
                            data-destination="${ destination }">
                            Ports
                        </button>

                    </td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(cargoWeight, 2) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(truckWeight, 2) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right font-medium text-gray-800 dark:text-gray-200">${ formatNumber(totalWeight, 2) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right font-bold text-blue-600 dark:text-blue-400" id="scaleFeeDisplay-${ actualIndex }">${ formatNumber(scaleTotal, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-green-600 dark:text-green-400" id="portFeeDisplay-${ actualIndex }">${ formatNumber(portTotal, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(policeValue, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">
                        <input 
                            value="0"
                            class="other-input w-full rounded-lg border border-gray-300 dark:border-gray-600 px-4 py-2.5 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-primary-500 focus:border-transparent shadow-sm"
                            data-row-id="${ actualIndex }"
                            data-row-index="${ processedData.length - 1 }">
                    </td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200 total-expense" id="totalExpense-${ actualIndex }">${ formatNumber(totalExpense, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-right text-gray-800 dark:text-gray-200">${ formatNumber(nationalRoadValue, 0) }</td>
                    <td class="border border-gray-200 dark:border-gray-700 px-4 py-2 text-gray-800 dark:text-gray-200">${ markValue }</td>
                    <td class="px-4 py-2 text-gray-800 dark:text-gray-200">${ noteValue }</td>
                </tr>
            `;

                tbody.append(tr);
            }

            // Update progress
            const progress = Math.min(100, Math.round((i + batch.length) / rows.length * 100));
            $('#progressBar').css('width', progress + '%').attr('aria-valuenow', progress);
            $('#progressText').text(`Processing... ${ progress }%`);
        }

        // Update the global data structure
        window.scalesReportData.displayData = processedData;

        // Attach other input listeners
        attachOtherInputListeners();

        return processedData;
    }

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

                const data = new Uint8Array(event.target.result);
                const workbook = XLSX.read(data, { type: 'array' });
                const sheetName = workbook.SheetNames[0];
                const sheet = workbook.Sheets[sheetName];
                const rows = XLSX.utils.sheet_to_json(sheet, { header: 1 });

                const tbody = $("#excelPreview");
                tbody.empty();

                // Process rows
                const scaleFeeData = await processRowsWithTruckRoutes(rows, tbody, 3);

                if (scaleFeeData.length === 0) {
                    tbody.empty();
                    tbody.append(`
                        <tr>
                            <td colspan="21" class="px-4 py-8 text-center text-gray-500 dark:text-gray-400">
                                <i class="fas fa-exclamation-circle text-4xl mb-2 block"></i>
                                No valid data found in the Excel file
                            </td>
                        </tr>
                    `);
                }

                updateStatistics(scaleFeeData);
                calculateGrandTotals();

                // Save to localStorage after processing
                saveToLocalStorage();

            } catch (error) {
                console.error('Error processing file:', error);

                const tbody = $("#excelPreview");
                tbody.empty();
                tbody.append(`
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
        } catch (err) {
            alert(err.message);
        }
    });

    /* ========================
       CLEAR DATA
    ======================== */
    $('#clearDataBtn').on('click', function () {
        if (!confirm('Clear all data?')) return;

        localStorage.removeItem('scales-report');
        window.scalesReportData = { displayData: [], timestamp: null };
        renderSavedData();
    });


    // Load data from localStorage on page load
    loadFromLocalStorage();

    // Add clear localStorage button functionality (optional)
    $('#clearLocalStorage').on('click', function () {
        if (confirm('Are you sure you want to clear all saved data?')) {
            localStorage.removeItem('scales-report');
            window.scalesReportData = {
                displayData: [],
                timestamp: null
            };
            $('#excelPreview').empty();
            $('#excelPreview').append(`
                <tr>
                    <td colspan="21" class="px-4 py-8 text-center text-gray-500 dark:text-gray-400">
                        <i class="fas fa-info-circle text-4xl mb-2 block"></i>
                        No data loaded. Please upload an Excel file.
                    </td>
                </tr>
            `);
            calculateGrandTotals();
            alert('Data cleared successfully');
        }
    });
});