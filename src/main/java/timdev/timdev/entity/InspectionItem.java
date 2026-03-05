package timdev.timdev.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "inspection_items")
public class InspectionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // "Sufficient straps"

    @Column(nullable = false)
    private String khmerName; // "មានខ្សែរឹតទំនិញគ្រប់គ្រាន់អត់"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference
    private InspectionCategory category;

    @Column(nullable = false)
    private Boolean isRequired = true;

    @OneToMany(mappedBy = "inspectionItem")
    @JsonIgnore
    private List<ServiceCheckerItemNote> itemNotes = new ArrayList<>();

    public List<ServiceCheckerItemNote> getItemNotes() {
        return itemNotes;
    }

    public void setItemNotes(List<ServiceCheckerItemNote> itemNotes) {
        this.itemNotes = itemNotes;
    }

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

    public String getKhmerName() {
        return khmerName;
    }

    public void setKhmerName(String khmerName) {
        this.khmerName = khmerName;
    }

    public InspectionCategory getCategory() {
        return category;
    }

    public void setCategory(InspectionCategory category) {
        this.category = category;
    }

    public boolean isPassed() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isPassed'");
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    // Getters and setters
}