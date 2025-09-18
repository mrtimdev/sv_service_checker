package timdev.timdev.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import timdev.timdev.enums.FatsOilsType;

@Entity
@Table(name = "fats_oils_settings")
public class FatsOilsSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FatsOilsType type;

    @Column(nullable = false)
    private Integer km; // main KM value

    @Column(name = "min_km")
    private Integer minKm;

    @Column(name = "max_km")
    private Integer maxKm;

    // Constructors
    public FatsOilsSetting() {}

    public FatsOilsSetting(FatsOilsType type, Integer km, Integer minKm, Integer maxKm) {
        this.type = type;
        this.km = km;
        this.minKm = minKm;
        this.maxKm = maxKm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Integer getKm() {
        return km;
    }

    public void setKm(Integer km) {
        this.km = km;
    }

    public Integer getMinKm() {
        return minKm;
    }

    public void setMinKm(Integer minKm) {
        this.minKm = minKm;
    }

    public Integer getMaxKm() {
        return maxKm;
    }

    public void setMaxKm(Integer maxKm) {
        this.maxKm = maxKm;
    }

    public FatsOilsType getType() {
        return type;
    }

    public void setType(FatsOilsType type) {
        this.type = type;
    }


    @Transient
    public Integer getDiffKm() {
        if (minKm != null && maxKm != null) {
            return maxKm - minKm;
        }
        return 0;
    }
}