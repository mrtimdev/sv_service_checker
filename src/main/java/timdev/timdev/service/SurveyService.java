package timdev.timdev.service;


import timdev.timdev.entity.QuestionGroup;
import timdev.timdev.entity.Survey;
import timdev.timdev.entity.SurveyResponse;
import timdev.timdev.repository.SurveyRepository;
import timdev.timdev.repository.SurveyResponseRepository;
import timdev.timdev.repository.QuestionRepository;
import timdev.timdev.repository.QuestionGroupRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.AccessOptions.SetOptions.Propagation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import timdev.timdev.entity.DriverInfo;
import timdev.timdev.entity.Question;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional(readOnly = false)
public class SurveyService {
    
    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionGroupRepository questionGroupRepository;
    
    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    @PersistenceContext
    private EntityManager entityManager;
    
    public List<Survey> getAllSurveys() {
        return surveyRepository.findAll();
    }
    
    public Optional<Survey> getSurveyById(UUID id) {
        return surveyRepository.findById(id);
    }
    
    public Optional<Survey> getSurveyByAccessCode(String accessCode) {
        return surveyRepository.findByAccessCode(accessCode);
    }
    
    @Transactional
    public Survey createSurvey(Survey incomingSurvey) {
        Survey survey = new Survey();
        survey.setTitle(incomingSurvey.getTitle());
        survey.setDescription(incomingSurvey.getDescription());
        survey.setScaleDescription(incomingSurvey.getScaleDescription());
        survey.setCommentTitle(incomingSurvey.getCommentTitle());
        survey.setCommentDescription(incomingSurvey.getCommentDescription());
        survey.setAccessCode(incomingSurvey.getAccessCode());
        survey.setActive(incomingSurvey.isActive());
        survey.setExpiresAt(incomingSurvey.getExpiresAt());

        if (incomingSurvey.getQuestionGroups() != null) {
            int groupIndex = 0;
            for (QuestionGroup incomingGroup : incomingSurvey.getQuestionGroups()) {
                QuestionGroup group = new QuestionGroup();
                group.setTitle(incomingGroup.getTitle());
                group.setOrderIndex(groupIndex++);
                group.setSurvey(survey); // important

                if (incomingGroup.getQuestions() != null) {
                    int questionIndex = 0;
                    for (Question incomingQuestion : incomingGroup.getQuestions()) {
                        Question question = new Question();
                        question.setText(incomingQuestion.getText());
                        question.setOrderIndex(questionIndex++);
                        question.setQuestionGroup(group); // important
                        group.addQuestion(question);
                    }
                }

                survey.addQuestionGroup(group);
            }
        }

        return surveyRepository.save(survey); // cascade saves everything
    }




    @Transactional
    public Survey updateSurvey(Survey incomingSurvey) {

        Survey dbSurvey = surveyRepository.findById(incomingSurvey.getId())
                .orElseThrow(() -> new EntityNotFoundException("Survey not found"));

        // Update simple fields
        dbSurvey.setTitle(incomingSurvey.getTitle());
        dbSurvey.setDescription(incomingSurvey.getDescription());
        dbSurvey.setScaleDescription(incomingSurvey.getScaleDescription());
        dbSurvey.setCommentTitle(incomingSurvey.getCommentTitle());
        dbSurvey.setCommentDescription(incomingSurvey.getCommentDescription());
        dbSurvey.setAccessCode(incomingSurvey.getAccessCode());
        dbSurvey.setActive(incomingSurvey.isActive());
        dbSurvey.setExpiresAt(incomingSurvey.getExpiresAt());

        // 1️⃣ DELETE existing groups + questions
        surveyResponseRepository.deleteBySurveyId(dbSurvey.getId());
        questionRepository.deleteByQuestionGroupSurveyId(dbSurvey.getId());
        questionGroupRepository.deleteBySurveyId(dbSurvey.getId());
        dbSurvey.getQuestionGroups().clear();

        // 2️⃣ ADD new groups from incomingSurvey
        if (incomingSurvey.getQuestionGroups() != null) {
            for (QuestionGroup newGroup : incomingSurvey.getQuestionGroups()) {

                newGroup.setId(null);            // ensure insertion, not update
                newGroup.setSurvey(dbSurvey);

                // Handle questions
                if (newGroup.getQuestions() != null) {
                    for (Question q : newGroup.getQuestions()) {
                        q.setId(null);
                        q.setQuestionGroup(newGroup);
                    }
                }

                dbSurvey.addQuestionGroup(newGroup);  // cascade saves group + questions
            }
        }

        return surveyRepository.save(dbSurvey);
    }



