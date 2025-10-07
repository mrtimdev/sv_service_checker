package timdev.timdev.dto;

public class TruckTypeStats {
    
    private long totalBigTruck;
    private long totalSmallTruck;
    private long totalBigTruckCheckedCount;
    private long totalSmallTruckCheckedCount;
    private long totalBigTruckIssueCount;
    private long totalSmallTruckIssueCount;

    public TruckTypeStats(long totalBigTruck, long totalSmallTruck,
                          long totalBigTruckCheckedCount, long totalSmallTruckCheckedCount,
                          long totalBigTruckIssueCount, long totalSmallTruckIssueCount) {
        this.totalBigTruck = totalBigTruck;
        this.totalSmallTruck = totalSmallTruck;
        this.totalBigTruckCheckedCount = totalBigTruckCheckedCount;
        this.totalSmallTruckCheckedCount = totalSmallTruckCheckedCount;
        this.totalBigTruckIssueCount = totalBigTruckIssueCount;
        this.totalSmallTruckIssueCount = totalSmallTruckIssueCount;
    }
    
    public long getTotalBigTruck() {
        return totalBigTruck;
    }
    public void setTotalBigTruck(long totalBigTruck) {
        this.totalBigTruck = totalBigTruck;
    }
    public long getTotalSmallTruck() {
        return totalSmallTruck;
    }
    public void setTotalSmallTruck(long totalSmallTruck) {
        this.totalSmallTruck = totalSmallTruck;
    }
    public long getTotalBigTruckCheckedCount() {
        return totalBigTruckCheckedCount;
    }
    public void setTotalBigTruckCheckedCount(long totalBigTruckCheckedCount) {
        this.totalBigTruckCheckedCount = totalBigTruckCheckedCount;
    }
    public long getTotalSmallTruckCheckedCount() {
        return totalSmallTruckCheckedCount;
    }
    public void setTotalSmallTruckCheckedCount(long totalSmallTruckCheckedCount) {
        this.totalSmallTruckCheckedCount = totalSmallTruckCheckedCount;
    }
    public long getTotalBigTruckIssueCount() {
        return totalBigTruckIssueCount;
    }
    public void setTotalBigTruckIssueCount(long totalBigTruckIssueCount) {
        this.totalBigTruckIssueCount = totalBigTruckIssueCount;
    }
    public long getTotalSmallTruckIssueCount() {
        return totalSmallTruckIssueCount;
    }
    public void setTotalSmallTruckIssueCount(long totalSmallTruckIssueCount) {
        this.totalSmallTruckIssueCount = totalSmallTruckIssueCount;
    }
}
