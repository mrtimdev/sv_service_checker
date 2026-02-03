package timdev.timdev.dto;

public class DestinationAjaxDTO {
    
    Long id;
    String date;
    String truck;
    Long truckId;
    String code;
    String destination;
    double distance;

    public DestinationAjaxDTO(Long id, String date, String truck, Long truckId, String code, String destination, double distance) {
        this.id = id;
        this.date = date;
        this.truck = truck;
        this.truckId = truckId;
        this.code = code;
        this.destination = destination;
        this.distance = distance;
    }



    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTruck() {
        return truck;
    }
    public void setTruck(String truck) {
        this.truck = truck;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }



    public Long getTruckId() {
        return truckId;
    }



    public void setTruckId(Long truckId) {
        this.truckId = truckId;
    }

    
}
