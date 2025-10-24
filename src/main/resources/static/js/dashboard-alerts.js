// Dashboard Alerts Module
class DashboardAlerts {
    constructor(trucks) {
        this.trucks = trucks;
    }

    getPriorityAlerts() {
        // Sort trucks by combined priority (highest priority first)
        const sortedTrucks = [...this.trucks].sort((a, b) => {
            const oilPriorityA = this.getOilPriority(a);
            const oilPriorityB = this.getOilPriority(b);
            const fatPriorityA = this.getFatPriority(a);
            const fatPriorityB = this.getFatPriority(b);
            
            // Get highest priority for each truck
            const maxPriorityA = Math.min(oilPriorityA, fatPriorityA);
            const maxPriorityB = Math.min(oilPriorityB, fatPriorityB);
            
            return maxPriorityA - maxPriorityB;
        });

        // Take top 10 for alerts
        return sortedTrucks.slice(0, 10);
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
        // higher priority first (smaller number = higher priority)
        const kmForFatsShoot = truck.kmForFatsShoot;
        const balance = truck.kmFatsBalance;
        
        if (balance === null || balance === undefined) {
            return 4; // unknown
        }
        
        if (kmForFatsShoot === 4000) {
            if (balance < 500) return 1; // red - danger
            if (balance <= 500) return 2; // yellow - warning
            return 3; // normal
        }
        
        if (kmForFatsShoot === 3500 || kmForFatsShoot === 2500) {
            if (balance < 200) return 1; // red - danger
            if (balance <= 200) return 2; // yellow - warning
            return 3; // normal
        }
        
        return 4; // default - unknown
    }

    getPriorityClass(priority) {
        switch (priority) {
            case 1: return 'alert-danger';
            case 2: return 'alert-warning';
            case 3: return 'alert-normal';
            default: return 'alert-normal';
        }
    }

    getPriorityText(priority) {
        switch (priority) {
            case 1: return 'DANGER';
            case 2: return 'WARNING';
            case 3: return 'NORMAL';
            default: return 'UNKNOWN';
        }
    }

    getPriorityIcon(priority) {
        switch (priority) {
            case 1:
                return `<svg class="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"/>
                </svg>`;
            case 2:
                return `<svg class="w-5 h-5 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M12 2a10 10 0 100 20 10 10 0 000-20z"/>
                </svg>`;
            case 3:
                return `<svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>`;
            default:
                return `<svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M12 2a10 10 0 100 20 10 10 0 000-20z"/>
                </svg>`;
        }
    }

    getFatThresholdInfo(truck) {
        const kmForFatsShoot = truck.kmForFatsShoot;
        if (kmForFatsShoot === 4000) {
            return { danger: '< 500 km', warning: '= 500 km', normal: '> 500 km' };
        } else if (kmForFatsShoot === 3500 || kmForFatsShoot === 2500) {
            return { danger: '< 200 km', warning: '= 200 km', normal: '> 200 km' };
        }
        return { danger: 'N/A', warning: 'N/A', normal: 'N/A' };
    }

