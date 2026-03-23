package timdev.timdev.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
@Table(name = "truck_report")
public class TruckReport extends AuditableUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date")
    private LocalDate reportDate;

    // Relationship with Truck
    @ManyToOne
    @JoinColumn(name = "destination_id")
    private DestinationSetting destination;

    @Column(name = "total_destination", columnDefinition = "TEXT")
    private String totalDestination;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "truck_id")
    private Truck truck;

    // Relationship with Scale Station
    @ManyToOne
    @JoinColumn(name = "scale_station_id")
    private ScaleStation scaleStation;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "loading_location")
    private String loadingLocation;

    @Column(name = "drop_location")
    private String dropLocation;

    private String route;

    @Column(name = "cargo_weight")
    private BigDecimal cargoWeight; // ទម្ងន់ទំនិញ

    @Column(name = "truck_weight")
    private BigDecimal truckWeight; // ទម្ងន់ឡាន

    @Column(name = "total_weight")
    private BigDecimal totalWeight; // ទម្ងន់សរុប

    @Column(name = "scale_fee")
    private BigDecimal scaleFee; // បង់ជញ្ជីង (Total from all selected scales)

    @Column(name = "other_fee")
    private BigDecimal otherFee;

    @Column(name = "yard_fee")
    private BigDecimal yardFee; // បង់បេន

    @Column(name = "port_fee")
    private BigDecimal portFee; // បង់ផែ (Total from all selected ports)

    @Column(name = "traffic_police_fee")
    private BigDecimal trafficPoliceFee; // ប៉ូលីសចរាចរណ៏

    @Column(name = "other_expense")
    private BigDecimal otherExpense; // ផ្សេងៗ

    @Column(name = "donation")
    private BigDecimal donation; // ចំណោយ

    @Column(name = "highway_fee")
    private BigDecimal highwayFee; // ផ្លូវជាតិ

    @Column(name = "on_highway")
    private String onHighway; // ផ្លូវជាតិ

    @Column(name = "note")
    private String note; // សម្គាល់

    @Column(name = "other_remark")
    private String otherRemark;

    // Relationships to child tables
    @OneToMany(mappedBy = "truckReport", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TruckReportScale> selectedScales = new ArrayList<>();

    @OneToMany(mappedBy = "truckReport", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TruckReportPort> selectedPorts = new ArrayList<>();

    // Helper methods to manage relationships
    public void addScale(TruckReportScale scale) {
        selectedScales.add(scale);
        scale.setTruckReport(this);
    }

    public void removeScale(TruckReportScale scale) {
        selectedScales.remove(scale);
        scale.setTruckReport(null);
    }

    public void addPort(TruckReportPort port) {
        selectedPorts.add(port);
        port.setTruckReport(this);
    }

    public void removePort(TruckReportPort port) {
        selectedPorts.remove(port);
        port.setTruckReport(null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    public ScaleStation getScaleStation() {
        return scaleStation;
    }

    public void setScaleStation(ScaleStation scaleStation) {
        this.scaleStation = scaleStation;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getLoadingLocation() {
        return loadingLocation;
    }

    public void setLoadingLocation(String loadingLocation) {
        this.loadingLocation = loadingLocation;
    }

    public String getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public BigDecimal getCargoWeight() {
        return cargoWeight;
    }

    public void setCargoWeight(BigDecimal cargoWeight) {
        this.cargoWeight = cargoWeight;
    }

    public BigDecimal getTruckWeight() {
        return truckWeight;
    }

    public void setTruckWeight(BigDecimal truckWeight) {
        this.truckWeight = truckWeight;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    public BigDecimal getScaleFee() {
        return scaleFee;
    }

    public void setScaleFee(BigDecimal scaleFee) {
        this.scaleFee = scaleFee;
    }

    public BigDecimal getOtherFee() {
        return otherFee;
    }

    public void setOtherFee(BigDecimal otherFee) {
        this.otherFee = otherFee;
    }

    public BigDecimal getYardFee() {
        return yardFee;
    }

    public void setYardFee(BigDecimal yardFee) {
        this.yardFee = yardFee;
    }

    public BigDecimal getPortFee() {
        return portFee;
    }

    public void setPortFee(BigDecimal portFee) {
        this.portFee = portFee;
    }

    public BigDecimal getTrafficPoliceFee() {
        return trafficPoliceFee;
    }

    public void setTrafficPoliceFee(BigDecimal trafficPoliceFee) {
        this.trafficPoliceFee = trafficPoliceFee;
    }

    public BigDecimal getOtherExpense() {
        return otherExpense;
    }

    public void setOtherExpense(BigDecimal otherExpense) {
        this.otherExpense = otherExpense;
    }

    public BigDecimal getDonation() {
        return donation;
    }

    public void setDonation(BigDecimal donation) {
        this.donation = donation;
    }

    public BigDecimal getHighwayFee() {
        return highwayFee;
    }

    public void setHighwayFee(BigDecimal highwayFee) {
        this.highwayFee = highwayFee;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getOtherRemark() {
        return otherRemark;
    }

    public void setOtherRemark(String otherRemark) {
        this.otherRemark = otherRemark;
    }

    public List<TruckReportScale> getSelectedScales() {
        return selectedScales;
    }

    public void setSelectedScales(List<TruckReportScale> selectedScales) {
        this.selectedScales = selectedScales;
    }

    public List<TruckReportPort> getSelectedPorts() {
        return selectedPorts;
    }

    public void setSelectedPorts(List<TruckReportPort> selectedPorts) {
        this.selectedPorts = selectedPorts;
    }

    public DestinationSetting getDestination() {
        return destination;
    }

    public void setDestination(DestinationSetting destination) {
        this.destination = destination;
    }

    public String getTotalDestination() {
        return totalDestination;
    }

    public void setTotalDestination(String totalDestination) {
        this.totalDestination = totalDestination;
    }

    public String getOnHighway() {
        return onHighway;
    }

    public void setOnHighway(String onHighway) {
        this.onHighway = onHighway;
    }
}