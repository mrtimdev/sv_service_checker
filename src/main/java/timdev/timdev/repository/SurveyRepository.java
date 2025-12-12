package timdev.timdev.repository;


import timdev.timdev.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, UUID> {
    Optional<Survey> findByAccessCode(String accessCode);
    boolean existsByAccessCode(String accessCode);

    @Query("SELECT s FROM Survey s LEFT JOIN FETCH s.questionGroups g LEFT JOIN FETCH g.questions WHERE s.id = :id")
    Optional<Survey> findByIdWithGroups(@Param("id") UUID id);

    @Query("SELECT s FROM Survey s " +
           "LEFT JOIN FETCH s.questionGroups g " +
           "LEFT JOIN FETCH g.questions q " +
           "WHERE s.id = :id " +
           "ORDER BY g.orderIndex, q.orderIndex")
    Optional<Survey> findByIdWithQuestionGroupsAndQuestions(@Param("id") UUID id);
}