    // @Transactional
    // public Survey updateSurvey(Survey survey) {

    //     // Ensure proper parent-child relationships (same as createSurvey)
    //     if (survey.getQuestionGroups() != null) {
    //         for (QuestionGroup group : survey.getQuestionGroups()) {
    //             group.setSurvey(survey);
    //             if (group.getQuestions() != null) {
    //                 for (Question question : group.getQuestions()) {
    //                     question.setQuestionGroup(group);
    //                 }
    //             }
    //         }
    //     }

    //     return surveyRepository.save(survey); // save once
    // }

    
    // @Transactional
    // public Survey updateSurvey(Survey survey) {
    // Survey existingSurvey = surveyRepository.findById(survey.getId())
    // .orElseThrow(() -> new RuntimeException("Survey not found"));


    // // Update survey basic info
    // existingSurvey.setTitle(survey.getTitle());
    // existingSurvey.setDescription(survey.getDescription());


    // // Handle QuestionGroups
    // List<QuestionGroup> incomingGroups = survey.getQuestionGroups();


    // for (QuestionGroup incomingGroup : incomingGroups) {
    // if (incomingGroup.getId() != null) {
    // // Existing group, update
    // QuestionGroup existingGroup = questionGroupRepository.findByCustomId(incomingGroup.getId())
    // .orElseThrow(() -> new RuntimeException("Group not found"));
    // existingGroup.setTitle(incomingGroup.getTitle());
    // existingGroup.setCode(incomingGroup.getCode());
    // existingGroup.setOrderIndex(incomingGroup.getOrderIndex());


    // // Update questions
    // for (Question incomingQuestion : incomingGroup.getQuestions()) {
    // Optional<Question> optExistingQuestion = existingGroup.getQuestions().stream()
    // .filter(q -> q.getId() != null && q.getId().equals(incomingQuestion.getId()))
    // .findFirst();


    // if (optExistingQuestion.isPresent()) {
    // Question existingQuestion = optExistingQuestion.get();
    // existingQuestion.setText(incomingQuestion.getText());
    // existingQuestion.setDescription(incomingQuestion.getDescription());
    // existingQuestion.setRequired(incomingQuestion.isRequired());
    // existingQuestion.setOrderIndex(incomingQuestion.getOrderIndex());
    // } else {
    // // New question
    // incomingQuestion.setQuestionGroup(existingGroup);
    // existingGroup.getQuestions().add(incomingQuestion);
    // }
    // }
    // } else {
    // // New group
    // incomingGroup.setSurvey(existingSurvey);
    // for (Question q : incomingGroup.getQuestions()) {
    // q.setQuestionGroup(incomingGroup);
    // }
    // existingSurvey.getQuestionGroups().add(incomingGroup);
    // }
    // }


    // return surveyRepository.save(existingSurvey);
    // }

    // @Transactional
    public Survey deleteExistingQuestionGroups_(Survey survey) {

        // 1️⃣ Remove responses (OK to delete manually)
        // surveyResponseRepository.deleteBySurveyId(survey.getId());

        // 2️⃣ Delete groups + questions using orphanRemoval
        survey.getQuestionGroups().clear();

        return survey;
    }


    @Transactional(readOnly = false)
    public void deleteExistingQuestionGroups(Survey survey) {
        // Get managed survey
        Survey managedSurvey = entityManager.find(Survey.class, survey.getId());
        
        // Remove groups - orphanRemoval will handle deletion
        for (QuestionGroup group : new ArrayList<>(managedSurvey.getQuestionGroups())) {
            group.setSurvey(null);
            managedSurvey.getQuestionGroups().remove(group);
        }
        
        // Flush to trigger deletions
        entityManager.flush();
    }

    @Transactional(readOnly = false)
    public Survey updateSurvey(Survey dbSurvey, List<QuestionGroup> incomingGroups) {
        // First clear the persistence context to avoid stale entities
        entityManager.flush();
        entityManager.clear();
        
        // Reload the survey in current persistence context
        Survey managedSurvey = surveyRepository.findById(dbSurvey.getId())
            .orElseThrow(() -> new RuntimeException("Survey not found"));
            
        // Merge with incoming data
        return mergeSurveyGroups(managedSurvey, incomingGroups);
    }
    
