package timdev.timdev.controller;

import java.time.LocalDateTime;

import timdev.timdev.entity.*;
import timdev.timdev.repository.SurveyResponseRepository;
import timdev.timdev.service.SurveyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/s")
public class AdminController {
    
    @Autowired
    private SurveyService surveyService;
    
    @Autowired
    private SurveyResponseRepository surveyResponseRepository;
    
    @GetMapping("results/{accessCode}")
    public String viewResults(@PathVariable String accessCode, Model model) {
        Optional<Survey> surveyOpt = surveyService.getSurveyByAccessCode(accessCode);
        
        if (surveyOpt.isEmpty()) {
            model.addAttribute("error", "សំណើមិនត្រឹមត្រូវ។ កូដស្ទង់មតិមិនត្រឹមត្រូវ ឬមិនមាន។");
            return "error";
        }
        
        Survey survey = surveyOpt.get();
        UUID surveyId = survey.getId();
        
        // Get actual response count
        long responseCount = surveyResponseRepository.countResponsesBySurveyId(surveyId);
        
        // Get responses for this survey with driver info
        List<SurveyResponse> responses = surveyResponseRepository.findResponsesWithDriverInfoBySurveyId(surveyId);
        
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
    
    @GetMapping("/results-by-id/{id}")
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
        
        if (responses == null || responses.isEmpty()) {
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
            if (answers != null) {
                for (Integer rating : answers.values()) {
                    totalRating += rating;
                    totalAnswers++;
                    highestRating = Math.max(highestRating, rating);
                    lowestRating = Math.min(lowestRating, rating);
                    
                    ratingCounts.put(rating, ratingCounts.getOrDefault(rating, 0) + 1);
                }
            }
        }
        
        double averageRating = totalAnswers > 0 ? totalRating / totalAnswers : 0;
        
        // Calculate rating distribution percentages
        Map<String, Integer> ratingDistribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            int count = ratingCounts.getOrDefault(i, 0);
            int percentage = totalAnswers > 0 ? (int) ((count * 100.0) / totalAnswers) : 0;
            String label = getRatingLabel(i);
            ratingDistribution.put(label, percentage);
        }
        
        stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        stats.put("responseRate", calculateResponseRate(survey, responses.size()));
        stats.put("highestRating", highestRating);
        stats.put("lowestRating", lowestRating);
        stats.put("totalResponses", responses.size());
        stats.put("completionTime", "10-15 នាទី");
        stats.put("ratingDistribution", ratingDistribution);
        stats.put("totalAnswers", totalAnswers);
        
        return stats;
    }
    
    private String getRatingLabel(int rating) {
        return switch (rating) {
            case 1 -> "មិនល្អខ្លាំង (1)";
            case 2 -> "មិនល្អ (2)";
            case 3 -> "មធ្យម (3)";
            case 4 -> "ល្អ (4)";
            case 5 -> "ល្អបំផុត (5)";
            default -> String.valueOf(rating);
        };
    }
    
    private Map<String, Map<String, Object>> calculateQuestionStatistics(Survey survey, List<SurveyResponse> responses) {
        Map<String, Map<String, Object>> questionStats = new HashMap<>();
        
        if (responses == null || responses.isEmpty()) {
            return questionStats;
        }
        
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
            if (answers != null) {
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
        }
        
        return questionStats;
    }
    
    private Map<String, Map<String, Object>> calculateGroupStatistics(Survey survey, List<SurveyResponse> responses) {
        Map<String, Map<String, Object>> groupStats = new HashMap<>();
        
        if (responses == null || responses.isEmpty()) {
            return groupStats;
        }
        
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
            if (answers != null) {
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
        }
        
        return groupStats;
    }
    
    private List<Map<String, Object>> extractCommentsFromResponses(List<SurveyResponse> responses) {
        List<Map<String, Object>> comments = new ArrayList<>();
        
        if (responses == null || responses.isEmpty()) {
            return comments;
        }
        
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
                if (answers != null && !answers.isEmpty()) {
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
    
    private int calculateResponseRate(Survey survey, int responseCount) {
        // This is a simplified calculation
        // In a real app, you might have a target audience count
        int targetAudience = 100; // Default value
        return targetAudience > 0 ? (int) ((responseCount * 100.0) / targetAudience) : 0;
    }
}