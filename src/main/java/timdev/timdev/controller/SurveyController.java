package timdev.timdev.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import timdev.timdev.entity.Response;
import timdev.timdev.entity.Survey;
import timdev.timdev.repository.ResponseRepository;
import timdev.timdev.repository.SurveyRepository;

@Controller
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyRepository surveyRepository;
    private final ResponseRepository responseRepository;

    public SurveyController(SurveyRepository surveyRepository, ResponseRepository responseRepository) {
        this.surveyRepository = surveyRepository;
        this.responseRepository = responseRepository;
    }

    @GetMapping("/{id}")
    public String showSurvey(@PathVariable Long id, Model model) {
        Survey survey = surveyRepository.findById(id).orElseThrow();
        model.addAttribute("survey", survey);
        return "survey/fill-survey";
    }

    @PostMapping("/{id}/submit")
    public String submitSurvey(@PathVariable Long id, @RequestParam Map<String, String> formData) {
        Survey survey = surveyRepository.findById(id).orElseThrow();
        Response response = new Response();
        response.setSurvey(survey);
        response.setSubmittedAt(LocalDateTime.now());
        // Parse answers from formData, save Answer entities linked to Response
        // ...
        responseRepository.save(response);
        return "survey/thankyou";
    }
}