    @Transactional
    public Survey mergeSurveyGroups(Survey dbSurvey, List<QuestionGroup> incomingGroups) {
        // Map DB groups by ID for easy lookup
        Map<String, QuestionGroup> dbGroupsById = dbSurvey.getQuestionGroups().stream()
                .filter(g -> g.getId() != null)
                .collect(Collectors.toMap(QuestionGroup::getId, g -> g));
        
        // Track groups to delete
        List<QuestionGroup> groupsToDelete = new ArrayList<>();
        
        // First pass: identify groups to delete
        for (QuestionGroup dbGroup : dbSurvey.getQuestionGroups()) {
            if (dbGroup.getId() != null && incomingGroups.stream()
                    .noneMatch(ig -> ig.getId() != null && ig.getId().equals(dbGroup.getId()))) {
                groupsToDelete.add(dbGroup);
            }
        }
        
        // Remove groups from the collection (orphanRemoval will handle deletion)
        dbSurvey.getQuestionGroups().removeAll(groupsToDelete);
        
        // Process incoming groups
        for (QuestionGroup incomingGroup : incomingGroups) {
            if (incomingGroup.getId() != null && dbGroupsById.containsKey(incomingGroup.getId())) {
                // Update existing group
                QuestionGroup dbGroup = dbGroupsById.get(incomingGroup.getId());
                dbGroup.setTitle(incomingGroup.getTitle());
                dbGroup.setCode(incomingGroup.getCode());
                dbGroup.setOrderIndex(incomingGroup.getOrderIndex());
                
                // Merge questions within the existing group
                mergeQuestions(dbGroup, incomingGroup.getQuestions());
                
            } else {
                // New group - clear ID and set survey reference
                incomingGroup.setId(null);
                incomingGroup.setSurvey(dbSurvey);
                
                // Process questions in new group
                if (incomingGroup.getQuestions() != null) {
                    for (Question q : incomingGroup.getQuestions()) {
                        q.setId(null);
                        q.setQuestionGroup(incomingGroup);
                    }
                }
                dbSurvey.getQuestionGroups().add(incomingGroup);
            }
        }
        
        // Save and return
        return surveyRepository.save(dbSurvey);
    }
    
    private void mergeQuestions(QuestionGroup dbGroup, List<Question> incomingQuestions) {
        if (incomingQuestions == null) {
            dbGroup.getQuestions().clear();
            return;
        }
        
        Map<String, Question> dbQuestionsById = dbGroup.getQuestions()
                .stream()
                .filter(q -> q.getId() != null)
                .collect(Collectors.toMap(Question::getId, q -> q));
        
        // Track questions to delete
        List<Question> questionsToDelete = new ArrayList<>();
        
        // Identify questions to delete
        for (Question dbQuestion : dbGroup.getQuestions()) {
            if (dbQuestion.getId() != null && incomingQuestions.stream()
                    .noneMatch(iq -> iq.getId() != null && iq.getId().equals(dbQuestion.getId()))) {
                questionsToDelete.add(dbQuestion);
            }
        }
        
        // Remove from collection
        dbGroup.getQuestions().removeAll(questionsToDelete);
        
        // Process incoming questions
        for (Question incomingQ : incomingQuestions) {
            if (incomingQ.getId() != null && dbQuestionsById.containsKey(incomingQ.getId())) {
                // Update existing question
                Question dbQ = dbQuestionsById.get(incomingQ.getId());
                dbQ.setText(incomingQ.getText());
                dbQ.setDescription(incomingQ.getDescription());
                dbQ.setOrderIndex(incomingQ.getOrderIndex());
                dbQ.setRequired(incomingQ.isRequired());
            } else {
                // New question
                incomingQ.setId(null);
                incomingQ.setQuestionGroup(dbGroup);
                dbGroup.getQuestions().add(incomingQ);
            }
        }
    }

    // @Transactional
public Survey replaceSurveyGroups(Survey dbSurvey, List<QuestionGroup> incomingGroups) {

    // 1️⃣ Remove all existing groups
    dbSurvey.getQuestionGroups().clear(); // orphanRemoval deletes DB rows

    // 2️⃣ Add incoming groups
    for (QuestionGroup group : incomingGroups) {
        group.setId(null);         // make sure Hibernate treats them as new
        group.setSurvey(dbSurvey);

        if (group.getQuestions() != null) {
            for (Question q : group.getQuestions()) {
                q.setId(null);    // new questions
                q.setQuestionGroup(group);
            }
        }

        dbSurvey.getQuestionGroups().add(group);
    }

    return dbSurvey; // dbSurvey is managed; changes will flush automatically
}


