package timdev.timdev.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Question;

import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    void deleteByQuestionGroupSurveyId(UUID surveyId);

    List<Question> findByQuestionGroupIdOrderByOrderIndex(String questionGroupId);
}