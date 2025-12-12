package timdev.timdev.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question_group")
public class QuestionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;
    private String code;

    @OneToMany(mappedBy = "questionGroup", 
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private final List<Question> questions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    @JsonIgnore
    private Survey survey;

    private int orderIndex;

    // ---------- Helper Methods ----------
    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuestionGroup(this);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuestionGroup(null);
    }

    // ---------- FIXED: Proper Setter ----------
    public void setQuestions(List<Question> newQuestions) {
        // Remove old questions
        for (Question q : new ArrayList<>(this.questions)) {
            q.setQuestionGroup(null);
        }
        this.questions.clear();

        // Add new questions
        if (newQuestions != null) {
            for (Question q : newQuestions) {
                q.setQuestionGroup(this);
                this.questions.add(q);
            }
        }
    }

    // ---------- Getters & Setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public List<Question> getQuestions() { return questions; }

    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
