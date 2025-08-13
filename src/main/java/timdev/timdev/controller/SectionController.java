package timdev.timdev.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import timdev.timdev.entity.Section;
import timdev.timdev.repository.SectionRepository;
import timdev.timdev.repository.SurveyRepository;

@Controller
@RequestMapping("/sections")
public class SectionController {

    private final SectionRepository sectionRepository;
    private final SurveyRepository surveyRepository;

    public SectionController(SectionRepository sectionRepository, SurveyRepository surveyRepository) {
        this.sectionRepository = sectionRepository;
        this.surveyRepository = surveyRepository;
    }

    @GetMapping
    public String listSections(Model model) {
        List<Section> sections = sectionRepository.findAll();
        model.addAttribute("sections", sections);
        model.addAttribute("section", new Section()); // Empty section for the form
        return "sections/index";
    }

    @PostMapping
    public String createSection(@Valid @ModelAttribute("section") Section section, 
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "sections/index";
        }

        if (section.getSurvey() != null && section.getSurvey().getId() == null) {
            surveyRepository.save(section.getSurvey());
        }
        
        sectionRepository.save(section);
        redirectAttributes.addFlashAttribute("successMessage", "Section created successfully!");
        return "redirect:/sections";
    }

    @GetMapping("/{id}/edit")
    public String editSectionForm(@PathVariable Long id, Model model) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid section ID: " + id));
        model.addAttribute("section", section);
        model.addAttribute("sections", sectionRepository.findAll());
        return "sections/index";
    }

    @PostMapping("/{id}")
    public String updateSection(@PathVariable Long id,
                              @Valid @ModelAttribute("section") Section section,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "sections/index";
        }
        
        section.setId(id);
        sectionRepository.save(section);
        redirectAttributes.addFlashAttribute("successMessage", "Section updated successfully!");
        return "redirect:/sections";
    }

    @PostMapping("/{id}/delete")
    public String deleteSection(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid section ID: " + id));
        
        sectionRepository.delete(section);
        redirectAttributes.addFlashAttribute("successMessage", "Section deleted successfully!");
        return "redirect:/sections";
    }
}
