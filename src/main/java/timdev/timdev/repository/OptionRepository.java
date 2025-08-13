package timdev.timdev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import timdev.timdev.entity.Option;

public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findByQuestionId(Long questionId);
}
