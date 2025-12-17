package timdev.timdev.controller;


import timdev.timdev.entity.Survey;
import timdev.timdev.entity.SurveyResponse;
import timdev.timdev.entity.DriverInfo;
import timdev.timdev.entity.QuestionGroup;
import timdev.timdev.service.SurveyService;
import timdev.timdev.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import timdev.timdev.entity.Question;
import timdev.timdev.repository.SurveyResponseRepository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import timdev.timdev.repository.DriverInfoRepository;



// @RequestMapping("survey")
@Controller
public class SurveyController {
    
    @Autowired
    private SurveyService surveyService;
    
    @Autowired
    private QuestionService questionService;

    @Autowired private SurveyResponseRepository surveyResponseRepository;

    // @Autowired
    // private SurveyResponseService surveyResponseService;
    @Autowired
    private DriverInfoRepository driverInfoRepository;

    

    @PersistenceContext
    private EntityManager entityManager;
    
     
    @GetMapping({"/admin/survey", "survey"})
    public String adminSurveyDashboard(Model model) {
        model.addAttribute("surveys", surveyService.getAllSurveys());
        return "admin-dashboard";
    }

    
    @GetMapping({"/dashboard", "/admin"})
    public String adminDashboard(Model model) {
        return "redirect:/admin/survey";
    }


    @GetMapping("/survey/{accessCode}")
        public String showSurvey(@PathVariable String accessCode, Model model, HttpServletRequest request) {
            Optional<Survey> surveyOpt = surveyService.getSurveyByAccessCode(accessCode);
            
            if (surveyOpt.isEmpty()) {
                model.addAttribute("error", "សំណើមិនត្រឹមត្រូវ ឬបានផុតកំណត់។");
                return "survey-response-error";
            }
            
            Survey survey = surveyOpt.get();
            
            // Check if survey is active
            if (!survey.isActive()) {
                model.addAttribute("error", "ការស្ទង់មតិនេះបានបិទរួចហើយ។");
                return "survey-response-error";
            }
            
            // Check if survey has expired
            if (survey.getExpiresAt() != null && LocalDateTime.now().isAfter(survey.getExpiresAt())) {
                model.addAttribute("error", "ការស្ទង់មតិនេះបានផុតកំណត់រួចហើយ។");
                return "survey-response-error";
            }

            // ---------------------------
            // 🔥 NEW: Duplicate submission check
            // ---------------------------
            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            boolean alreadySubmitted = surveyResponseRepository
                    .existsBySurveyIdAndIpAddressAndUserAgent(survey.getId(), ip, userAgent);

            // if (alreadySubmitted) {
            //     SurveyResponse response = surveyResponseRepository
            //         .findBySurveyIdAndIpAddressAndUserAgent(survey.getId(), ip, userAgent);
                
            //     DriverInfo driverInfo = response.getDriverInfo();
            //     model.addAttribute("isAlreadySubmitted", true);
            //     model.addAttribute("error", "អ្នកបានបញ្ចូនការឆ្លើយតបរួចហើយ។");

            //     model.addAttribute("response", response);
            //     model.addAttribute("driverInfo", driverInfo);
            //     model.addAttribute("survey", survey);
            //     model.addAttribute("submittedAt", response.getSubmittedAt());
            //     model.addAttribute("answers", response.getAnswers());
            //     return "survey-response-view";  // or another page to show message
            // }
            
            model.addAttribute("survey", survey);
            model.addAttribute("driverInfo", new DriverInfo());
            return "survey-form";
        }

