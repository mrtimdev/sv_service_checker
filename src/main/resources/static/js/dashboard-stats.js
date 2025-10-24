// Dashboard Statistics Module
class DashboardStats {
    constructor(trucks) {
        this.trucks = trucks;
        this.stats = this.calculateStats();
    }

    calculateStats() {
        const stats = {
            // Overall counts
            totalTrucks: this.trucks.length,
            noOilReports: 0,
            noFatReports: 0,
            noReportsAtAll: 0,
            
            // Oil change stats
            oil: {
                danger: 0,
                warning: 0,
                normal: 0,
                unknown: 0
            },
            
            // Fats shoot stats
            fat: {
                danger: 0,
                warning: 0,
                normal: 0,
                unknown: 0
            }
        };

        this.trucks.forEach(truck => {
            // Check for missing reports
            const hasOilReports = truck.truckOilsReports && truck.truckOilsReports.length > 0;
            const hasFatReports = truck.truckFatsReports && truck.truckFatsReports.length > 0;
            
            if (!hasOilReports) stats.noOilReports++;
            if (!hasFatReports) stats.noFatReports++;
            if (!hasOilReports && !hasFatReports) stats.noReportsAtAll++;

            // Calculate oil priority
            const oilPriority = this.getOilPriority(truck);
            switch (oilPriority) {
                case 1:
                    stats.oil.danger++;
                    break;
                case 2:
                    stats.oil.warning++;
                    break;
                case 3:
                    stats.oil.normal++;
                    break;
                case 4:
                    stats.oil.unknown++;
                    break;
            }

            // Calculate fat priority
            const fatPriority = this.getFatPriority(truck);
            switch (fatPriority) {
                case 1:
                    stats.fat.danger++;
                    break;
                case 2:
                    stats.fat.warning++;
                    break;
                case 3:
                    stats.fat.normal++;
                    break;
                case 4:
                    stats.fat.unknown++;
                    break;
            }
        });

        return stats;
    }

    getOilPriority(truck) {
        const balance = truck.kmOilsBalance;
        if (balance === null || balance === undefined) {
            return 4; // unknown
        }
        if (balance < 0) {
            return 1; // danger
        }
        if (balance >= 500) {
            return 3; // normal/safe
        }
        if (balance > 0 && balance < 500) {
            return 2; // warning
        }
        return 4; // unknown
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

    renderStatsCards() {
        // Update overall stats
        $('#totalTrucks').text(this.stats.totalTrucks);
        $('#noOilReports').text(this.stats.noOilReports);
        $('#noFatReports').text(this.stats.noFatReports);
        $('#noReports').text(this.stats.noReportsAtAll);

        // Update oil stats
        $('#oilDanger').text(this.stats.oil.danger);
        $('#oilWarning').text(this.stats.oil.warning);
        $('#oilNormal').text(this.stats.oil.normal);

        // Update fat stats
        $('#fatDanger').text(this.stats.fat.danger);
        $('#fatWarning').text(this.stats.fat.warning);
        $('#fatNormal').text(this.stats.fat.normal);

        // Add pulse animation to danger cards if there are danger items
        if (this.stats.oil.danger > 0) {
            $('.stat-card.danger:has(#oilDanger)').addClass('pulse-animation');
        }
        if (this.stats.fat.danger > 0) {
            $('.stat-card.danger:has(#fatDanger)').addClass('pulse-animation');
        }
    }

    getTrucksWithoutReports() {
        return this.trucks.filter(truck => {
            const hasOilReports = truck.truckOilsReports && truck.truckOilsReports.length > 0;
            const hasFatReports = truck.truckFatsReports && truck.truckFatsReports.length > 0;
            return !hasOilReports || !hasFatReports;
        });
    }

    getStats() {
        return this.stats;
    }

    getTrucks() {
        return this.trucks;
    }
}