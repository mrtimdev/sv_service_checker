package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import timdev.timdev.entity.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {
   
}