    @PostMapping("/survey/{accessCode}/submit")
    @Transactional
    public String submitSurvey(@PathVariable String accessCode,
                            @RequestParam Map<String, String> formParams,
                            @ModelAttribute DriverInfo driverInfo,
                            BindingResult bindingResult,
                            HttpServletRequest request,
                            Model model) {

        if (driverInfo.getTruckNumber() == null || driverInfo.getTruckNumber().trim().isEmpty()) {
            bindingResult.rejectValue("truckNumber", "required", "សូមបញ្ចូលលេខឡាន");
        }
        
        if (driverInfo.getFullName() == null || driverInfo.getFullName().trim().isEmpty()) {
            bindingResult.rejectValue("fullName", "required", "សូមបញ្ចូលឈ្មោះពេញ");
        }
        
        // Extract comments
        String comments = formParams.get("comments");
        
        // Check if all required questions are answered
        Survey survey = surveyService.getSurveyByAccessCode(accessCode)
            .orElseThrow(() -> new IllegalArgumentException("Survey not found"));

        Optional<SurveyResponse> responseOpt =
            surveyResponseRepository.findBySurveyIdAndDriverInfo(
                survey.getId(),
                driverInfo.getFullName(),
                driverInfo.getPhoneNumber()
            );

        if (responseOpt.isPresent()) {
            SurveyResponse response_ = responseOpt.get();
                
                DriverInfo driverInfo_ = response_.getDriverInfo();
                model.addAttribute("isAlreadySubmitted", true);
                model.addAttribute("error", "អ្នកបានបញ្ចូនការឆ្លើយតបរួចហើយ។");

                model.addAttribute("response", response_);
                model.addAttribute("driverInfo", driverInfo_);
                model.addAttribute("survey", survey);
                model.addAttribute("submittedAt", response_.getSubmittedAt());
                model.addAttribute("answers", response_.getAnswers());
                return "survey-response-view"; 

        }

            
        
        List<String> unansweredQuestions = new ArrayList<>();
        for (QuestionGroup group : survey.getQuestionGroups()) {
            for (Question question : group.getQuestions()) {
                if (question.isRequired()) {
                    String questionId = question.getId();
                    // String answer = formParams.get("q_" + questionId);
                    String answer = formParams.get("ans_" + questionId);
                    if (answer == null || answer.trim().isEmpty()) {
                        unansweredQuestions.add(question.getText());
                    }
                }
            }
        }
        
        if (!unansweredQuestions.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("សូមជ្រើសរើសពិន្ទុសម្រាប់សំណួរទាំងអស់៖<br>");
            for (int i = 0; i < Math.min(unansweredQuestions.size(), 3); i++) {
                errorMessage.append("• ").append(unansweredQuestions.get(i)).append("<br>");
            }
            if (unansweredQuestions.size() > 3) {
                errorMessage.append("• និងសំណួរផ្សេងទៀត...");
            }
            
            model.addAttribute("survey", survey);
            model.addAttribute("driverInfo", driverInfo);
            model.addAttribute("error", errorMessage.toString());
            model.addAttribute("unansweredQuestions", unansweredQuestions);
            return "survey-form";
        }
        
        // Submit survey
        SurveyResponse response = surveyService.submitSurvey(accessCode, formParams, comments, driverInfo, request);
        
        model.addAttribute("message", "សូមអរគុណ! ការឆ្លើយតបរបស់អ្នកត្រូវបានរក្សាទុក។");
        model.addAttribute("driverInfo", driverInfo);
        model.addAttribute("responseDate", LocalDateTime.now());
        
        // return "survey-response";
        return "redirect:/survey/response/" + survey.getId() + "/" + response.getId();
    }

    // GET method to view a submitted response
    @GetMapping("/survey/response/{surveyId}/{responseId}")
    public String viewSurveyResponse(@PathVariable UUID surveyId,
                                    @PathVariable String responseId,
                                    Model model) {
        try {
            SurveyResponse response = surveyResponseRepository
                .findByIdAndSurveyId(responseId, surveyId)
                .orElseThrow(() -> new IllegalArgumentException("Survey response not found"));
            
            DriverInfo driverInfo = response.getDriverInfo();
            Survey survey = response.getSurvey();
            
            model.addAttribute("response", response);
            model.addAttribute("driverInfo", driverInfo);
            model.addAttribute("survey", survey);
            model.addAttribute("submittedAt", response.getSubmittedAt());
            model.addAttribute("answers", response.getAnswers());
            
            return "survey-response-view";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "មិនមានការឆ្លើយតបនេះទេ។");
            return "survey-response-error";
        } catch (Exception e) {
            model.addAttribute("error", "មានកំហុសកើតឡើងក្នុងការទាញយកទិន្នន័យ។");
            return "survey-response-error";
        }
    }
    
    
    // GET method for admin to view all responses for a survey
    @GetMapping("/{surveyId}/responses")
    public String viewAllResponses(@PathVariable UUID surveyId, Model model) {
        try {
            Survey survey = surveyService.getSurveyById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("Survey not found"));
            
            List<SurveyResponse> responses = survey.getResponses();
            
            model.addAttribute("survey", survey);
            model.addAttribute("responses", responses);
            model.addAttribute("responseCount", responses.size());
            
            return "survey-responses-list";
            
        } catch (Exception e) {
            model.addAttribute("error", "មិនអាចទាញយកទិន្នន័យបាន។");
            return "survey-response-error";
        }
    }
   
    @GetMapping("/admin/survey/new")
    public String newSurveyForm(Model model) {
        Survey survey = questionService.createDefaultSurvey();
        model.addAttribute("survey", survey);
        model.addAttribute("pageTitle", "បង្កើតការស្ទង់មតិថ្មី");
        return "survey-form-edit";
    }
    
    @GetMapping("/admin/survey/{id}/edit")
    public String editSurveyForm(@PathVariable UUID id, Model model) {
        Optional<Survey> surveyOpt = surveyService.getSurveyById(id);
        if (surveyOpt.isEmpty()) {
            model.addAttribute("error", "សំណើមិនត្រឹមត្រូវ។");
            return "error";
        }
        
        model.addAttribute("survey", surveyOpt.get());
        model.addAttribute("pageTitle", "កែសម្រួលការស្ទង់មតិ");
        return "survey-form-edit";
    }
    
    @PostMapping("/admin/survey")
    public String createSurvey(@ModelAttribute Survey survey, 
                             @RequestParam Map<String, String> formParams,
                             RedirectAttributes redirectAttributes) {
        try {
            // Process the form data to build the survey structure
            // processSurveyFormDataNew(survey, formParams);
            
            Survey savedSurvey = surveyService.createSurvey(survey);
            // Survey savedSurvey = surveyService.createSurvey(survey);
            redirectAttributes.addFlashAttribute("message", "ការស្ទង់មតិត្រូវបានបង្កើតដោយជោគជ័យ!");
            return "redirect:/admin/survey/" + savedSurvey.getId() + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "មានកំហុសក្នុងការបង្កើតការស្ទង់មតិ៖ " + e.getMessage());
            return "redirect:/admin/survey/new";
        }
    }

    // @Transactional
    @PostMapping("/admin/survey/{id}")
