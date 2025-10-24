// Main Dashboard Controller
class DashboardMain {
    constructor() {
        this.trucks = [];
        this.statsModule = null;
        this.chartsModule = null;
        
        this.init();
    }

    init() {
        this.loadTruckData();
        
        // Set up auto-refresh every 5 minutes
        setInterval(() => {
            this.loadTruckData();
        }, 300000);
    }

    loadTruckData() {
        if (typeof window.trucksData !== 'undefined') {
            this.trucks = window.trucksData;
            this.updateDashboard();
        } else {
            this.fetchTruckDataFromBackend();
        }
    }

    fetchTruckDataFromBackend() {
        $.ajax({
            url: '/api/trucks/status',
            method: 'GET',
            success: (data) => {
                console.log('Fetched truck data from backend:', {data});
                this.trucks = data.data;
                this.updateDashboard();
            },
            error: (xhr, status, error) => {
                console.error('Failed to fetch truck data:', error);
            }
        });
    }

    updateDashboard() {
        // Initialize or update stats
        if (!this.statsModule) {
            this.statsModule = new DashboardStats(this.trucks);
        } else {
            this.statsModule.trucks = this.trucks;
            this.statsModule.stats = this.statsModule.calculateStats();
        }
        
        this.statsModule.renderStatsCards();

        // Initialize or update charts
        if (!this.chartsModule) {
            this.chartsModule = new DashboardCharts(this.statsModule.getStats(), this.trucks);
            this.chartsModule.initCharts();
        } else {
            this.chartsModule.updateCharts(this.statsModule.getStats(), this.trucks);
        }

        this.updateLastRefreshTime();
    }

    updateLastRefreshTime() {
        const now = new Date();
        const timeString = now.toLocaleTimeString();
        console.log(`Dashboard updated at: ${timeString}`);
    }
}

// Initialize dashboard when document is ready
$(document).ready(function() {
    window.dashboard = new DashboardMain();
});