package timdev.timdev.dto;

import java.math.BigDecimal;

public class DestinationPortDTO {

    private Long id;
    private Long destinationId;
    private Long portId;
    private String portName;
    private BigDecimal amount;

    public DestinationPortDTO() {
    }

    public DestinationPortDTO(Long id, Long destinationId, Long portId, String portName, BigDecimal amount) {
        this.id = id;
        this.destinationId = destinationId;
        this.portId = portId;
        this.portName = portName;
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

    public Long getPortId() {
        return portId;
    }

    public void setPortId(Long portId) {
        this.portId = portId;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // Getters & Setters
}