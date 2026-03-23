package timdev.timdev.dto;

import java.math.BigDecimal;

public class DestinationScaleStationDTO {

    private Long id;
    private Long destinationId;
    private Long scaleStationId;
    private String scaleStationName;
    private BigDecimal amount;

    public DestinationScaleStationDTO() {
    }

    public DestinationScaleStationDTO(Long id, Long destinationId, Long scaleStationId, String scaleStationName,
            BigDecimal amount) {
        this.id = id;
        this.destinationId = destinationId;
        this.scaleStationId = scaleStationId;
        this.scaleStationName = scaleStationName;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(Long destinationId) {
        this.destinationId = destinationId;
    }

    public Long getScaleStationId() {
        return scaleStationId;
    }

    public void setScaleStationId(Long scaleStationId) {
        this.scaleStationId = scaleStationId;
    }

    public String getScaleStationName() {
        return scaleStationName;
    }

    public void setScaleStationName(String scaleStationName) {
        this.scaleStationName = scaleStationName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // Getters & Setters
}