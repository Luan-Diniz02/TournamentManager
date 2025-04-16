package br.com.luandiniz.tournamentmanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import br.com.luandiniz.tournamentmanager.model.Duelista;

public class DAOSQLITE extends SQLiteOpenHelper {

    private static DAOSQLITE instance;
    private static final int DATABASE_VERSION = 3; // Incrementado para adicionar o campo participacoes

    public DAOSQLITE(Context context) {
        super(context, "torneios", null, DATABASE_VERSION);
    }

    public static synchronized DAOSQLITE getInstance(Context context) {
        if (instance == null) {
            instance = new DAOSQLITE(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlDuelistas = "CREATE TABLE Duelistas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "vitorias INTEGER NOT NULL, " +
                "derrotas INTEGER NOT NULL, " +
                "empates INTEGER NOT NULL, " +
                "participacoes INTEGER NOT NULL, " + // Novo campo
                "pontos INTEGER NOT NULL)";
        db.execSQL(sqlDuelistas);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Adicionar o campo participacoes com valor padrão 2
            db.execSQL("ALTER TABLE Duelistas ADD COLUMN participacoes INTEGER NOT NULL DEFAULT 2");
        }
    }

    public List<Duelista> listarDuelistas() {
        List<Duelista> duelistas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Duelistas", null);

        if (cursor.moveToFirst()) {
            do {
                Duelista duelista = new Duelista(
                        cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("vitorias")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("derrotas")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("empates"))
                );
                duelista.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                duelista.setParticipacao(cursor.getInt(cursor.getColumnIndexOrThrow("participacoes")));
                duelista.setPontos(cursor.getInt(cursor.getColumnIndexOrThrow("pontos")));
                duelistas.add(duelista);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return duelistas;
    }

    public void adicionarDuelista(Duelista duelista) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nome", duelista.getNome());
            values.put("vitorias", duelista.getVitorias());
            values.put("derrotas", duelista.getDerrotas());
            values.put("empates", duelista.getEmpates());
            values.put("participacoes", duelista.getParticipacao());
            values.put("pontos", duelista.getPontos());
            db.insert("Duelistas", null, values);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}