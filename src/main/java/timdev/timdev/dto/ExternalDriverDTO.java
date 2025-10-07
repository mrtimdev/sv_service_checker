package timdev.timdev.dto;

public class ExternalDriverDTO {
    

    private Long id;
    private String firstName;
    private String lastName;
    private String licenseNumber;
    private String name;
    private String phone;
    private Double rating;
    private String status;
    private AssignedVehicleDTO assignedVehicle;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getLicenseNumber() {
        return licenseNumber;
    }
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullName()
    {
        return this.firstName + ' ' + this.lastName;
    }
    public Double getRating() {
        return rating;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public AssignedVehicleDTO getAssignedVehicle() {
        return assignedVehicle;
    }
    public void setAssignedVehicle(AssignedVehicleDTO assignedVehicle) {
        this.assignedVehicle = assignedVehicle;
    }
}
