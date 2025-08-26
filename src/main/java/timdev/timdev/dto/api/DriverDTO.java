package timdev.timdev.dto.api;

import timdev.timdev.entity.Driver;

public class DriverDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    
    // Constructors
    public DriverDTO() {}
    
    public DriverDTO(Driver driver) {
        this.id = driver.getId();
        this.firstName = driver.getFirstName();
        this.lastName = driver.getLastName();
        this.fullName = driver.getFullName();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