    renderRecentAlerts() {
        const priorityAlerts = this.getPriorityAlerts();
        const alertsContainer = $('#recentAlerts');

        if (priorityAlerts.length === 0) {
            alertsContainer.html(`
                <div class="text-center py-8 text-gray-500">
                    <svg class="w-12 h-12 mx-auto mb-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                    <p>No alerts found</p>
                </div>
            `);
            return;
        }

        let alertsHtml = '<div class="space-y-3">';
        
        priorityAlerts.forEach(truck => {
            const oilPriority = this.getOilPriority(truck);
            const fatPriority = this.getFatPriority(truck);
            const highestPriority = Math.min(oilPriority, fatPriority);
            const priorityClass = this.getPriorityClass(highestPriority);
            const priorityText = this.getPriorityText(highestPriority);
            const priorityIcon = this.getPriorityIcon(highestPriority);

            const hasOilReports = truck.truckOilsReports && truck.truckOilsReports.length > 0;
            const hasFatReports = truck.truckFatsReports && truck.truckFatsReports.length > 0;
            const fatThresholdInfo = this.getFatThresholdInfo(truck);

            alertsHtml += `
                <div class="alert-item ${priorityClass} rounded-lg p-4">
                    <div class="flex items-center justify-between mb-2">
                        <div class="flex items-center space-x-3">
                            ${priorityIcon}
                            <h4 class="font-semibold text-gray-800">${truck.licensePlate || 'Unknown Plate'}</h4>
                        </div>
                        <span class="badge badge-${highestPriority === 1 ? 'danger' : highestPriority === 2 ? 'warning' : highestPriority === 3 ? 'normal' : 'unknown'}">
                            ${priorityText}
                        </span>
                    </div>
                    <div class="grid grid-cols-2 gap-4 text-sm">
                        <div>
                            <span class="font-medium">Oil Change:</span>
                            <span class="ml-2 ${oilPriority === 1 ? 'text-red-600' : oilPriority === 2 ? 'text-yellow-600' : 'text-green-600'}">
                                ${truck.kmOilsBalance !== null ? truck.kmOilsBalance + ' km' : 'N/A'}
                                ${!hasOilReports ? ' (No reports)' : ''}
                            </span>
                        </div>
                        <div>
                            <span class="font-medium">Fats Shoot:</span>
                            <span class="ml-2 ${fatPriority === 1 ? 'text-red-600' : fatPriority === 2 ? 'text-yellow-600' : 'text-green-600'}">
                                ${truck.kmFatsBalance !== null ? truck.kmFatsBalance + ' km' : 'N/A'}
                                ${!hasFatReports ? ' (No reports)' : ''}
                            </span>
                        </div>
                    </div>
                    <div class="mt-2 text-xs text-gray-500 grid grid-cols-2 gap-2">
                        <div>Current KM: ${truck.currentKmFormat || '0 km'}</div>
                        <div>Fat Threshold: ${truck.kmForFatsShoot || 'N/A'} km</div>
                    </div>
                    ${fatPriority !== 4 ? `
                    <div class="mt-1 text-xs text-gray-400">
                        Fat thresholds: Danger ${fatThresholdInfo.danger}, Warning ${fatThresholdInfo.warning}
                    </div>
                    ` : ''}
                </div>
            `;
        });

        alertsHtml += '</div>';
        alertsContainer.html(alertsHtml);
    }

    renderNoReportsSection(trucksWithoutReports) {
        const noReportsSection = $('#noReportsSection');
        
        if (trucksWithoutReports.length === 0) {
            noReportsSection.html(`
                <div class="text-center py-8 text-gray-500">
                    <svg class="w-12 h-12 mx-auto mb-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                    <p>All trucks have reports</p>
                </div>
            `);
            return;
        }

        let noReportsHtml = '<div class="space-y-3">';
        
        trucksWithoutReports.forEach(truck => {
            const hasOilReports = truck.truckOilsReports && truck.truckOilsReports.length > 0;
            const hasFatReports = truck.truckFatsReports && truck.truckFatsReports.length > 0;

            noReportsHtml += `
                <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center space-x-3">
                            <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M12 2a10 10 0 100 20 10 10 0 000-20z"/>
                            </svg>
                            <h4 class="font-semibold text-gray-800">${truck.licensePlate || 'Unknown Plate'}</h4>
                        </div>
                        <div class="flex space-x-2">
                            ${!hasOilReports ? '<span class="badge badge-warning">No Oil Reports</span>' : ''}
                            ${!hasFatReports ? '<span class="badge badge-warning">No Fat Reports</span>' : ''}
                        </div>
                    </div>
                    <div class="mt-2 text-sm text-gray-600">
                        Current KM: ${truck.currentKmFormat || '0 km'} | Fat Threshold: ${truck.kmForFatsShoot || 'N/A'} km
                    </div>
                </div>
            `;
        });

        noReportsHtml += '</div>';
        noReportsSection.html(noReportsHtml);
    }
}