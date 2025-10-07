package timdev.timdev.dto;

public class AssignedVehicleDTO {
    

    private Long id;
    private String licensePlate;
    private String model;
    private String manufacturer;
    private String type;
    private String status;
    private Double mileage;
    private Double fuelConsumption;
    private Long lastInspectionDate;
    private Long lastServiceDate;
    private Long nextServiceDue;
    private String truckSize;
    private Integer qtyPalletsCapacity;
    private String assignedZone;
    private String unavailableRoutes;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getLicensePlate() {
        return licensePlate;
    }
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getManufacturer() {
        return manufacturer;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Double getMileage() {
        return mileage;
    }
    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }
    public Double getFuelConsumption() {
        return fuelConsumption;
    }
    public void setFuelConsumption(Double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }
    public Long getLastInspectionDate() {
        return lastInspectionDate;
    }
    public void setLastInspectionDate(Long lastInspectionDate) {
        this.lastInspectionDate = lastInspectionDate;
    }
    public Long getLastServiceDate() {
        return lastServiceDate;
    }
    public void setLastServiceDate(Long lastServiceDate) {
        this.lastServiceDate = lastServiceDate;
    }
    public Long getNextServiceDue() {
        return nextServiceDue;
    }
    public void setNextServiceDue(Long nextServiceDue) {
        this.nextServiceDue = nextServiceDue;
    }
    public String getTruckSize() {
        return truckSize;
    }
    public void setTruckSize(String truckSize) {
        this.truckSize = truckSize;
    }
    public Integer getQtyPalletsCapacity() {
        return qtyPalletsCapacity;
    }
    public void setQtyPalletsCapacity(Integer qtyPalletsCapacity) {
        this.qtyPalletsCapacity = qtyPalletsCapacity;
    }
    public String getAssignedZone() {
        return assignedZone;
    }
    public void setAssignedZone(String assignedZone) {
        this.assignedZone = assignedZone;
    }
    public String getUnavailableRoutes() {
        return unavailableRoutes;
    }
    public void setUnavailableRoutes(String unavailableRoutes) {
        this.unavailableRoutes = unavailableRoutes;
    }
}
