package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Model;



@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    
    boolean existsByCode(String code);
}