public String updateSurvey(@PathVariable UUID id,
                        @ModelAttribute Survey surveyForm,
                        @RequestParam Map<String, String> formParams,
                        RedirectAttributes redirectAttributes) {
    try {
        // Process form data first
        // Survey updatedSurvey = processSurveyUpdate(id, surveyForm, formParams);
        
        // Save through service
        surveyService.updateSurvey(surveyForm);
        
        redirectAttributes.addFlashAttribute("message", "Survey updated successfully!");
        return "redirect:/admin/survey/" + id + "/edit";
        
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error updating survey: " + e.getMessage());
        return "redirect:/admin/survey/" + id + "/edit";
    }
}

// Non-transactional helper method
private Survey processSurveyUpdate(UUID id, Survey surveyForm, Map<String, String> formParams) {
    Survey survey = new Survey();
    survey.setId(id);
    survey.setTitle(surveyForm.getTitle());
    survey.setDescription(surveyForm.getDescription());
    survey.setScaleDescription(surveyForm.getScaleDescription());
    survey.setAccessCode(surveyForm.getAccessCode());
    survey.setActive(surveyForm.isActive());
    survey.setExpiresAt(surveyForm.getExpiresAt());
    
    // Process groups (same logic as above)
    List<QuestionGroup> groups = processFormData(formParams, survey);
    survey.setQuestionGroups(groups);
    
    return survey;
}

private List<QuestionGroup> processFormData(Map<String, String> formParams, Survey survey) {
    List<QuestionGroup> groups = new ArrayList<>();
    
    // Parse group indices
    Set<Integer> groupIndices = new HashSet<>();
    
    for (String key : formParams.keySet()) {
        if (key.startsWith("questionGroups[") && key.contains("].")) {
            int start = key.indexOf("[") + 1;
            int end = key.indexOf("]");
            int groupIndex = Integer.parseInt(key.substring(start, end));
            groupIndices.add(groupIndex);
        }
    }
    
    // Sort indices
    List<Integer> sortedIndices = new ArrayList<>(groupIndices);
    Collections.sort(sortedIndices);
    
    // Create groups
    for (int groupIndex : sortedIndices) {
        QuestionGroup group = new QuestionGroup();
        group.setSurvey(survey);
        
        // Get group properties
        String titleKey = "questionGroups[" + groupIndex + "].title";
        String codeKey = "questionGroups[" + groupIndex + "].code";
        
        group.setTitle(formParams.getOrDefault(titleKey, ""));
        group.setCode(formParams.getOrDefault(codeKey, ""));
        group.setOrderIndex(groupIndex);
        
        // Parse questions for this group
        List<Question> questions = new ArrayList<>();
        Set<Integer> questionIndices = new HashSet<>();
        
        for (String key : formParams.keySet()) {
            if (key.startsWith("questionGroups[" + groupIndex + "].questions[") && key.contains("].")) {
                int start = key.indexOf("questions[") + 10;
                int end = key.indexOf("]", start);
                int questionIndex = Integer.parseInt(key.substring(start, end));
                questionIndices.add(questionIndex);
            }
        }
        
        List<Integer> sortedQuestionIndices = new ArrayList<>(questionIndices);
        Collections.sort(sortedQuestionIndices);
        
        for (int questionIndex : sortedQuestionIndices) {
            Question question = new Question();
            question.setQuestionGroup(group);
            
            String textKey = "questionGroups[" + groupIndex + "].questions[" + questionIndex + "].text";
            String descKey = "questionGroups[" + groupIndex + "].questions[" + questionIndex + "].description";
            
            question.setText(formParams.getOrDefault(textKey, ""));
            question.setDescription(formParams.getOrDefault(descKey, ""));
            question.setOrderIndex(questionIndex);
            question.setRequired(true);
            
            questions.add(question);
        }
        
        group.setQuestions(questions);
        groups.add(group);
    }
    
    return groups;
}

