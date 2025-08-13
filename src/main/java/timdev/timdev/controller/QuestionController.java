package timdev.timdev.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import timdev.timdev.entity.Question;
import timdev.timdev.entity.Section;
import timdev.timdev.enums.QuestionType;
import timdev.timdev.repository.OptionRepository;
import timdev.timdev.repository.QuestionRepository;
import timdev.timdev.repository.SectionRepository;

@Controller
@RequestMapping("/sections/{sectionId}/questions")
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final SectionRepository sectionRepository;
    private final OptionRepository optionRepository;

    public QuestionController(QuestionRepository questionRepository, 
                            SectionRepository sectionRepository,
                            OptionRepository optionRepository) {
        this.questionRepository = questionRepository;
        this.sectionRepository = sectionRepository;
        this.optionRepository = optionRepository;
    }

    @GetMapping
    public String listQuestions(@PathVariable Long sectionId, Model model) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid section ID: " + sectionId));
        
        model.addAttribute("section", section);
        model.addAttribute("questions", section.getQuestions());
        model.addAttribute("question", new Question()); // For the create form
        model.addAttribute("questionTypes", QuestionType.values());
        
        return "questions/index";
    }

    @PostMapping
    public String createQuestion(@PathVariable Long sectionId,
                               @Valid @ModelAttribute("question") Question question,
                               BindingResult result,
                               Model model) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid section ID: " + sectionId));
        
        if (result.hasErrors()) {
            model.addAttribute("section", section);
            model.addAttribute("questions", section.getQuestions());
            model.addAttribute("questionTypes", QuestionType.values());
            return "questions/index";
        }
        
        question.setSection(section);
        questionRepository.save(question);
        
        return "redirect:/sections/" + sectionId + "/questions";
    }

    @GetMapping("/{questionId}/edit")
    public String editQuestionForm(@PathVariable Long sectionId,
                                 @PathVariable Long questionId,
                                 Model model) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid section ID: " + sectionId));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid question ID: " + questionId));
        
        model.addAttribute("section", section);
        model.addAttribute("question", question);
        model.addAttribute("questionTypes", QuestionType.values());
        
        return "questions/edit";
    }

    @PostMapping("/{questionId}")
    public String updateQuestion(@PathVariable Long sectionId,
                               @PathVariable Long questionId,
                               @Valid @ModelAttribute("question") Question question,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("section", sectionRepository.findById(sectionId).orElseThrow());
            model.addAttribute("questionTypes", QuestionType.values());
            return "questions/edit";
        }
        
        question.setId(questionId);
        question.setSection(sectionRepository.findById(sectionId).orElseThrow());
        questionRepository.save(question);
        
        return "redirect:/sections/" + sectionId + "/questions";
    }

    @PostMapping("/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long sectionId,
                               @PathVariable Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid question ID: " + questionId));
        questionRepository.delete(question);
        
        return "redirect:/sections/" + sectionId + "/questions";
    }
}