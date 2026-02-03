package timdev.timdev.entity;


import java.io.Serializable;

import org.hibernate.envers.Audited;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import timdev.timdev.listener.AuditListener;

@Entity
@Table(
    name = "averages",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_truck_measurement",
        columnNames = {"truck_id", "measurement_id"}
    )
)
@Audited
@EntityListeners(AuditListener.class)
public class Average extends AuditableUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The measurement value
    @Column(nullable = false)
    private double value;

    // link to Measurement
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "measurement_id", nullable = false)
    private Measurement measurement;

    // Link to Truck
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    public Average() {}

    public Average(double value, Measurement measurement) {
        this.value = value;
        this.measurement = measurement;
    }

    public Long getId() {
        return id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Measurement measurement) {
        this.measurement = measurement;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

}