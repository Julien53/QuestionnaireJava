package com.example.tp3sqlite;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences infoPref;
    private static final int GAME_ACTIVITY_REQUEST_CODE = 42;
    private static final String SHARED_PREF_USER_INFO = "SHARED_PREF_USER_INFO";
    private static final String SHARED_PREF_USER_INFO_NAME = "SHARED_PREF_USER_INFO_NAME";
    private static final String SHARED_PREF_USER_INFO_SCORE = "SHARED_PREF_USER_INFO_SCORE";

    private TextView mGreetingTextView;
    private TextView mUserTextView;
    private TextView mPassWordTextView;
    private Button mResetButton;
    private Button mFinishButton;
    private Button mConnectButton;
    private Button mCreateButton;

    private SQLiteDatabase bd;
    private JoueurHelper joueurHelper;
    private Integer id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Au lancement du jeu, on efface les préférences de joueur, si elles existent
        if (SHARED_PREF_USER_INFO_NAME != null) {
            infoPref = getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE);
            SharedPreferences.Editor editor = infoPref.edit();
            editor.clear();
            editor.apply();
        }
        mGreetingTextView = findViewById(R.id.main_textview_greeting);
        mUserTextView = findViewById(R.id.main_edittext_nom);
        mPassWordTextView = findViewById(R.id.main_edittext_password);
        mResetButton = findViewById(R.id.main_button_delete);
        mFinishButton = findViewById(R.id.finish_play);
        mConnectButton = findViewById(R.id.main_button_connect);
        mCreateButton = findViewById(R.id.main_button_create);

        try{
            joueurHelper = new JoueurHelper(getApplicationContext());
        }
        catch (Exception e){
            System.out.print(e.getMessage());
        }

        bd = joueurHelper.getWritableDatabase();

        // On écoute le bouton "Quitter"
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });

        // On écoute un click du bouton "Jouons"
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               try{
                   String message = joueurHelper.deleteAllEntries() ? "All entries deleted" : "La bd est fermé";
                   Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
               }
               catch (Exception e){
                   Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
               }
            }
        });

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = mUserTextView.getText().toString();
                String password = mPassWordTextView.getText().toString();

               try{
                    String resultMessage = joueurHelper.RegisterUser(username, password);

                    if(resultMessage.isEmpty()){
                        String title = "Utilisateur créer";
                        String msg = "Bienvenu votre compte à été créé avec succès. Vous pouvez débuter le questionnaire en cliquant sur \"SE CONNECTER\"";
                        dialogue(msg, title, false);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), resultMessage, Toast.LENGTH_SHORT).show();
                    }

               }
               catch (Exception e){
                   Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
               }
            }
        });

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = mUserTextView.getText().toString();
                String password = mPassWordTextView.getText().toString();

                if(!username.isEmpty()){
                    try {
                        Integer[] res = joueurHelper.FindUser(username, password);
                        if(res[0] != 0){
                            id = res[0];

                            String title = res[1] == -1 ? "Bienvenu" : "Rebienvenu";
                            String msg = res[1] == -1 ? "Aucun score trouvé" : "Votre meilleur score est de " + res[1];

                            dialogue(msg,title,true);
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Il n'y pas compte qui correspond aux données. Veuillez cliquer sur \'Créer Joueur\'", Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Veuillez entrer un nom", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void dialogue(String msg, String title, Boolean startGame) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(startGame)
                            mStartForResult.launch(new Intent(LoginActivity.this, MainActivity.class));
                    }
                })
                .create()
                .show();
    }

    // Méthode pour recevoir le résultat après la partie - associée à la nouvelle façon de lancer l'activité
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        Integer score = intent.getIntExtra(MainActivity.RESULT_SCORE, 0);

                        try{
                            String msg = "Vous n'avez pas battu vote meilleur score";
                            if(joueurHelper.UpdateScore(id, score)){
                                msg = "Vote score à été sauvegardé";
                            }
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        }
                        catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Une erreur est sourvenu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        //greetUser();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Un problème est survenu: Le score n'a pas pu être sauvegardé", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    protected void onDestroy () {
        joueurHelper.closeBD();
        super.onDestroy();
    }

    // Message d'acceuil du joueur
    /*private void greetUser() {
        //String firstName = getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE).getString(SHARED_PREF_USER_INFO_NAME, null);
        //int score = getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE).getInt(SHARED_PREF_USER_INFO_SCORE, -1);
        String firstName =
        if (firstName != null) {
            if (score != -1) {
                mGreetingTextView.setText(getString(R.string.welcome_back_with_score, firstName, score));
            } else {
                mGreetingTextView.setText(getString(R.string.welcome_back, firstName));
            }
        } else {
            mGreetingTextView.setText("Bonjour !!");
        }
        mUserTextView.setText(firstName);

    }*/

}