// Dashboard Charts Module
class DashboardCharts {
    constructor(stats, trucks) {
        this.stats = stats;
        this.trucks = trucks;
        this.oilChart = null;
        this.fatChart = null;
        this.priorityChart = null;
        this.maintenanceChart = null;
    }

    initCharts() {
        this.renderOilChart();
        this.renderFatChart();
        this.renderPriorityChart();
        this.renderMaintenanceChart();
        this.renderPriorityTable();
        this.renderNoMaintenanceTable();
    }

    renderOilChart() {
        const ctx = document.getElementById('oilChart').getContext('2d');
        
        if (this.oilChart) {
            this.oilChart.destroy();
        }

        this.oilChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['DANGER', 'WARNING', 'NORMAL', 'UNKNOWN'],
                datasets: [{
                    data: [
                        this.stats.oil.danger,
                        this.stats.oil.warning,
                        this.stats.oil.normal,
                        this.stats.oil.unknown
                    ],
                    backgroundColor: [
                        '#ef4444',
                        '#f59e0b',
                        '#10b981',
                        '#6b7280'
                    ],
                    borderColor: [
                        '#dc2626',
                        '#d97706',
                        '#059669',
                        '#4b5563'
                    ],
                    borderWidth: 2,
                    hoverOffset: 15
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Oil Change Status',
                        font: {
                            size: 16
                        }
                    },
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true,
                            font: {
                                size: 12
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.raw || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = Math.round((value / total) * 100);
                                return `${label}: ${value} (${percentage}%)`;
                            }
                        }
                    }
                },
                cutout: '60%'
            }
        });
    }

    renderFatChart() {
        const ctx = document.getElementById('fatChart').getContext('2d');
        
        if (this.fatChart) {
            this.fatChart.destroy();
        }

        this.fatChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['DANGER', 'WARNING', 'NORMAL', 'UNKNOWN'],
                datasets: [{
                    data: [
                        this.stats.fat.danger,
                        this.stats.fat.warning,
                        this.stats.fat.normal,
                        this.stats.fat.unknown
                    ],
                    backgroundColor: [
                        '#ef4444',
                        '#f59e0b',
                        '#10b981',
                        '#6b7280'
                    ],
                    borderColor: [
                        '#dc2626',
                        '#d97706',
                        '#059669',
                        '#4b5563'
                    ],
                    borderWidth: 2,
                    hoverOffset: 15
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Fats Shoot Status',
                        font: {
                            size: 16
                        }
                    },
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true,
                            font: {
                                size: 12
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.raw || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = Math.round((value / total) * 100);
                                return `${label}: ${value} (${percentage}%)`;
                            }
                        }
                    }
                },
                cutout: '60%'
            }
        });
    }

    renderPriorityChart() {
        const ctx = document.getElementById('priorityChart').getContext('2d');
        
        if (this.priorityChart) {
            this.priorityChart.destroy();
        }

        // Get priority trucks (top 10 with highest priority)
        const priorityTrucks = this.getPriorityTrucks().slice(0, 10);
        
        const labels = priorityTrucks.map(truck => truck.licensePlate);
        const oilPriorities = priorityTrucks.map(truck => this.getOilPriority(truck));
        const fatPriorities = priorityTrucks.map(truck => this.getFatPriority(truck));

        this.priorityChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Oil Priority',
                        data: oilPriorities,
                        backgroundColor: 'rgba(59, 130, 246, 0.7)',
                        borderColor: 'rgb(59, 130, 246)',
                        borderWidth: 1
                    },
                    {
                        label: 'Fat Priority',
                        data: fatPriorities,
                        backgroundColor: 'rgba(245, 158, 11, 0.7)',
                        borderColor: 'rgb(245, 158, 11)',
                        borderWidth: 1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Top 10 Priority Trucks',
                        font: {
                            size: 16
                        }
                    },
                    legend: {
                        position: 'top',
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const priority = context.raw;
                                let status = '';
                                if (priority === 1) status = 'DANGER';
                                else if (priority === 2) status = 'WARNING';
                                else if (priority === 3) status = 'NORMAL';
                                else status = 'UNKNOWN';
                                
                                return `${context.dataset.label}: ${status}`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        reverse: true, // Lower number (higher priority) at top
                        ticks: {
                            callback: function(value) {
                                if (value === 1) return 'DANGER';
                                if (value === 2) return 'WARNING';
                                if (value === 3) return 'NORMAL';
                                if (value === 4) return 'UNKNOWN';
                                return value;
                            }
                        }
                    },
                    x: {
                        ticks: {
                            maxRotation: 45,
                            minRotation: 45
                        }
                    }
                }
            }
        });
    }

    renderMaintenanceChart() {
        const ctx = document.getElementById('maintenanceChart').getContext('2d');
        
        if (this.maintenanceChart) {
            this.maintenanceChart.destroy();
        }

        this.maintenanceChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Never Oil Change', 'Never Fats Shoot', 'Never Both'],
                datasets: [{
                    label: 'Number of Trucks',
                    data: [
                        this.stats.noOilReports,
                        this.stats.noFatReports,
                        this.stats.noReportsAtAll
                    ],
                    backgroundColor: [
                        'rgba(249, 115, 22, 0.7)',
                        'rgba(245, 158, 11, 0.7)',
                        'rgba(147, 51, 234, 0.7)'
                    ],
                    borderColor: [
                        'rgb(249, 115, 22)',
                        'rgb(245, 158, 11)',
                        'rgb(147, 51, 234)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Trucks Without Maintenance History',
                        font: {
                            size: 16
                        }
                    },
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    }

    renderPriorityTable() {
        const tableBody = $('#priorityTableBody');
        const priorityTrucks = this.getPriorityTrucks().slice(0, 10);

        if (priorityTrucks.length === 0) {
            tableBody.html(`
                <tr>
                    <td colspan="4" class="px-4 py-4 text-center text-gray-500">
                        No priority trucks found
                    </td>
                </tr>
            `);
            return;
        }

        let tableHtml = '';
        priorityTrucks.forEach(truck => {
            const oilPriority = this.getOilPriority(truck);
            const fatPriority = this.getFatPriority(truck);
            const oilStatus = this.getPriorityText(oilPriority);
            const fatStatus = this.getPriorityText(fatPriority);
            const oilStatusClass = this.getStatusClass(oilPriority);
            const fatStatusClass = this.getStatusClass(fatPriority);

            tableHtml += `
                <tr>
                    <td class="px-4 py-3 whitespace-nowrap text-sm font-medium">
                        ${truck.licensePlate || 'Unknown'}
                    </td>
                    <td class="px-4 py-3 whitespace-nowrap text-sm">
                        <span class="status status-badge ${oilStatusClass}">${oilStatus}</span>
                    </td>
                    <td class="px-4 py-3 whitespace-nowrap text-sm">
                        <span class="status status-badge ${fatStatusClass}">${fatStatus}</span>
                    </td>
                    <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                        ${truck.currentKmFormat || '0 km'}
                    </td>
                </tr>
            `;
        });

        tableBody.html(tableHtml);
    }

    renderNoMaintenanceTable() {
        const tableBody = $('#noMaintenanceTableBody');
        const noMaintenanceTrucks = this.getTrucksWithoutMaintenance();

        if (noMaintenanceTrucks.length === 0) {
            tableBody.html(`
                <tr>
                    <td colspan="4" class="px-4 py-4 text-center text-gray-500">
                        All trucks have maintenance records
                    </td>
                </tr>
            `);
            return;
        }

        let tableHtml = '';
        noMaintenanceTrucks.forEach(truck => {
            const hasOilReports = truck.truckOilsReports && truck.truckOilsReports.length > 0;
            const hasFatReports = truck.truckFatsReports && truck.truckFatsReports.length > 0;

            tableHtml += `
                <tr>
                    <td class="px-4 py-3 whitespace-nowrap text-sm font-medium">
                        ${truck.licensePlate || 'Unknown'}
                    </td>
                    <td class="px-4 py-3 whitespace-nowrap text-sm text-center">
                        ${!hasOilReports ? '❌' : '✅'}
                    </td>
                    <td class="px-4 py-3 whitespace-nowrap text-sm text-center">
                        ${!hasFatReports ? '❌' : '✅'}
                    </td>
                    <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                        ${truck.currentKmFormat || '0 km'}
                    </td>
                </tr>
            `;
        });

        tableBody.html(tableHtml);
    }

    getPriorityTrucks() {
        return [...this.trucks].sort((a, b) => {
            const oilPriorityA = this.getOilPriority(a);
            const oilPriorityB = this.getOilPriority(b);
            const fatPriorityA = this.getFatPriority(a);
            const fatPriorityB = this.getFatPriority(b);
            
            const maxPriorityA = Math.min(oilPriorityA, fatPriorityA);
            const maxPriorityB = Math.min(oilPriorityB, fatPriorityB);
            
            return maxPriorityA - maxPriorityB;
        });
    }

    getTrucksWithoutMaintenance() {
        return this.trucks.filter(truck => {
            const hasOilReports = truck.truckOilsReports && truck.truckOilsReports.length > 0;
            const hasFatReports = truck.truckFatsReports && truck.truckFatsReports.length > 0;
            return !hasOilReports || !hasFatReports;
        });
    }

    getOilPriority(truck) {
        const balance = truck.kmOilsBalance;
        if (balance === null || balance === undefined) return 4;
        if (balance < 0) return 1;
        if (balance >= 500) return 3;
        if (balance > 0 && balance < 500) return 2;
        return 4;
    }

    getFatPriority(truck) {
        const kmForFatsShoot = truck.kmForFatsShoot;
        const balance = truck.kmFatsBalance;
        
        if (balance === null || balance === undefined) return 4;
        
        if (kmForFatsShoot === 4000) {
            if (balance < 500) return 1;
            if (balance <= 500) return 2;
            return 3;
        }
        
        if (kmForFatsShoot === 3500 || kmForFatsShoot === 2500) {
            if (balance < 200) return 1;
            if (balance <= 200) return 2;
            return 3;
        }
        
        return 4;
    }

    getPriorityText(priority) {
        switch (priority) {
            case 1: return 'DANGER';
            case 2: return 'WARNING';
            case 3: return 'NORMAL';
            default: return 'UNKNOWN';
        }
    }

    getStatusClass(priority) {
        switch (priority) {
            case 1: return 'status-danger';
            case 2: return 'status-warning';
            case 3: return 'status-normal';
            default: return 'status-unknown';
        }
    }

    updateCharts(newStats, newTrucks) {
        this.stats = newStats;
        this.trucks = newTrucks;
        
        if (this.oilChart) {
            this.oilChart.data.datasets[0].data = [
                this.stats.oil.danger,
                this.stats.oil.warning,
                this.stats.oil.normal,
                this.stats.oil.unknown
            ];
            this.oilChart.update();
        }

        if (this.fatChart) {
            this.fatChart.data.datasets[0].data = [
                this.stats.fat.danger,
                this.stats.fat.warning,
                this.stats.fat.normal,
                this.stats.fat.unknown
            ];
            this.fatChart.update();
        }

        if (this.priorityChart || this.maintenanceChart) {
            this.renderPriorityChart();
            this.renderMaintenanceChart();
            this.renderPriorityTable();
            this.renderNoMaintenanceTable();
        }
    }
}