    public List<QuestionGroup> processSurveyFormDataForUpdate(Map<String, String> formParams) {
        List<QuestionGroup> questionGroups = new ArrayList<>();

        // Extract group data
        Map<Integer, Map<String, Object>> groupsData = new HashMap<>();
        for (Map.Entry<String, String> entry : formParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.startsWith("questionGroups[") && key.contains("].")) {
                int startIndex = key.indexOf("[") + 1;
                int endIndex = key.indexOf("]");
                int groupIndex = Integer.parseInt(key.substring(startIndex, endIndex));
                String property = key.substring(endIndex + 2);

                groupsData.putIfAbsent(groupIndex, new HashMap<>());
                groupsData.get(groupIndex).put(property, value);
            }
        }

        // Build QuestionGroup objects
        for (Map.Entry<Integer, Map<String, Object>> groupEntry : groupsData.entrySet()) {
            int groupIndex = groupEntry.getKey();
            Map<String, Object> groupData = groupEntry.getValue();

            QuestionGroup group = new QuestionGroup();

            // Set ID if exists (for existing DB group)
            String groupId = (String) groupData.getOrDefault("id", "");
            if (!groupId.isEmpty()) {
                group.setId(groupId);
            }

            group.setTitle((String) groupData.getOrDefault("title", ""));
            group.setCode((String) groupData.getOrDefault("code", ""));
            group.setOrderIndex(groupIndex);

            // Extract questions
            List<Question> questions = new ArrayList<>();
            Map<Integer, Map<String, Object>> questionsData = new HashMap<>();

            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.startsWith("questionGroups[" + groupIndex + "].questions[") && key.contains("].")) {
                    int startIndex = key.indexOf("questions[") + 10;
                    int endIndex = key.indexOf("]", startIndex);
                    int questionIndex = Integer.parseInt(key.substring(startIndex, endIndex));
                    String property = key.substring(endIndex + 2);

                    questionsData.putIfAbsent(questionIndex, new HashMap<>());
                    questionsData.get(questionIndex).put(property, value);
                }
            }

            for (Map.Entry<Integer, Map<String, Object>> questionEntry : questionsData.entrySet()) {
                int questionIndex = questionEntry.getKey();
                Map<String, Object> questionData = questionEntry.getValue();

                Question question = new Question();
                String questionId = (String) questionData.getOrDefault("id", "");
                if (!questionId.isEmpty()) {
                    question.setId(questionId);
                }

                question.setText((String) questionData.getOrDefault("text", ""));
                question.setDescription((String) questionData.getOrDefault("description", ""));
                question.setOrderIndex(questionIndex);
                question.setRequired(true);
                question.setQuestionGroup(group);

                questions.add(question);
            }

            group.setQuestions(questions);
            questionGroups.add(group);
        }

        questionGroupRepository.saveAll(questionGroups);

        return questionGroups;
    }




    @Transactional(readOnly = true)
    public Survey findByIdWithGroups(UUID id) {
        return surveyRepository.findByIdWithGroups(id)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + id));
    }
    
    private void updateQuestionGroupsForSurvey(Survey existingSurvey, List<QuestionGroup> newGroups) {
        if (newGroups == null) {
            existingSurvey.getQuestionGroups().clear();
            return;
        }
        
        // Map existing groups by ID
        Map<String, QuestionGroup> existingGroupsMap = existingSurvey.getQuestionGroups().stream()
            .filter(g -> g.getId() != null)
            .collect(Collectors.toMap(QuestionGroup::getId, g -> g));
        
        List<QuestionGroup> mergedGroups = new ArrayList<>();
        
        for (QuestionGroup newGroup : newGroups) {
            if (newGroup.getId() != null && existingGroupsMap.containsKey(newGroup.getId())) {
                // Update existing group
                QuestionGroup existingGroup = existingGroupsMap.get(newGroup.getId());
                updateQuestionGroupFields(existingGroup, newGroup);
                mergedGroups.add(existingGroup);
                existingGroupsMap.remove(newGroup.getId()); // Remove from map to track processed groups
            } else {
                // New group
                newGroup.setSurvey(existingSurvey);
                // Cascade to questions
                if (newGroup.getQuestions() != null) {
                    for (Question question : newGroup.getQuestions()) {
                        question.setQuestionGroup(newGroup);
                    }
                }
                mergedGroups.add(newGroup);
            }
        }
        
        // Remove groups that weren't in the new list
        existingSurvey.setQuestionGroups(mergedGroups);
    }
    
    private void updateQuestionGroupFields(QuestionGroup existing, QuestionGroup newData) {
        existing.setTitle(newData.getTitle());
        existing.setCode(newData.getCode());
        existing.setOrderIndex(newData.getOrderIndex());
        
        // Update questions
        updateQuestionsForGroup(existing, newData.getQuestions());
    }
    
    private void updateQuestionsForGroup(QuestionGroup existingGroup, List<Question> newQuestions) {
        if (newQuestions == null) {
            existingGroup.getQuestions().clear();
            return;
        }
        
        Map<String, Question> existingQuestionsMap = existingGroup.getQuestions().stream()
            .filter(q -> q.getId() != null)
            .collect(Collectors.toMap(Question::getId, q -> q));
        
        List<Question> mergedQuestions = new ArrayList<>();
        
        for (Question newQuestion : newQuestions) {
            if (newQuestion.getId() != null && existingQuestionsMap.containsKey(newQuestion.getId())) {
                // Update existing question
                Question existingQuestion = existingQuestionsMap.get(newQuestion.getId());
                updateQuestionFields(existingQuestion, newQuestion);
                mergedQuestions.add(existingQuestion);
                existingQuestionsMap.remove(newQuestion.getId());
            } else {
                // New question
                newQuestion.setQuestionGroup(existingGroup);
                mergedQuestions.add(newQuestion);
            }
        }
        
        existingGroup.setQuestions(mergedQuestions);
    }
    
    private void updateQuestionFields(Question existing, Question newData) {
        existing.setText(newData.getText());
        existing.setDescription(newData.getDescription());
        existing.setOrderIndex(newData.getOrderIndex());
        existing.setRequired(newData.isRequired());
    }


    @Transactional
    public void deleteBySurvey(Survey survey) {

        // 1️⃣ Delete responses (lowest level)
        entityManager.detach(survey);
        // surveyResponseRepository.deleteBySurveyId(survey.getId());

        // // 2️⃣ Delete questions via groups
        // questionRepository.deleteByQuestionGroupSurveyId(survey.getId());

        // // 3️⃣ Delete groups
        // questionGroupRepository.deleteBySurveyId(survey.getId());
        

         // Make sure the survey is managed
        Survey managedSurvey = surveyRepository.findById(survey.getId())
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        // Delete responses (lowest level)
        surveyResponseRepository.deleteBySurveyId(managedSurvey.getId());

        // Delete questions (via groups)
        questionRepository.deleteByQuestionGroupSurveyId(managedSurvey.getId());

        // Delete groups
        questionGroupRepository.deleteBySurveyId(managedSurvey.getId());

        // Clear groups collection in the parent
        managedSurvey.getQuestionGroups().clear();

        // Flush changes to DB
        entityManager.flush();
        entityManager.clear();
        
    }
    
    @Transactional
    public void deleteSurvey(UUID id) {

        // 1️⃣ Delete responses (lowest level)
        surveyResponseRepository.deleteBySurveyId(id);

        // 2️⃣ Delete questions via groups
        questionRepository.deleteByQuestionGroupSurveyId(id);

        // 3️⃣ Delete groups
        questionGroupRepository.deleteBySurveyId(id);

        // 4️⃣ Delete the survey
        surveyRepository.deleteById(id);
    }


    
    @Transactional
    public Survey duplicateSurvey(UUID id) {
        return surveyRepository.findById(id)
                .map(original -> {
                    Survey duplicate = new Survey();
                    duplicate.setTitle(original.getTitle() + " (Copy)");
                    duplicate.setDescription(original.getDescription());
                    duplicate.setScaleDescription(original.getScaleDescription());
                    duplicate.setCommentTitle(original.getCommentTitle());
                    duplicate.setCommentDescription(original.getCommentDescription());
                    duplicate.setActive(true);
                    
                    // Deep copy question groups
                    List<QuestionGroup> duplicatedGroups = new ArrayList<>();
                    for (QuestionGroup originalGroup : original.getQuestionGroups()) {
                        QuestionGroup duplicatedGroup = new QuestionGroup();
                        duplicatedGroup.setTitle(originalGroup.getTitle());
                        duplicatedGroup.setCode(originalGroup.getCode());
                        duplicatedGroup.setOrderIndex(originalGroup.getOrderIndex());
                        
                        // Deep copy questions
                        List<Question> duplicatedQuestions = new ArrayList<>();
                        for (Question originalQuestion : originalGroup.getQuestions()) {
                            Question duplicatedQuestion = new Question();
                            duplicatedQuestion.setText(originalQuestion.getText());
                            duplicatedQuestion.setDescription(originalQuestion.getDescription());
                            duplicatedQuestion.setOrderIndex(originalQuestion.getOrderIndex());
                            duplicatedQuestion.setRequired(originalQuestion.isRequired());
                            duplicatedQuestion.setQuestionGroup(duplicatedGroup);
                            duplicatedQuestions.add(duplicatedQuestion);
                        }
                        duplicatedGroup.setQuestions(duplicatedQuestions);
                        duplicatedGroup.setSurvey(duplicate);
                        duplicatedGroups.add(duplicatedGroup);
                    }
                    
                    duplicate.setQuestionGroups(duplicatedGroups);
                    return surveyRepository.save(duplicate);
                })
                .orElseThrow(() -> new RuntimeException("Survey not found with id: " + id));
    }
    
    @Transactional
    public SurveyResponse submitSurvey(String accessCode, Map<String, String> answers, 
                                     String comments, DriverInfo driverInfo,
                                     HttpServletRequest request) {
        Optional<Survey> surveyOpt = surveyRepository.findByAccessCode(accessCode);
        if (surveyOpt.isEmpty()) {
            throw new IllegalArgumentException("Survey not found");
        }
        
        Survey survey = surveyOpt.get();
        
        if (!survey.isActive()) {
            throw new IllegalArgumentException("Survey is no longer active");
        }
        
        if (survey.getExpiresAt() != null && LocalDateTime.now().isAfter(survey.getExpiresAt())) {
            throw new IllegalArgumentException("Survey has expired");
        }
        
        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setComments(comments);
        response.setIpAddress(request.getRemoteAddr());
        response.setUserAgent(request.getHeader("User-Agent"));
        
        // Convert answers to integer ratings
        Map<String, Integer> answerMap = response.getAnswers();
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            // if (entry.getKey().startsWith("q_")) {
            //     try {
            //         answerMap.put(entry.getKey().substring(2), Integer.parseInt(entry.getValue()));
            //     } catch (NumberFormatException e) {
            //         // Ignore invalid ratings
            //     }
            // }

            if (entry.getKey().startsWith("ans_")) {
                try {
                    answerMap.put(entry.getKey().substring(4), Integer.parseInt(entry.getValue()));
                } catch (NumberFormatException e) {
                    // Ignore invalid ratings
                }
            }
        }
        
        // Link driver info
        if (driverInfo != null) {
            driverInfo.setSurveyResponse(response);
            response.setDriverInfo(driverInfo);
        }
        
        return surveyResponseRepository.save(response);
    }
    
    public String generateSurveyLink(String baseUrl, String accessCode) {
        return baseUrl + "/survey/" + accessCode;
    }
    
    public long getResponseCount(UUID surveyId) {
        return surveyResponseRepository.countBySurveyId(surveyId);
    }

    
    public long countResponsesBySurveyId(UUID surveyId) {
        return surveyResponseRepository.countBySurveyId(surveyId);
    }

    
    public List<SurveyResponse> findBySurveyIdOrderBySubmittedAtDesc(UUID surveyId) {
        return surveyResponseRepository.findBySurveyIdOrderBySubmittedAtDesc(surveyId);
    }

    
    // public List<Object[]> getAverageRatingsByQuestion(UUID surveyId) {
    //     return surveyResponseRepository.getAverageRatingsByQuestion(surveyId);
    // }

    
    // public List<Object[]> getAverageRatingsByGroup(UUID surveyId) {
    //     return surveyResponseRepository.getAverageRatingsByGroup(surveyId);
    // }

    
    // public List<Object[]> getRatingDistribution(UUID surveyId) {
    //     return surveyResponseRepository.getRatingDistribution(surveyId);
    // }

    
    public SurveyResponse save(SurveyResponse response) {
        return surveyResponseRepository.save(response);
    }

    public Survey findById(UUID id) {
        return surveyRepository.findById(id).orElse(null);
    }

    
    public Survey save(Survey survey) {
        return surveyRepository.save(survey);
    }
    
    public Optional<Survey> findByAccessCode(String accessCode) {
        return surveyRepository.findByAccessCode(accessCode);
    }


    public Optional<Survey> findByIdWithQuestionGroupsAndQuestions(UUID id) {
        return surveyRepository.findByIdWithQuestionGroupsAndQuestions(id);
    }
    
    public Survey saveSurveyWithRelations(Survey survey) {
        return surveyRepository.save(survey);
    }
}