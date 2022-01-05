package com.example.tp3sqlite;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Handler;
import android.content.DialogInterface;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences infoPref;
    public static final String RESULT_SCORE = "RESULT_SCORE";

    private static final String BUNDLE_STATE_SCORE = "BUNDLE_STATE_SCORE";
    private static final String BUNDLE_STATE_QUESTION_COUNT = "BUNDLE_STATE_QUESTION_COUNT";
    private static final String BUNDLE_STATE_QUESTION_BANK = "BUNDLE_STATE_QUESTION_BANK";

    private static final String SHARED_PREF_USER_INFO = "SHARED_PREF_USER_INFO";

    private static int mScore;

    private static final int INITIAL_QUESTION_COUNT = 5;

    private TextView mTextViewQuestion;
    private Button mAnswerButton1;
    private Button mAnswerButton2;
    private Button mAnswerButton3;
    private Button mAnswerButton4;
    private ImageView mAnswerImage;
    private Button mSuivantButton;
    private TextView mTextViewBravo;

    private int mRemainingQuestionCount;
    private BanqueQuestions mQuestionBank;
    private SQLiteDatabase QuestionBD;

    private boolean mEnableTouchEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoPref = getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE);

        questionnaire();


        //Connect To Question BD
        try{
            //Try to load Question BD
            QuestionBD = SQLiteDatabase.openDatabase("data/data/com.example.tp3sqlite/databases/bdquestions.db", null,
                    SQLiteDatabase.OPEN_READWRITE);

            // On sauvegarde l'état du questionnaire pour un changement d'état éventuel
            if (savedInstanceState != null) {
                mScore = savedInstanceState.getInt(BUNDLE_STATE_SCORE);
                mRemainingQuestionCount = savedInstanceState.getInt(BUNDLE_STATE_QUESTION_COUNT);
                mQuestionBank = (BanqueQuestions) savedInstanceState.getSerializable(BUNDLE_STATE_QUESTION_BANK);
            } else {
                mScore = 0;
                mRemainingQuestionCount = INITIAL_QUESTION_COUNT;
                mQuestionBank = generateQuestionBank();
            }

            displayQuestion(mQuestionBank.getCurrentQuestion());
        }
        catch(Exception e) {
            //Return to menu
            Toast.makeText(this, "Un problème est survenu: Les questions sont introuvable", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    // Méthode de sauvegarde de l'instance
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(BUNDLE_STATE_SCORE, mScore);
        outState.putInt(BUNDLE_STATE_QUESTION_COUNT, mRemainingQuestionCount);
        outState.putSerializable(BUNDLE_STATE_QUESTION_BANK, mQuestionBank);
    }

    // Méthode qui 'écoute' un click à l'écran
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mEnableTouchEvents && super.dispatchTouchEvent(ev);
    }

    // Méthode qui prépare le "layout" du questionnaire
    protected void questionnaire() {
        mEnableTouchEvents = true;

        mTextViewQuestion = findViewById(R.id.main_activity_textview_question);
        mAnswerButton1 = findViewById(R.id.main_activity_button_1);
        mAnswerButton2 = findViewById(R.id.main_activity_button_2);
        mAnswerButton3 = findViewById(R.id.main_activity_button_3);
        mAnswerButton4 = findViewById(R.id.main_activity_button_4);
        mSuivantButton = findViewById(R.id.main_activity_button_suivant);
        mTextViewBravo = findViewById(R.id.main_activity_textview_bravo);

        // Grâce à "mEnableTouchEvent, on écoute les 4 boutons réponses avec le même "listener"
        mAnswerButton1.setOnClickListener(this);
        mAnswerButton2.setOnClickListener(this);
        mAnswerButton3.setOnClickListener(this);
        mAnswerButton4.setOnClickListener(this);
        mSuivantButton.setOnClickListener(this);
    }

    // Méthode qui traite le "click" d'un bouton réponse
    @Override
    public void onClick(View v) {
        int index;
        String msg;

        if (v == mAnswerButton1) {
            index = 0;
        } else if (v == mAnswerButton2) {
            index = 1;
        } else if (v == mAnswerButton3) {
            index = 2;
        } else if (v == mAnswerButton4) {
            index = 3;
        } else {
            throw new IllegalStateException("Unknown clicked view : " + v);
        }

        boolean isCorrect = index == mQuestionBank.getCurrentQuestion().getAnswerIndex();

        int qIndex = mQuestionBank.getCurrentQuestion().getQindex();

        mAnswerButton1.setVisibility(View.GONE);
        mAnswerButton2.setVisibility(View.GONE);
        mAnswerButton3.setVisibility(View.GONE);
        mAnswerButton4.setVisibility(View.GONE);

        mAnswerImage = findViewById(R.id.main_activity_image);

        if(isCorrect) {
            mAnswerImage.setImageDrawable(getDrawable(R.drawable.thumbsup));
            mScore++;
            msg = "bravo… bonne réponse";
        }
        else {
            mAnswerImage.setImageDrawable(getDrawable(R.drawable.thumbsdown));
            msg = "Désolé, mauvaise réponse";
        }

        mTextViewBravo.setText(msg);
        mTextViewBravo.setVisibility(View.VISIBLE);

        mAnswerImage.setVisibility(View.VISIBLE);
        mSuivantButton.setVisibility(View.VISIBLE);


        mSuivantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextStep("good");
            }
        });

    }

    // Méthode qui détermine si le jeu continu ou est terminé
    private void nextStep(String from) {
        mEnableTouchEvents = true;
        mRemainingQuestionCount--;

        if (mRemainingQuestionCount <= 0) {
            endGame();
        } else {
            if (from.equals("good")) {
                mTextViewBravo.setVisibility(View.GONE);
                mAnswerImage.setVisibility(View.GONE);
                mSuivantButton.setVisibility(View.GONE);
            }
            displayQuestion(mQuestionBank.getNextQuestion());
        }

    }

    // Méthode qui traite le message de fin de la partie selon le score
    private void endGame() {
        if (mScore < 3) {
            dialogue("Hum, vous pourriez faire mieux...");
        } else {
            dialogue(" Bien fait ! ");
        }
    }

    // Méthode qui affiche le message et le score à la fin de la partie
    private void dialogue(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(msg)
                .setMessage("Votre score est " + mScore)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra(RESULT_SCORE, mScore);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .create()
                .show();

    }

    // Méthode pour afficher la question courante et les choix de réponse
    private void displayQuestion(final Questions question) {
        mTextViewQuestion.setText(question.getQuestion());
        mAnswerButton1.setText(question.getChoiceList().get(0));
        mAnswerButton2.setText(question.getChoiceList().get(1));
        mAnswerButton3.setText(question.getChoiceList().get(2));
        mAnswerButton4.setText(question.getChoiceList().get(3));
        mAnswerButton1.setVisibility(View.VISIBLE);
        mAnswerButton2.setVisibility(View.VISIBLE);
        mAnswerButton3.setVisibility(View.VISIBLE);
        mAnswerButton4.setVisibility(View.VISIBLE);
    }

    // Méthode pour générer la banque de questions selon la classe 'Questions'
    private BanqueQuestions generateQuestionBank() {
        //Get questions from bd
        List<Questions> ListQuestions = new ArrayList<>();
        Cursor res = QuestionBD.rawQuery("select * from " + Questions.NOM_TABLE + " order by RANDom() limit 5", null);
        res.moveToFirst();

        //Loop through question and store them in to list
        do{
            Questions question = new Questions(res.getInt(res.getColumnIndex(Questions.COLONNE_IDQUESTION)),
                    res.getString(res.getColumnIndex(Questions.COLONNE_QUESTION)),
                    Arrays.asList(
                            res.getString(res.getColumnIndex(Questions.COLONNE_REP1)),
                            res.getString(res.getColumnIndex(Questions.COLONNE_REP2)),
                            res.getString(res.getColumnIndex(Questions.COLONNE_REP3)),
                            res.getString(res.getColumnIndex(Questions.COLONNE_REP4))
                    ),
                    res.getInt(res.getColumnIndex(Questions.COLONNE_IDREPONSE)) -1
            );
            ListQuestions.add(question);
        }
        while (res.moveToNext());
        res.close();

        return new BanqueQuestions(ListQuestions);
    }

}