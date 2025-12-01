package timdev.timdev.entity;

import org.hibernate.envers.Audited;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Audited
@Entity
@Table(name = "permissions")
@Getter @Setter
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;         
    private String displayName;  
}
