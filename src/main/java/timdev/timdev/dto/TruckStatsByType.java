package timdev.timdev.dto;

public class TruckStatsByType {
    private long total;
    private long checkedCount;
    private long unCheckedCount;
    private long issueItemCount;

    public TruckStatsByType(long total, long checkedCount, long unCheckedCount, long issueItemCount) {
        this.total = total;
        this.checkedCount = checkedCount;
        this.unCheckedCount = unCheckedCount;
        this.issueItemCount = issueItemCount;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCheckedCount() {
        return checkedCount;
    }

    public void setCheckedCount(long checkedCount) {
        this.checkedCount = checkedCount;
    }

    public long getUnCheckedCount() {
        return unCheckedCount;
    }

    public void setUnCheckedCount(long unCheckedCount) {
        this.unCheckedCount = unCheckedCount;
    }

    public long getIssueItemCount() {
        return issueItemCount;
    }

    public void setIssueItemCount(long issueItemCount) {
        this.issueItemCount = issueItemCount;
    }

   
}
