package timdev.timdev.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.Audited;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import timdev.timdev.listener.AuditListener;

@Entity
@Audited
@EntityListeners(AuditListener.class)
@Table(name = "destination_settings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DestinationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "The Destination Code is required")
    @Column(name = "destination_code", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull(message = "The Destination Name is required")
    @Column(name = "destination_name", nullable = false, unique = true, columnDefinition="TEXT")
    private String name;

    @Column(name = "distance", nullable = false)
    private double distance;

    @OneToMany(mappedBy = "setting", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Destination> destinations = new ArrayList<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Transient
    public String getDistanceFormat() {
        return String.format("%,.2f km", distance); 
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }
    
}