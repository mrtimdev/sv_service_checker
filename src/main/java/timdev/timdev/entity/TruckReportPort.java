package timdev.timdev.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
@Table(name = "truck_report_ports")
public class TruckReportPort extends AuditableUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "truck_report_id", nullable = false)
    private TruckReport truckReport;

    @ManyToOne
    @JoinColumn(name = "port_id", nullable = false)
    private Port port;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "default_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultAmount;

    // Constructors
    public TruckReportPort() {
    }

    public TruckReportPort(TruckReport truckReport, Port port, BigDecimal amount) {
        this.truckReport = truckReport;
        this.port = port;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TruckReport getTruckReport() {
        return truckReport;
    }

    public void setTruckReport(TruckReport truckReport) {
        this.truckReport = truckReport;
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getDefaultAmount() {
        return defaultAmount;
    }

    public void setDefaultAmount(BigDecimal defaultAmount) {
        this.defaultAmount = defaultAmount;
    }
}