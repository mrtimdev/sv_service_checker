package timdev.timdev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import timdev.timdev.entity.Response;

public interface ResponseRepository extends JpaRepository<Response, Long> {
    List<Response> findBySurveyId(Long surveyId);
}
