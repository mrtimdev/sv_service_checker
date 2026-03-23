package timdev.timdev.entity;

import java.math.BigDecimal;

import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
@Table(name = "destination_scale_station")
public class DestinationScaleStation extends AuditableUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "destination_id")
    private DestinationSetting destination;

    @ManyToOne
    @JoinColumn(name = "scale_station_id")
    private ScaleStation scaleStation;

    @Column(precision = 10, scale = 2)
    private BigDecimal minWeight;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxWeight;

    private BigDecimal amount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ScaleStation getScaleStation() {
        return scaleStation;
    }

    public void setScaleStation(ScaleStation scaleStation) {
        this.scaleStation = scaleStation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public DestinationSetting getDestination() {
        return destination;
    }

    public void setDestination(DestinationSetting destination) {
        this.destination = destination;
    }

    public BigDecimal getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(BigDecimal minWeight) {
        this.minWeight = minWeight;
    }

    public BigDecimal getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(BigDecimal maxWeight) {
        this.maxWeight = maxWeight;
    }

    // getters setters
}