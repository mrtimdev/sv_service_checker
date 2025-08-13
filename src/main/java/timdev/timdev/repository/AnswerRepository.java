package timdev.timdev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import timdev.timdev.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByResponseId(Long responseId);
}
