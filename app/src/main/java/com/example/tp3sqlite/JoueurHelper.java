package com.example.tp3sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.text.MessageFormat;

public class JoueurHelper extends SQLiteOpenHelper {

    public static final int VERSION_BD = 2;
    public static final String NOM_BD = "BDJoueur.db";
    private SQLiteDatabase BDJoueur;
    private String createTableQuery = MessageFormat.format(
            "CREATE TABLE IF NOT EXISTS {0} ({1} integer NOT NULL PRIMARY KEY, {2} text(50), {3} text(50), {4} integer);",
            Joueur.NOM_TABLE,
            Joueur._ID,
            Joueur.COLONNE_NOM,
            Joueur.COLONNE_MOTDEPASSE,
            Joueur.COLONNE_SCORE);


    public JoueurHelper(Context context) {
        super(context, NOM_BD, null, VERSION_BD);
        BDJoueur = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableQuery);
        BDJoueur = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Delete all users from database
     * @return state of query
     * @throws Exception
     */
    public Boolean deleteAllEntries() throws Exception{

        if (BDJoueur.isOpen()) {
            BDJoueur.execSQL("Delete from " + Joueur.NOM_TABLE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Store username, password and score in database. Score is set to -1 by default
     * @param username username to insert into the db
     * @param password password to insert into the db
     * @return Return the error massage or an empty string if successful
     * */
    public String RegisterUser(String username, String password) throws Exception{

        try {
            if(BDJoueur.isOpen()){
                if (!username.isEmpty()) {

                    Integer[] res = FindUser(username, password);

                    if(res[0] == 0) {
                        ContentValues cv = new ContentValues();
                        cv.put(Joueur.COLONNE_NOM, username);
                        cv.put(Joueur.COLONNE_MOTDEPASSE, password);
                        cv.put(Joueur.COLONNE_SCORE, -1);

                        Long key = BDJoueur.insert(Joueur.NOM_TABLE, null, cv);
                        if (key == -1) { return "Un problème est survenu, l'utilisateur n'a pas pu être créé."; }
                        return "";
                    }
                    else{return "Ce compte existe déjà veuillez vous connecter."; }
                }
                else { return "Veuillez entrer un nom"; }
            }
            else{ return "La bd est fermé"; }
        }
        catch (Exception e){ throw new Exception(e.getMessage()); }
    }

    /**
     * Look for user who matchs both username and password
     * @param username Username to find
     * @param password Password to find
     * @return  [Id user, score user], set by default to [0, -1]
     * @throws Exception
     */
    public Integer[] FindUser(String username, String password) throws Exception {

        Cursor result;
        Integer[] val = {0, -1};

        try {
            result = BDJoueur.query(
                    Joueur.NOM_TABLE,
                    new String[]{Joueur._ID, Joueur.COLONNE_SCORE},
                    Joueur.COLONNE_NOM + " = ? and " + Joueur.COLONNE_MOTDEPASSE + " = ?",
                    new String[]{username, password},
                    null,
                    null,
                    null
            );

            if (result.moveToFirst()) {
                val[1] = result.getInt(result.getColumnIndex(Joueur.COLONNE_SCORE));
                val[0] = result.getInt(result.getColumnIndex(Joueur._ID));
                result.close();
            }
            return  val;

        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    /**
     * Update the score of the user if is higher than last
     * @param id
     * @param score
     * @return  true if score was higher than last, false if not
     * @throws Exception
     */
    public boolean UpdateScore(Integer id, Integer score) throws Exception {
        try {
            if(score > findScore(id)){
                ContentValues cv = new ContentValues();
                cv.put(Joueur.COLONNE_SCORE, score.toString());
                BDJoueur.update(Joueur.NOM_TABLE, cv, Joueur._ID + " = ?", new String[]{id.toString()});
                return true;
            }
            else{
                return false;
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    /**
     * Find the score of the user with his id
     * @param id
     * @return  Score of the user.
     * @throws Exception
     */
    public Integer findScore(Integer id) throws Exception{
        try {
            Cursor result = BDJoueur.query(
                    Joueur.NOM_TABLE,
                    new String[]{Joueur.COLONNE_SCORE},
                    Joueur._ID + " = ?",
                    new String[]{id.toString()},
                    null,
                    null,
                    null
            );
            result.moveToFirst();
            int score = result.getInt(result.getColumnIndex(Joueur.COLONNE_SCORE));
            result.close();
            return score;
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    public void closeBD() {
        if (BDJoueur.isOpen()) { BDJoueur.close();}
    }



}
