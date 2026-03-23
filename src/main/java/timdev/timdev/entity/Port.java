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
public class Port {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(name = "is_highway")
    private Boolean isHighway = false;

    @OneToMany(mappedBy = "port")
    private List<DestinationPort> destinationPorts;

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

    public Boolean getIsHighway() {
        return isHighway;
    }

    public void setIsHighway(Boolean isHighway) {
        this.isHighway = isHighway;
    }

    public List<DestinationPort> getDestinationPorts() {
        return destinationPorts;
    }

    public void setDestinationPorts(List<DestinationPort> destinationPorts) {
        this.destinationPorts = destinationPorts;
    }
}
