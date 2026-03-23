package timdev.timdev.entity;

import java.util.List;

import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
public class ScaleStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(name = "is_highway")
    private Boolean isHighway = false;

    @OneToMany(mappedBy = "scaleStation")
    private List<DestinationScaleStation> destinationScaleStations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DestinationScaleStation> getDestinationScaleStations() {
        return destinationScaleStations;
    }

    public void setDestinationScaleStations(List<DestinationScaleStation> destinationScaleStations) {
        this.destinationScaleStations = destinationScaleStations;
    }

    public Boolean getIsHighway() {
        return isHighway;
    }

    public void setIsHighway(Boolean isHighway) {
        this.isHighway = isHighway;
    }
}
