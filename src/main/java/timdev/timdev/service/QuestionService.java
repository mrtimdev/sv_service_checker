package timdev.timdev.service;


import timdev.timdev.entity.Question;
import timdev.timdev.entity.QuestionGroup;
import timdev.timdev.entity.Survey;
import timdev.timdev.repository.QuestionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
public class QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    public Survey createDefaultSurvey() {
        Survey survey = new Survey();
        survey.setTitle("ការស្ទង់មតិពីការពេញចិត្តរបស់អ្នកបើកបរ SV TRUCKING ឆ្នាំ២០២៥");
        survey.setDescription("គោលបំណង៖ ប្រមូលមតិកែលម្អប្រកបដោយស្មោះត្រង់ និងស្ថាបនា ដើម្បីឱ្យថ្នាក់ដឹកនាំយល់ពីបញ្ហាប្រឈមរបស់អ្នកបើកបរ និងកែលម្អការរៀបចំផែនការដំណើរ ការទំនាក់ទំនង GPS ការថែទាំជួសជុល និងការគាំទ្រទូទៅ។");
        survey.setScaleDescription("មាត្រដ្ឋានវាយតម្លៃ៖ ១ = មិនល្អខ្លាំង | ២ = មិនល្អ | ៣ = មធ្យម | ៤ = ល្អ | ៥ = ល្អបំផុត");
        survey.setActive(true);
        
        // Create question groups and questions
        createQuestionGroups(survey);
        
        return survey;
    }
    
    private void createQuestionGroups(Survey survey) {
        // Group A
        QuestionGroup groupA = new QuestionGroup();
        groupA.setTitle("A. ក្រុមរៀបចំផែនការដំណើរ និង GPS (PLN + GPS)");
        groupA.setCode("PLN_GPS");
        groupA.setSurvey(survey);
        groupA.setOrderIndex(1);
        
        List<Question> questionsA = List.of(
            createQuestion("១. ភាពត្រឹមត្រូវនៃការរៀបចំផែនការដំណើរ", 
                          "តើអ្នកពេញចិត្តប៉ុណ្ណាចំពោះភាពត្រឹមត្រូវនៃការចាត់តាំងផ្លូវ ផ្លូវធ្វើដំណើរ និងកាលវិភាគពេលវេលា?", 
                          1, groupA),
            createQuestion("២. ភាពយុត្តិធម៌នៃការចែកចាយការងារ", 
                          "តើការចែកចាយដំណើរ បន្ទុកការងារ និងការលុបចោល មានភាពយុត្តិធម៌ប៉ុណ្ណា?", 
                          2, groupA),
            createQuestion("៣. គុណភាពនៃការទំនាក់ទំនង", 
                          "តើអ្នកពេញចិត្តប៉ុណ្ណាចំពោះការទំនាក់ទំនងពីក្រុមរៀបចំផែនការដំណើរ + GPS នៅពេលទទួលដំណើរ ឬការផ្លាស់ប្តូរ?", 
                          3, groupA),
            createQuestion("៤. សម្លេងនិយាយ និងការគោរពរបស់ក្រុម GPS", 
                          "តើក្រុម GPS មានសម្លេងនិយាយគួរសម សូមាគរ និងស្ងប់ស្ងាត់ប៉ុណ្ណានៅពេលទាក់ទងអ្នក?", 
                          4, groupA),
            createQuestion("៥. ការគាំទ្រផ្លូវចិត្តរបស់ក្រុម GPS ក្នុងស្ថានភាពលំបាក", 
                          "តើក្រុម GPS ផ្តល់ការគាំទ្រផ្លូវចិត្តប៉ុណ្ណា នៅពេលអ្នកប្រឈមមុខនឹងចរាចរណ៍ ឡានខូច ការពន្យារពេល ឬស្ថានភាពស្ត្រេស?", 
                          5, groupA),
            createQuestion("៦. ភាពជាអ្នកដឹកនាំ និងការសម្រេចចិត្ត", 
                          "តើអ្នកពេញចិត្តប៉ុណ្ណាចំពោះរបៀបដឹកនាំរបស់ក្រុមរៀបចំផែនការដំណើរ + GPS (ភាពច្បាស់លាស់ យុត្តិធម៌ វិជ្ជាជីវៈ)?", 
                          6, groupA),
            createQuestion("៧. ផលប៉ះពាល់ផ្លូវចិត្តលើការងាររបស់អ្នក", 
                          "តើក្រុមរៀបចំផែនការដំណើរ + GPS បន្ថយភាពតានតឹង និងជួយឱ្យអ្នកមានទំនុកចិត្តប៉ុណ្ណា ក្នុងអំឡុងប្រតិបត្តិការប្រចាំថ្ងៃ?", 
                          7, groupA)
        );
        groupA.setQuestions(questionsA);
        
        // Group B
        QuestionGroup groupB = new QuestionGroup();
        groupB.setTitle("B. ក្រុមបច្ចេកទេស និងថែទាំជួសជុល (RMP/DRG)");
        groupB.setCode("RMP_DRG");
        groupB.setSurvey(survey);
        groupB.setOrderIndex(2);
        
        List<Question> questionsB = List.of(
            createQuestion("៨. គុណភាពនៃការជួសជុល", "", 8, groupB),
            createQuestion("៩. ល្បឿននៃសេវាថែទាំ", "", 9, groupB),
            createQuestion("១០. វិជ្ជាជីវៈ និងការគោរពរបស់ជាងមេកានិច", "", 10, groupB)
        );
        groupB.setQuestions(questionsB);
        
        // Group C
        QuestionGroup groupC = new QuestionGroup();
        groupC.setTitle("C. ការគាំទ្រពីក្រុមដោះស្រាយវិវាទ (DRG)");
        groupC.setCode("DRG");
        groupC.setSurvey(survey);
        groupC.setOrderIndex(3);
        
        List<Question> questionsC = List.of(
            createQuestion("១១. ការគាំទ្រនៅពេលមានបញ្ហា", "", 11, groupC),
            createQuestion("១២. ការទំនាក់ទំនង និងល្បឿនដោះស្រាយបញ្ហា", "", 12, groupC)
        );
        groupC.setQuestions(questionsC);
        
        // Group D
        QuestionGroup groupD = new QuestionGroup();
        groupD.setTitle("D. បទពិសោធន៍ទូទៅ");
        groupD.setCode("GENERAL");
        groupD.setSurvey(survey);
        groupD.setOrderIndex(4);
        
        List<Question> questionsD = List.of(
            createQuestion("១៣. ការពេញចិត្តទូទៅចំពោះការគាំទ្រពីក្រុមហ៊ុន", "", 13, groupD)
        );
        groupD.setQuestions(questionsD);
        
        survey.setQuestionGroups(List.of(groupA, groupB, groupC, groupD));
    }
    
    private Question createQuestion(String text, String description, int orderIndex, QuestionGroup group) {
        Question question = new Question();
        question.setText(text);
        question.setDescription(description);
        question.setOrderIndex(orderIndex);
        question.setQuestionGroup(group);
        question.setRequired(true);
        return question;
    }

    public List<Question> findByQuestionGroupId(String questionGroupId) {
        return questionRepository.findByQuestionGroupIdOrderByOrderIndex(questionGroupId);
    }
    
    public Question save(Question question) {
        return questionRepository.save(question);
    }

   
    public void delete(String id) {
        questionRepository.deleteById(UUID.fromString(id));
    }
}