package timdev.timdev.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(name = "measurements", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Measurement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Keep names as string constants or use an enum; here we use string for flexibility
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "native_name", nullable = false, length = 100)
    private String nativeName;

    public Measurement() {}

    public Measurement(String name, String nativeName) {
        this.name = name;
        this.nativeName = nativeName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

}