package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import timdev.timdev.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    
}
