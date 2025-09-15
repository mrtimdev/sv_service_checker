package timdev.timdev.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import timdev.timdev.enums.ApprovalLevel;

@Entity
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ApprovalLevel approvedLevel;

    @Column(name = "km_for_fats_shoot", nullable = false)
    private Double kmForFatsShoot = 0.0;
    @Column(name = "km_for_oils_change", nullable = false)
    private Double kmForOilsChange = 0.0;

    @Column(name = "fats_km", nullable = false)
    private Double kmFats = 0.0;
    @Column(name = "oils_km", nullable = false)
    private Double kmOils = 0.0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApprovalLevel getApprovedLevel() {
        return approvedLevel;
    }

    public void setApprovedLevel(ApprovalLevel approvedLevel) {
        this.approvedLevel = approvedLevel;
    }

    public Double getKmFats() {
        return kmFats;
    }

    public void setKmFats(Double kmFats) {
        this.kmFats = kmFats;
    }

    public Double getKmOils() {
        return kmOils;
    }

    public void setKmOils(Double kmOils) {
        this.kmOils = kmOils;
    }

    public Double getKmForFatsShoot() {
        return kmForFatsShoot;
    }

    public void setKmForFatsShoot(Double kmForFatsShoot) {
        this.kmForFatsShoot = kmForFatsShoot;
    }

    public Double getKmForOilsChange() {
        return kmForOilsChange;
    }

    public void setKmForOilsChange(Double kmForOilsChange) {
        this.kmForOilsChange = kmForOilsChange;
    }

}
