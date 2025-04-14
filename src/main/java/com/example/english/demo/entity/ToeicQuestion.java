package com.example.english.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToeicQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long questionId;
    private String questionText;
    private String dapAn1;
    private String dapAn2;
    private String dapAn3;
    private String dapAn4;
    private String correctAnswer;
    private String image;
    private Integer part;

    @ManyToOne
    @JoinColumn(name = "fk_exam_id" , referencedColumnName = "exam_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ToeicExam toeicExam;

    public boolean isCorrectAnswer(String answer) {
        return correctAnswer.equals(answer);
    }
}
