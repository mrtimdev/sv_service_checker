package timdev.timdev.dto;

import java.util.List;

public class DestinationFullDTO {

    private DestinationSettingDTO destination;
    private List<DestinationScaleStationDTO> scales;
    private List<DestinationPortDTO> ports;

    public DestinationFullDTO() {
    }

    public DestinationFullDTO(DestinationSettingDTO destination,
            List<DestinationScaleStationDTO> scales,
            List<DestinationPortDTO> ports) {
        this.destination = destination;
        this.scales = scales;
        this.ports = ports;
    }

    public DestinationSettingDTO getDestination() {
        return destination;
    }

    public void setDestination(DestinationSettingDTO destination) {
        this.destination = destination;
    }

    public List<DestinationScaleStationDTO> getScales() {
        return scales;
    }

    public void setScales(List<DestinationScaleStationDTO> scales) {
        this.scales = scales;
    }

    public List<DestinationPortDTO> getPorts() {
        return ports;
    }

    public void setPorts(List<DestinationPortDTO> ports) {
        this.ports = ports;
    }

    // Getters & Setters
}