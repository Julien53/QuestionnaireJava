package com.example.tp3sqlite;

import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import java.util.List;

public class Questions{

    public static final String NOM_TABLE = "questions";
    public static final String COLONNE_IDQUESTION = "idQuestion";
    public static final String COLONNE_QUESTION = "question";
    public static final String COLONNE_REP1 = "rep1";
    public static final String COLONNE_REP2 = "rep2";
    public static final String COLONNE_REP3 = "rep3";
    public static final String COLONNE_REP4 = "rep4";
    public static final String COLONNE_IDREPONSE = "idReponse";

    @NonNull
    private final int mQindex;

    @NonNull
    private final String mQuestion;

    @NonNull
    private final List<String> mChoiceList;
    private final int mAnswerIndex;

    public Questions(@NonNull int index, @NonNull String question, @NonNull List<String> choiceList, int answerIndex) {
        mQindex = index;
        mQuestion = question;
        mChoiceList = choiceList;
        mAnswerIndex = answerIndex;
    }

    @NonNull
    public int getQindex() { return mQindex; }

    @NonNull
    public String getQuestion() {
        return mQuestion;
    }

    @NonNull
    public List<String> getChoiceList() {
        return mChoiceList;
    }

    public int getAnswerIndex() {
        return mAnswerIndex;
    }
}
