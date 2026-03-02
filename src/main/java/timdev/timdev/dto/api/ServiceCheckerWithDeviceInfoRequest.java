package timdev.timdev.dto.api;

import java.time.LocalDate;
import java.util.List;

import io.micrometer.common.lang.Nullable;

public class ServiceCheckerWithDeviceInfoRequest {
    

    private LocalDate date;
    @Nullable
    private String licensePlate;
    @Nullable
    private String licensePlateEstimated;
    private String imagePath;
    private DeviceInfoDTO deviceInfo;
    private List<CategoryItemRequest> categories;
    
    public ServiceCheckerWithDeviceInfoRequest() {}
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public DeviceInfoDTO getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(DeviceInfoDTO deviceInfo) { this.deviceInfo = deviceInfo; }
    
    public List<CategoryItemRequest> getCategories() { return categories; }
    public void setCategories(List<CategoryItemRequest> categories) { this.categories = categories; }

    public String getLicensePlateEstimated() {
        return licensePlateEstimated;
    }

    public void setLicensePlateEstimated(String licensePlateEstimated) {
        this.licensePlateEstimated = licensePlateEstimated;
    }
}
