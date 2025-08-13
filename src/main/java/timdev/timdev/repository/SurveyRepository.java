package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import timdev.timdev.entity.Survey;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    // Additional query methods if needed
}