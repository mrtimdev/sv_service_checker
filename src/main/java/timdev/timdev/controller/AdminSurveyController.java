package timdev.timdev.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import timdev.timdev.entity.Question;
import timdev.timdev.entity.Survey;
import timdev.timdev.enums.QuestionType;
import timdev.timdev.repository.QuestionRepository;
import timdev.timdev.repository.SurveyRepository;

@Controller
@RequestMapping("/admin")
public class AdminSurveyController {
    
    
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;

    public AdminSurveyController(SurveyRepository surveyRepository, QuestionRepository questionRepository) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping("/surveys")
    public String listSurveys(Model model) {
        model.addAttribute("surveys", surveyRepository.findAll());
        return "admin/survey-list";
    }

    @GetMapping("/surveys/new")
    public String newSurveyForm(Model model) {
        model.addAttribute("survey", new Survey());
        return "admin/survey-form";
    }

    @PostMapping("/surveys")
    public String createSurvey(@ModelAttribute Survey survey) {
        survey.setCreatedAt(LocalDateTime.now());
        surveyRepository.save(survey);
        return "redirect:/admin/surveys";
    }

    @GetMapping("/surveys/{surveyId}/questions/new")
    public String newQuestionForm(@PathVariable Long surveyId, Model model) {
        model.addAttribute("surveyId", surveyId);
        model.addAttribute("question", new Question());
        model.addAttribute("types", QuestionType.values());
        return "admin/question-form";
    }

    @PostMapping("/surveys/{surveyId}/questions")
    public String createQuestion(@PathVariable Long surveyId, @ModelAttribute Question question) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow();
        question.setSurvey(survey);
        questionRepository.save(question);
        return "redirect:/admin/surveys/" + surveyId + "/questions";
    }
}
