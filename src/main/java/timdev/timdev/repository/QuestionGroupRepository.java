package timdev.timdev.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.QuestionGroup;

import java.util.UUID;

@Repository
public interface QuestionGroupRepository extends JpaRepository<QuestionGroup, UUID> {
    void deleteBySurveyId(UUID surveyId);

    @Query("SELECT g FROM QuestionGroup g WHERE g.id = :id")
    Optional<QuestionGroup> findByCustomId(@Param("id") String id);

    List<QuestionGroup> findBySurveyIdOrderByOrderIndex(UUID surveyId);
}