package timdev.timdev.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import timdev.timdev.entity.QuestionGroup;
import timdev.timdev.repository.QuestionGroupRepository;


@Service
@Transactional(readOnly = false)
public class QuestionGroupService {
    
    private QuestionGroupRepository questionGroupRepository;
    
    
    public List<QuestionGroup> findBySurveyId(UUID surveyId) {
        return questionGroupRepository.findBySurveyIdOrderByOrderIndex(surveyId);
    }
    
    public QuestionGroup save(QuestionGroup questionGroup) {
        return questionGroupRepository.save(questionGroup);
    }
    
    public void delete(String id) {
        questionGroupRepository.deleteById(UUID.fromString(id));
    }
}