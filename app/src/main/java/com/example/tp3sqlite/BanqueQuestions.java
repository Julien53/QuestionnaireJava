package com.example.tp3sqlite;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class BanqueQuestions implements Serializable {



    private final List<Questions> mQuestionList;
    private int mQuestionIndex;

    // Méthode pour mélanger l'ordre des questions de 'mQuestionList'
    public BanqueQuestions(List<Questions> questionList) {
        mQuestionList = questionList;
        Collections.shuffle(mQuestionList);
    }

    // Getter qui retourne l'index de la question courante
    public Questions getCurrentQuestion() {
        return mQuestionList.get(mQuestionIndex);
    }

    // Getter qui retourne la prochaine question
    public Questions getNextQuestion() {
        mQuestionIndex++;
        return getCurrentQuestion();
    }
}