// Helper method to process form data for update
private List<QuestionGroup> processSurveyFormDataForUpdate(Map<String, String> formParams, Survey survey) {
    List<QuestionGroup> questionGroups = new ArrayList<>();
    
    // Extract group data from form parameters
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
        
        // For updates, don't set IDs from form - let Hibernate generate new ones
        group.setTitle((String) groupData.getOrDefault("title", ""));
        group.setCode((String) groupData.getOrDefault("code", ""));
        group.setOrderIndex(groupIndex);
        group.setSurvey(survey); // Set the survey reference
        
        // Extract questions for this group
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
        
        // Build Question objects
        for (Map.Entry<Integer, Map<String, Object>> questionEntry : questionsData.entrySet()) {
            int questionIndex = questionEntry.getKey();
            Map<String, Object> questionData = questionEntry.getValue();
            
            Question question = new Question();
            
            // Don't set ID from form for new questions
            question.setText((String) questionData.getOrDefault("text", ""));
            question.setDescription((String) questionData.getOrDefault("description", ""));
            question.setOrderIndex(questionIndex);
            question.setRequired(true);
            question.setQuestionGroup(group); // Set the group reference
            
            questions.add(question);
        }
        
        group.setQuestions(questions);
        questionGroups.add(group);
    }
    
    return questionGroups;
}

    private void processSurveyFormDataNew(Survey survey, Map<String, String> formParams) {
        List<QuestionGroup> questionGroups = new ArrayList<>();

        // Map to hold group -> question mapping
        Map<Integer, List<Question>> groupQuestionsMap = new HashMap<>();

        // First, collect questions by group index
        for (Map.Entry<String, String> entry : formParams.entrySet()) {
            String key = entry.getKey();

            if (key.startsWith("questionGroups[") && key.contains("].questions[")) {
                // Extract groupIndex
                int startGroup = key.indexOf("[") + 1;
                int endGroup = key.indexOf("]");
                int groupIndex = Integer.parseInt(key.substring(startGroup, endGroup));

                // Extract questionIndex
                int startQuestion = key.indexOf("questions[") + 10;
                int endQuestion = key.indexOf("]", startQuestion);
                int questionIndex = Integer.parseInt(key.substring(startQuestion, endQuestion));

                // Create Question object with only index
                Question q = new Question();
                q.setOrderIndex(questionIndex);

                // Add question to the group
                groupQuestionsMap.computeIfAbsent(groupIndex, k -> new ArrayList<>()).add(q);
            }
        }

        // Build QuestionGroup objects
        for (Map.Entry<Integer, List<Question>> groupEntry : groupQuestionsMap.entrySet()) {
            int groupIndex = groupEntry.getKey();
            List<Question> questions = groupEntry.getValue();

            QuestionGroup group = new QuestionGroup();
            group.setOrderIndex(groupIndex);

            // Set bidirectional relationship
            for (Question q : questions) {
                q.setQuestionGroup(group);
            }

            group.setQuestions(questions);
            group.setSurvey(survey);

            questionGroups.add(group);
        }

        survey.setQuestionGroups(questionGroups);
    }

    


    private void processSurveyFormData(Survey survey, Map<String, String> formParams) {
        List<QuestionGroup> questionGroups = new ArrayList<>();
        
        // Extract group data from form parameters
        Map<Integer, Map<String, Object>> groupsData = new HashMap<>();
        
        for (Map.Entry<String, String> entry : formParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (key.startsWith("questionGroups[") && key.contains("].")) {
                // Parse group index
                int startIndex = key.indexOf("[") + 1;
                int endIndex = key.indexOf("]");
                int groupIndex = Integer.parseInt(key.substring(startIndex, endIndex));
                
                // Get the property name
                String property = key.substring(endIndex + 2); // Skip "]."
                
                // Initialize group data if not exists
                groupsData.putIfAbsent(groupIndex, new HashMap<>());
                
                // Store the property
                groupsData.get(groupIndex).put(property, value);
            }
        }
        
        // Build QuestionGroup objects
        for (Map.Entry<Integer, Map<String, Object>> groupEntry : groupsData.entrySet()) {
            int groupIndex = groupEntry.getKey();
            Map<String, Object> groupData = groupEntry.getValue();
            
            QuestionGroup group = new QuestionGroup();
            
            // Check if there's a group ID in the form data
            String groupId = (String) groupData.getOrDefault("id", "");
            if (!groupId.isEmpty()) {
                try {
                    group.setId(groupId);
                } catch (IllegalArgumentException e) {
                    // Ignore invalid UUID, will create new group
                }
            }
            
            group.setTitle((String) groupData.getOrDefault("title", ""));
            group.setCode((String) groupData.getOrDefault("code", ""));
            group.setOrderIndex(groupIndex);
            
            // Extract questions for this group
            List<Question> questions = new ArrayList<>();
            Map<Integer, Map<String, Object>> questionsData = new HashMap<>();
            
            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if (key.startsWith("questionGroups[" + groupIndex + "].questions[") && key.contains("].")) {
                    // Parse question index
                    int startIndex = key.indexOf("questions[") + 10;
                    int endIndex = key.indexOf("]", startIndex);
                    int questionIndex = Integer.parseInt(key.substring(startIndex, endIndex));
                    
                    // Get the property name
                    String property = key.substring(endIndex + 2); // Skip "]."
                    
                    // Initialize question data if not exists
                    questionsData.putIfAbsent(questionIndex, new HashMap<>());
                    
                    // Store the property
                    questionsData.get(questionIndex).put(property, value);
                }
            }
            
            // Build Question objects
            for (Map.Entry<Integer, Map<String, Object>> questionEntry : questionsData.entrySet()) {
                int questionIndex = questionEntry.getKey();
                Map<String, Object> questionData = questionEntry.getValue();
                
                Question question = new Question();
                
                // Check if there's a question ID in the form data
                String questionId = (String) questionData.getOrDefault("id", "");
                if (!questionId.isEmpty()) {
                    try {
                        question.setId(questionId);
                    } catch (IllegalArgumentException e) {
                        // Ignore invalid UUID, will create new question
                    }
                }
                
                question.setText((String) questionData.getOrDefault("text", ""));
                question.setDescription((String) questionData.getOrDefault("description", ""));
                question.setOrderIndex(questionIndex);
                question.setRequired(true);
                question.setQuestionGroup(group);
                
                questions.add(question);
            }
            
            group.setQuestions(questions);
            group.setSurvey(survey);
            questionGroups.add(group);
        }
        
        survey.setQuestionGroups(questionGroups);
    }
    
    private void processSurveyFormData__old(Survey survey, Map<String, String> formParams) {
        List<QuestionGroup> questionGroups = new ArrayList<>();
        
        // Extract group data from form parameters
        Map<Integer, Map<String, Object>> groupsData = new HashMap<>();
        
        for (Map.Entry<String, String> entry : formParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (key.startsWith("questionGroups[") && key.contains("].")) {
                // Parse group index
                int startIndex = key.indexOf("[") + 1;
                int endIndex = key.indexOf("]");
                int groupIndex = Integer.parseInt(key.substring(startIndex, endIndex));
                
                // Get the property name
                String property = key.substring(endIndex + 2); // Skip "]."
                
                // Initialize group data if not exists
                groupsData.putIfAbsent(groupIndex, new HashMap<>());
                
                // Store the property
                groupsData.get(groupIndex).put(property, value);
            }
        }
        
        // Build QuestionGroup objects
        for (Map.Entry<Integer, Map<String, Object>> groupEntry : groupsData.entrySet()) {
            int groupIndex = groupEntry.getKey();
            Map<String, Object> groupData = groupEntry.getValue();
            
            QuestionGroup group = new QuestionGroup();
            group.setTitle((String) groupData.getOrDefault("title", ""));
            group.setCode((String) groupData.getOrDefault("code", ""));
            group.setOrderIndex(groupIndex);
            
            // Extract questions for this group
            List<Question> questions = new ArrayList<>();
            Map<Integer, Map<String, Object>> questionsData = new HashMap<>();
            
            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if (key.startsWith("questionGroups[" + groupIndex + "].questions[") && key.contains("].")) {
                    // Parse question index
                    int startIndex = key.indexOf("questions[") + 10;
                    int endIndex = key.indexOf("]", startIndex);
                    int questionIndex = Integer.parseInt(key.substring(startIndex, endIndex));
                    
                    // Get the property name
                    String property = key.substring(endIndex + 2); // Skip "]."
                    
                    // Initialize question data if not exists
                    questionsData.putIfAbsent(questionIndex, new HashMap<>());
                    
                    // Store the property
                    questionsData.get(questionIndex).put(property, value);
                }
            }
            
            // Build Question objects
            for (Map.Entry<Integer, Map<String, Object>> questionEntry : questionsData.entrySet()) {
                int questionIndex = questionEntry.getKey();
                Map<String, Object> questionData = questionEntry.getValue();
                
                Question question = new Question();
                question.setText((String) questionData.getOrDefault("text", ""));
                question.setDescription((String) questionData.getOrDefault("description", ""));
                question.setOrderIndex(questionIndex);
                question.setRequired("on".equals(questionData.getOrDefault("required", "on")));
                question.setQuestionGroup(group);
                
                questions.add(question);
            }
            
            group.setQuestions(questions);
            group.setSurvey(survey);
            questionGroups.add(group);
        }
        
        survey.setQuestionGroups(questionGroups);
    }
    
    @PostMapping("/admin/survey/{id}/duplicate")
    public String duplicateSurvey(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Survey duplicatedSurvey = surveyService.duplicateSurvey(id);
            redirectAttributes.addFlashAttribute("message", "ការស្ទង់មតិត្រូវបានចម្លងដោយជោគជ័យ!");
            return "redirect:/admin/survey/" + duplicatedSurvey.getId() + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "មានកំហុសក្នុងការចម្លងការស្ទង់មតិ៖ " + e.getMessage());
            return "redirect:/admin";
        }
    }
    
    @PostMapping("/admin/survey/{id}/delete")
    @Transactional
    public String deleteSurvey(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            surveyService.deleteSurvey(id);
            redirectAttributes.addFlashAttribute("message", "ការស្ទង់មតិត្រូវបានលុបដោយជោគជ័យ!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "មានកំហុសក្នុងការលុបការស្ទង់មតិ៖ " + e.getMessage());
            e.printStackTrace(); // Add this for debugging
        }
        return "redirect:/admin/survey";
    }



    @GetMapping("/admin/survey/results/{accessCode}")
    public String viewResults(@PathVariable String accessCode, Model model) {
        Optional<Survey> surveyOpt = surveyService.getSurveyByAccessCode(accessCode);
        
        if (surveyOpt.isEmpty()) {
            model.addAttribute("error", "សំណើមិនត្រឹមត្រូវ។ កូដស្ទង់មតិមិនត្រឹមត្រូវ ឬមិនមាន។");
            return "error";
        }
        
        Survey survey = surveyOpt.get();
        UUID surveyId = survey.getId();
        
        // Get actual response count
        long responseCount = surveyService.countResponsesBySurveyId(surveyId);
        
        // Get responses for this survey
        List<SurveyResponse> responses = surveyService.findBySurveyIdOrderBySubmittedAtDesc(surveyId);
        
        // Calculate statistics based on actual data
        Map<String, Object> statistics = calculateSurveyStatistics(survey, responses);
        
        // Get comments from responses
        List<Map<String, Object>> comments = extractCommentsFromResponses(responses);
        
        model.addAttribute("survey", survey);
        model.addAttribute("responseCount", responseCount);
        model.addAttribute("statistics", statistics);
        model.addAttribute("comments", comments);
        model.addAttribute("accessCode", accessCode);
        model.addAttribute("responses", responses);
        
        // Calculate question-wise statistics
        Map<String, Map<String, Object>> questionStats = calculateQuestionStatistics(survey, responses);
        model.addAttribute("questionStats", questionStats);
        
        // Calculate group-wise statistics
        Map<String, Map<String, Object>> groupStats = calculateGroupStatistics(survey, responses);
        model.addAttribute("groupStats", groupStats);
        
        return "survey-results";
    }

    @GetMapping("/admin/survey/results/{accessCode}/details")
    public String viewResultsWithDetails(@PathVariable String accessCode, Model model) {
        Optional<Survey> surveyOpt = surveyService.getSurveyByAccessCode(accessCode);
        
        if (surveyOpt.isEmpty()) {
            model.addAttribute("error", "សំណើមិនត្រឹមត្រូវ។ កូដស្ទង់មតិមិនត្រឹមត្រូវ ឬមិនមាន។");
            return "error";
        }
        
        Survey survey = surveyOpt.get();
        UUID surveyId = survey.getId();
        
        // Get actual response count
        long responseCount = surveyService.countResponsesBySurveyId(surveyId);
        
        // Get responses for this survey
        List<SurveyResponse> responses = surveyService.findBySurveyIdOrderBySubmittedAtDesc(surveyId);
        
        // Calculate statistics based on actual data
        Map<String, Object> statistics = calculateSurveyStatistics(survey, responses);
        
        // Get comments from responses
        List<Map<String, Object>> comments = extractCommentsFromResponses(responses);
        
        model.addAttribute("survey", survey);
        model.addAttribute("responseCount", responseCount);
        model.addAttribute("statistics", statistics);
        model.addAttribute("comments", comments);
        model.addAttribute("accessCode", accessCode);
        model.addAttribute("responses", responses);
        
        // Calculate question-wise statistics
        Map<String, Map<String, Object>> questionStats = calculateQuestionStatistics(survey, responses);
        model.addAttribute("questionStats", questionStats);
        
        // Calculate group-wise statistics
        Map<String, Map<String, Object>> groupStats = calculateGroupStatistics(survey, responses);
        model.addAttribute("groupStats", groupStats);
        
        return "survey-results-details";
    }
    
    @GetMapping("/admin/results-by-id/{id}")
    public String viewResultsById(@PathVariable UUID id, Model model) {
        Optional<Survey> surveyOpt = surveyService.getSurveyById(id);
        
        if (surveyOpt.isEmpty()) {
            model.addAttribute("error", "សំណើមិនត្រឹមត្រូវ។ ការស្ទង់មតិមិនត្រឹមត្រូវ ឬមិនមាន។");
            return "error";
        }
        
        Survey survey = surveyOpt.get();
        return "redirect:/admin/results/" + survey.getAccessCode();
    }
    
    private Map<String, Object> calculateSurveyStatistics(Survey survey, List<SurveyResponse> responses) {
        Map<String, Object> stats = new HashMap<>();
        
        if (responses.isEmpty()) {
            stats.put("averageRating", 0.0);
            stats.put("responseRate", 0);
            stats.put("highestRating", 0.0);
            stats.put("lowestRating", 0.0);
            stats.put("totalResponses", 0);
            stats.put("completionTime", "N/A");
            
            // Empty rating distribution
            Map<String, Integer> ratingDistribution = new LinkedHashMap<>();
            ratingDistribution.put("មិនល្អខ្លាំង (1)", 0);
            ratingDistribution.put("មិនល្អ (2)", 0);
            ratingDistribution.put("មធ្យម (3)", 0);
            ratingDistribution.put("ល្អ (4)", 0);
            ratingDistribution.put("ល្អបំផុត (5)", 0);
            stats.put("ratingDistribution", ratingDistribution);
            
            return stats;
        }
        
        // Calculate overall statistics
        double totalRating = 0;
        int totalAnswers = 0;
        double highestRating = 0;
        double lowestRating = 5;
        Map<Integer, Integer> ratingCounts = new HashMap<>();
        
        for (SurveyResponse response : responses) {
            Map<String, Integer> answers = response.getAnswers();
            for (Integer rating : answers.values()) {
                totalRating += rating;
                totalAnswers++;
                highestRating = Math.max(highestRating, rating);
                lowestRating = Math.min(lowestRating, rating);
                
                ratingCounts.put(rating, ratingCounts.getOrDefault(rating, 0) + 1);
            }
        }
        
        double averageRating = totalRating / totalAnswers;
        
        // Calculate rating distribution percentages
        Map<String, Integer> ratingDistribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            int count = ratingCounts.getOrDefault(i, 0);
            int percentage = (int) ((count * 100.0) / totalAnswers);
            String label = switch (i) {
                case 1 -> "មិនល្អខ្លាំង (1)";
                case 2 -> "មិនល្អ (2)";
                case 3 -> "មធ្យម (3)";
                case 4 -> "ល្អ (4)";
                case 5 -> "ល្អបំផុត (5)";
                default -> String.valueOf(i);
            };
            ratingDistribution.put(label, percentage);
        }
        
        stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        stats.put("responseRate", calculateResponseRate(survey));
        stats.put("highestRating", highestRating);
        stats.put("lowestRating", lowestRating);
        stats.put("totalResponses", responses.size());
        stats.put("completionTime", "10-15 នាទី");
        stats.put("ratingDistribution", ratingDistribution);
        
        return stats;
    }
    
    private Map<String, Map<String, Object>> calculateQuestionStatistics_old(Survey survey, List<SurveyResponse> responses) {
        Map<String, Map<String, Object>> questionStats = new HashMap<>();
        
        // Initialize all questions
        for (QuestionGroup group : survey.getQuestionGroups()) {
            for (Question question : group.getQuestions()) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalRatings", 0);
                stats.put("sumRatings", 0);
                stats.put("average", 0.0);
                stats.put("distribution", new int[5]);
                questionStats.put(question.getId(), stats);
            }
        }
        
        // Calculate statistics from responses
        for (SurveyResponse response : responses) {
            Map<String, Integer> answers = response.getAnswers();
            for (Map.Entry<String, Integer> entry : answers.entrySet()) {
                String questionId = entry.getKey();
                Integer rating = entry.getValue();
                
                Map<String, Object> stats = questionStats.get(questionId);
                if (stats != null) {
                    int totalRatings = (int) stats.get("totalRatings");
                    int sumRatings = (int) stats.get("sumRatings");
                    int[] distribution = (int[]) stats.get("distribution");
                    
                    totalRatings++;
                    sumRatings += rating;
                    if (rating >= 1 && rating <= 5) {
                        distribution[rating - 1]++;
                    }
                    
                    double average = totalRatings > 0 ? (double) sumRatings / totalRatings : 0;
                    
                    stats.put("totalRatings", totalRatings);
                    stats.put("sumRatings", sumRatings);
                    stats.put("average", Math.round(average * 10.0) / 10.0);
                    stats.put("distribution", distribution);
                }
            }
        }
        
        return questionStats;
    }

    private Map<String, Map<String, Object>> calculateQuestionStatistics(
        Survey survey, List<SurveyResponse> responses) {

        Map<String, Map<String, Object>> questionStats = new HashMap<>();

        // Initialize stats for each question
        for (QuestionGroup group : survey.getQuestionGroups()) {
            for (Question q : group.getQuestions()) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalRatings", 0);
                stats.put("sumRatings", 0);
                stats.put("average", 0.0);
                stats.put("distribution", new int[5]); // rating 1-5
                questionStats.put(q.getId(), stats);
            }
        }

        // Process answers
        for (SurveyResponse response : responses) {
            Map<String, Integer> answers = response.getAnswers();

            for (Map.Entry<String, Integer> entry : answers.entrySet()) {
                String qId = entry.getKey();
                int rating = entry.getValue();

                Map<String, Object> stats = questionStats.get(qId);
                if (stats == null) continue;

                int total = (int) stats.get("totalRatings");
                int sum = (int) stats.get("sumRatings");
                int[] dist = (int[]) stats.get("distribution");

                // Update values
                total++;
                sum += rating;

                if (rating >= 1 && rating <= 5) {
                    dist[rating - 1]++;  // count rating
                }

                double avg = (double) sum / total;

                stats.put("totalRatings", total);
                stats.put("sumRatings", sum);
                stats.put("average", Math.round(avg * 10.0) / 10.0);
                stats.put("distribution", dist);
            }
        }

        return questionStats;
    }

    
    private Map<String, Map<String, Object>> calculateGroupStatistics(Survey survey, List<SurveyResponse> responses) {
        Map<String, Map<String, Object>> groupStats = new HashMap<>();
        
        // Initialize all groups
        for (QuestionGroup group : survey.getQuestionGroups()) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRatings", 0);
            stats.put("sumRatings", 0);
            stats.put("average", 0.0);
            stats.put("questionCount", group.getQuestions().size());
            stats.put("responses", responses.size());
            groupStats.put(group.getId(), stats);
        }
        
        // Calculate group statistics
        for (SurveyResponse response : responses) {
            Map<String, Integer> answers = response.getAnswers();
            for (Map.Entry<String, Integer> entry : answers.entrySet()) {
                String questionId = entry.getKey();
                Integer rating = entry.getValue();
                
                // Find which group this question belongs to
                for (QuestionGroup group : survey.getQuestionGroups()) {
                    for (Question question : group.getQuestions()) {
                        if (question.getId().equals(questionId)) {
                            Map<String, Object> stats = groupStats.get(group.getId());
                            if (stats != null) {
                                int totalRatings = (int) stats.get("totalRatings");
                                int sumRatings = (int) stats.get("sumRatings");
                                
                                totalRatings++;
                                sumRatings += rating;
                                double average = (double) sumRatings / totalRatings;
                                
                                stats.put("totalRatings", totalRatings);
                                stats.put("sumRatings", sumRatings);
                                stats.put("average", Math.round(average * 10.0) / 10.0);
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        return groupStats;
    }
    
    private List<Map<String, Object>> extractCommentsFromResponses(List<SurveyResponse> responses) {
        List<Map<String, Object>> comments = new ArrayList<>();
        
        for (SurveyResponse response : responses) {
            String commentText = response.getComments();
            if (commentText != null && !commentText.trim().isEmpty()) {
                Map<String, Object> comment = new HashMap<>();
                comment.put("text", commentText);
                comment.put("date", response.getSubmittedAt());
                
                // Get driver info if available
                DriverInfo driverInfo = response.getDriverInfo();
                if (driverInfo != null) {
                    comment.put("truckNumber", driverInfo.getTruckNumber());
                    comment.put("driverName", driverInfo.getFullName());
                } else {
                    comment.put("truckNumber", "N/A");
                    comment.put("driverName", "អនាមិក");
                }
                
                // Calculate average rating for this response
                Map<String, Integer> answers = response.getAnswers();
                if (!answers.isEmpty()) {
                    double sum = answers.values().stream().mapToInt(Integer::intValue).sum();
                    double avg = sum / answers.size();
                    comment.put("rating", Math.round(avg * 10.0) / 10.0);
                } else {
                    comment.put("rating", 0.0);
                }
                
                comments.add(comment);
            }
        }
        
        // Sort by date descending
        comments.sort((a, b) -> {
            LocalDateTime dateA = (LocalDateTime) a.get("date");
            LocalDateTime dateB = (LocalDateTime) b.get("date");
            return dateB.compareTo(dateA);
        });
        
        return comments;
    }
    
    private int calculateResponseRate(Survey survey) {
        // This would ideally come from actual target audience data
        // For now, we'll calculate based on some logic
        int targetAudience = 100; // This should come from actual data
        int responses = survey.getResponses().size();
        
        if (targetAudience > 0) {
            return (int) ((responses * 100.0) / targetAudience);
        }
        return 0;
    }
}